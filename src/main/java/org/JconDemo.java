package org.example;

import uk.ac.manchester.tornado.api.GridScheduler;
import uk.ac.manchester.tornado.api.ImmutableTaskGraph;
import uk.ac.manchester.tornado.api.KernelContext;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.WorkerGrid1D;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;
import uk.ac.manchester.tornado.api.exceptions.TornadoExecutionPlanException;
import uk.ac.manchester.tornado.api.types.arrays.FloatArray;

public class JconDemo {

    // Approach 1 : paralle api
    public static void vectorAdd(FloatArray a, FloatArray b, FloatArray c) {
        for (@Parallel int i = 0; i < a.getSize(); i++) {
            c.set(i, a.get(i) + b.get(i));
        }
    }

    // Aproach 2: kernelcontext
    public static void vectorAddKernelContext(KernelContext context, FloatArray a, FloatArray b, FloatArray c) {
        int globalIndex = context.globalIdx;
        c.set(globalIndex, a.get(globalIndex) + b.get(globalIndex));
    }

    public static void main(String[] args) {

        int size = 256;
        FloatArray a = new FloatArray(size);
        FloatArray b = new FloatArray(size);
        FloatArray c = new FloatArray(size);

        a.init(1);
        b.init(2);
        c.init(0);

        TaskGraph tg = new TaskGraph("s0");

        // define -> I/O + compute

        tg.transferToDevice(DataTransferMode.FIRST_EXECUTION, a, b);
        tg.task("t0", JconDemo::vectorAdd, a, b, c);
        tg.transferToHost(DataTransferMode.EVERY_EXECUTION, c);

        ImmutableTaskGraph immutableTaskGraph = tg.snapshot();
        TornadoExecutionPlan executor = new TornadoExecutionPlan(immutableTaskGraph);
        executor.withPrintKernel().withThreadInfo().execute();

        for (int i = 0; i < 10; i++) {
            System.out.println(c.get(i));
        }

        FloatArray kcC = new FloatArray(size);
        KernelContext kc = new KernelContext();
        TaskGraph tg2 = new TaskGraph("s1");

        tg2.transferToDevice(DataTransferMode.FIRST_EXECUTION, a, b);
        tg2.task("t1", JconDemo::vectorAddKernelContext, kc, a, b, kcC);
        tg2.transferToHost(DataTransferMode.EVERY_EXECUTION, kcC);

        GridScheduler scheduler = new GridScheduler();

        WorkerGrid1D workerGrid = new WorkerGrid1D(size);
        workerGrid.setGlobalWork(size, 1, 1);
        workerGrid.setLocalWork(32, 1, 1);

        scheduler.addWorkerGrid("s1.t1", workerGrid);

        ImmutableTaskGraph immutableTaskGraph2 = tg2.snapshot();
        TornadoExecutionPlan executor2 = new TornadoExecutionPlan(immutableTaskGraph2);
        executor2.withPrintKernel().withGridScheduler(scheduler).withThreadInfo().execute();

        for (int i = 0; i < 10; i++) {
            System.out.println(kcC.get(i));
        }

        //ChatGPT said:
        //
        //The try-with-resources statement
        // in Java is useful because it helps you automatically manage resources — things like files, streams, sockets, or database connections — that need to be closed after use.
        // the tornaodvm exectuion plan class implements Autoclosanble

        try (TornadoExecutionPlan executor3 = new TornadoExecutionPlan(immutableTaskGraph2)) {
            executor3.withPrintKernel().withGridScheduler(scheduler).withThreadInfo().execute();
        } catch (TornadoExecutionPlanException e) {
            e.printStackTrace();
        }


    }
}

