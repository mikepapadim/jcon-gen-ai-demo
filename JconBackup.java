import uk.ac.manchester.tornado.api.GridScheduler;
import uk.ac.manchester.tornado.api.ImmutableTaskGraph;
import uk.ac.manchester.tornado.api.KernelContext;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.TornadoExecutionResult;
import uk.ac.manchester.tornado.api.WorkerGrid;
import uk.ac.manchester.tornado.api.WorkerGrid1D;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.common.TornadoDevice;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;
import uk.ac.manchester.tornado.api.exceptions.TornadoExecutionPlanException;
import uk.ac.manchester.tornado.api.types.arrays.FloatArray;

import java.util.ArrayList;

/**
 * Simple TornadoVM demo showing:
 *  1. Vector addition using @Parallel annotation
 *  2. The same computation using KernelContext and explicit Grid configuration
 */
public class JconBackup {

    // ------------------------------
    // 1. Simple Parallel Vector Add
    // ------------------------------
    public static void vectorAdd(FloatArray a, FloatArray b, FloatArray c) {
        // @Parallel tells TornadoVM this loop can be executed in parallel
        for (@Parallel int i = 0; i < a.getSize(); i++) {
            c.set(i, a.get(i) + b.get(i));
        }
    }

    // ----------------------------------------
    // 2. Kernel version using KernelContext API
    // ----------------------------------------
    public static void vectorAddKernel(KernelContext kernelContext, FloatArray a, FloatArray b, FloatArray c) {
        // The global ID of this thread (like CUDA's threadIdx + blockIdx * blockDim)
        int globalIdX = kernelContext.globalIdx;

        // Each thread handles one element
        c.set(globalIdX, a.get(globalIdX) + b.get(globalIdX));
    }

    // -----------------
    // 3. Main Execution
    // -----------------
    public static void main(String[] args) {
        System.out.println("Welcome to TornadoVM");

        // Allocate three FloatArrays of length 100
        FloatArray a = new FloatArray(100);
        FloatArray b = new FloatArray(100);
        FloatArray c = new FloatArray(100);

        // Initialize input arrays
        a.init(10f);
        b.init(20f);

        // -------------------------
        //  First TaskGraph Example
        // -------------------------
        TaskGraph tg = new TaskGraph("s0");

        // Move data to the device before each execution
        tg.transferToDevice(DataTransferMode.EVERY_EXECUTION, a, b);

        // Add computation task (uses @Parallel version)
        tg.task("t0", JconBackup::vectorAdd, a, b, c);

        // Bring result back to host
        tg.transferToHost(DataTransferMode.EVERY_EXECUTION, c);

        // Freeze (snapshot) the task graph before execution
        ImmutableTaskGraph immutableTaskGraph = tg.snapshot();

        // Create a TornadoExecutionPlan from the graph
        TornadoExecutionPlan tornadoExecutionPlan = new TornadoExecutionPlan(immutableTaskGraph);

        // Execute the plan on the selected device
        tornadoExecutionPlan.execute();

        // Retrieve the result to print on host
        float[] output = c.toHeapArray();
        System.out.println("Output after first vectorAdd:");
        for (int i = 0; i < output.length; i++) {
            System.out.print(output[i] + " ");
        }
        System.out.println();

        // ---------------------------------------
        //  Second Example: Using KernelContext API
        // ---------------------------------------
        KernelContext kernelContext = new KernelContext();

        // Define a 1D grid of threads (size = array length)
        WorkerGrid workerGrid = new WorkerGrid1D(a.getSize());

        // Set the number of threads per block/workgroup
        workerGrid.setLocalWork(10, 1, 1);

        // Scheduler that maps kernel names to specific grids
        GridScheduler gridScheduler = new GridScheduler();
        gridScheduler.addWorkerGrid("s1.t2", workerGrid);

        // Create new arrays for second computation
        FloatArray newA = FloatArray.fromSegment(c.getSegment());
        FloatArray updateOutput = new FloatArray(100);

        // Create another task graph for kernel-based version
        TaskGraph taskGraph = new TaskGraph("s1");
        taskGraph.transferToDevice(DataTransferMode.EVERY_EXECUTION, newA, b);
        taskGraph.task("t2", JconBackup::vectorAddKernel, kernelContext, newA, b, updateOutput);
        taskGraph.transferToHost(DataTransferMode.EVERY_EXECUTION, updateOutput);

        ImmutableTaskGraph immutableTaskGraph1 = taskGraph.snapshot();

        // Attach grid scheduler to the execution plan
        TornadoExecutionPlan tornadoExecutionPlan1 =
                new TornadoExecutionPlan(immutableTaskGraph1).withGridScheduler(gridScheduler);

        // Execute and collect result
        TornadoExecutionResult res = tornadoExecutionPlan1.execute();

        System.out.println("Output after kernelContext vectorAdd:");
        for (int i = 0; i < updateOutput.getSize(); i++) {
            System.out.print(updateOutput.get(i) + " ");
        }
        System.out.println();




        try (TornadoExecutionPlan executionPlan = new TornadoExecutionPlan(immutableTaskGraph1)) {
            TornadoDevice device = TornadoExecutionPlan.getDevice(0, 0);

            executionPlan.withDevice(device).withPreCompilation();

            for (int i = 0; i < 100; i++) {
                executionPlan.execute();
            }

            ArrayList<Long> kernelTimers = new ArrayList<>();
            ArrayList<Long> totalTimers = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                TornadoExecutionResult executionResult = executionPlan.execute();
                kernelTimers.add(executionResult.getProfilerResult().getDeviceKernelTime());
                totalTimers.add(executionResult.getProfilerResult().getTotalTime());
            }

            long[] kernelTimersLong = kernelTimers.stream().mapToLong(Long::longValue).toArray();
            long[] totalTimersLong = totalTimers.stream().mapToLong(Long::longValue).toArray();
            // System.out.println("Stats KernelTime");
            // Utils.computeStatistics(kernelTimersLong);
            // System.out.println("Stats TotalTime");
            // Utils.computeStatistics(totalTimersLong);
        } catch (TornadoExecutionPlanException e) {
            e.printStackTrace();
        }
    }
}
