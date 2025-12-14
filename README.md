# 🚀 Your First Java Program on GPU in 5 Minutes

**Run Java code on OpenCL GPUs with zero code changes!** This demo shows how TornadoVM makes GPU programming as simple as writing regular Java.

**Just ONE file.** That's all you need to see. `JconBackup.java` - a single, self-contained example that runs on your GPU.

```
jcon-gen-ai-demo/
├── JconBackup.java    ← Your GPU program (that's it!)
├── pom.xml            ← Dependencies
└── README.md          ← You are here
```

## 🎯 What You'll Learn

- Run a Java program on your GPU in literally 3 commands
- See parallel vector addition execute on OpenCL devices
- No CUDA knowledge required. No complex setup. Just Java.
- Everything in one file - no complex project structure

## ⚡ Quick Start (Choose Your Platform)

### Linux (x86_64)

```bash
# 1. Download TornadoVM
wget https://github.com/beehive-lab/TornadoVM/releases/download/v2.0.0/tornadovm-2.0.0-opencl-linux-amd64.zip
unzip tornadovm-2.0.0-opencl-linux-amd64.zip

# 2. Set environment
export TORNADO_SDK="$(pwd)/tornadovm-2.0.0-opencl"
export PATH=$TORNADO_SDK/bin:$PATH

# 3. Verify installation
tornado --devices
tornado --version
```

### macOS (Apple Silicon)

```bash
# 1. Download TornadoVM
wget https://github.com/beehive-lab/TornadoVM/releases/download/v2.0.0/tornadovm-2.0.0-opencl-mac-aarch64.zip
unzip tornadovm-2.0.0-opencl-mac-aarch64.zip

# 2. Set environment
export TORNADO_SDK="$(pwd)/tornadovm-2.0.0-opencl"
export PATH=$TORNADO_SDK/bin:$PATH

# 3. Verify installation
tornado --devices
tornado --version
```

### Windows (10+)

```powershell
# 1. Download TornadoVM
curl -L -o tornadovm-2.0.0-opencl-windows-amd64.zip https://github.com/beehive-lab/TornadoVM/releases/download/v2.0.0/tornadovm-2.0.0-opencl-windows-amd64.zip
tar -xf tornadovm-2.0.0-opencl-windows-amd64.zip

# 2. Set environment
set TORNADO_SDK=%cd%\tornadovm-2.0.0-opencl
set PATH=%TORNADO_SDK%\bin;%PATH%

# 3. Verify installation
tornado --devices
tornado --version
```

## 🎮 Run Your First GPU Program

### Option 1: Use TornadoVM SDK configuration (Recommended)

```bash
# Build the single Java file
mvn clean package -DskipTests

# Run on GPU using TornadoVM's configuration!
java @$TORNADO_SDK/bin/tornado-argfile -cp target/jcon-demo-1.0-SNAPSHOT.jar JconBackup
```

### Option 2: Use local configuration file

```bash
# Copy tornado-argfile to project directory (one-time setup)
cp $TORNADO_SDK/bin/tornado-argfile .

# Build
mvn clean package -DskipTests

# Run on GPU!
java @tornado-argfile -cp target/jcon-demo-1.0-SNAPSHOT.jar JconBackup
```

**That's it!** Your Java code is now running on the GPU.

> **Note**: The `tornado-argfile` contains all the JVM options, module exports, and native library paths needed to run TornadoVM programs. Using the one from `$TORNADO_SDK/bin/` ensures you have the correct configuration for your TornadoVM installation.

## 🔥 What Just Happened?

The demo runs a simple **vector addition** on your GPU. Here's the magic:

```java
public static void vectorAdd(FloatArray a, FloatArray b, FloatArray c) {
    // @Parallel tells TornadoVM this loop runs on GPU
    for (@Parallel int i = 0; i < a.getSize(); i++) {
        c.set(i, a.get(i) + b.get(i));
    }
}
```

TornadoVM sees the `@Parallel` annotation and automatically:
1. Compiles your Java code to OpenCL
2. Transfers data to GPU memory
3. Executes in parallel across GPU cores
4. Brings results back to Java

**No kernel code. No memory management. Just Java.**

### 🧩 Key Concepts Explained

**TaskGraph** - Your GPU workflow container:
```java
TaskGraph tg = new TaskGraph("s0")
    .transferToDevice(DataTransferMode.EVERY_EXECUTION, a, b)  // Send data to GPU
    .task("t0", JconBackup::vectorAdd, a, b, c)                // Define computation
    .transferToHost(DataTransferMode.EVERY_EXECUTION, c);      // Get results back
```
Think of it as a recipe: what data to move, what computation to run, what results to retrieve.

**ExecutionPlan** - Executes your TaskGraph:
```java
ImmutableTaskGraph immutable = tg.snapshot();              // Freeze the graph
TornadoExecutionPlan plan = new TornadoExecutionPlan(immutable);
plan.execute();                                            // Run on GPU!
```
Takes your TaskGraph and runs it. Can run multiple times, select specific devices, enable profiling.

**KernelContext** - For explicit GPU programming:
```java
public static void vectorAddKernel(KernelContext ctx, FloatArray a, FloatArray b, FloatArray c) {
    int i = ctx.globalIdx;  // Which thread am I? (like CUDA's threadIdx)
    c.set(i, a.get(i) + b.get(i));
}
```
Gives you direct control over GPU threads. Use when you need fine-grained control over parallelization.

**@Parallel** - The easiest way:
```java
for (@Parallel int i = 0; i < size; i++) {
    // TornadoVM parallelizes this automatically
}
```
Just annotate your loop. TornadoVM handles thread mapping, work distribution, everything.

## 📊 What You'll See

```
Welcome to TornadoVM
Output after first vectorAdd:
30.0 30.0 30.0 30.0 30.0 ... (100 elements)

Output after kernelContext vectorAdd:
50.0 50.0 50.0 50.0 50.0 ... (100 elements)
```

## 🧠 Two Programming Models

This demo showcases **both** approaches in one file:

1. **@Parallel Annotation** (Lines 26-31) - Add `@Parallel` to loops, TornadoVM handles everything
2. **KernelContext API** (Lines 36-42) - Explicit control over GPU threads and grid configuration

Both produce the same result, but KernelContext gives you fine-grained control over thread scheduling, similar to writing CUDA kernels.

## 🎓 What's Inside JconBackup.java?

**Everything you need in ONE file!** Just open [`JconBackup.java`](JconBackup.java) in the root directory.

The demo includes:
- ✅ Parallel vector addition with `@Parallel` (Lines 26-31)
- ✅ Kernel-based implementation with `KernelContext` (Lines 36-42)
- ✅ Grid scheduling configuration (Lines 101-108)
- ✅ Performance profiling setup (Lines 138-163)
- ✅ Device selection and execution plans (Lines 139-141)

No digging through folders - it's all right there!

## 🛠️ Requirements

- **Java 21+** with preview features enabled
- **OpenCL-compatible device** (Intel/AMD/NVIDIA GPU, or CPU)
- **Maven 3.6+**

## 🔧 Troubleshooting

### Error: Can't find tornado-argfile?

Make sure you've set the `TORNADO_SDK` environment variable:
```bash
echo $TORNADO_SDK  # Should print the path to your TornadoVM installation
```

If it's empty, go back to the Quick Start section and set up TornadoVM first.

### Error: UnsatisfiedLinkError or module not found?

This usually means the paths in tornado-argfile don't match your TornadoVM installation. Always use the tornado-argfile from your `$TORNADO_SDK/bin/` directory, as it has the correct paths for your installation.

### Want to see what devices are available?

```bash
tornado --devices
```

This will list all OpenCL-compatible devices on your system (GPUs, CPUs, etc.)

## 📚 Next Steps

Want to do more?

1. **Try different devices**: `tornado --devices` lists all available accelerators
2. **Add profiling**: Uncomment the performance stats at the bottom of JconBackup.java
3. **Write your own kernel**: Modify the vector addition to do multiplication, matrix ops, etc.
4. **Learn more**: Visit [TornadoVM Documentation](https://tornadovm.readthedocs.io/)

## 🤝 Why TornadoVM?

- **Zero Learning Curve**: If you know Java, you know TornadoVM
- **Performance**: Near-native GPU performance from pure Java
- **Portability**: Same code runs on NVIDIA, AMD, Intel GPUs and CPUs
- **Safety**: Type-safe parallel programming, no segfaults

## 📖 Resources

- [TornadoVM GitHub](https://github.com/beehive-lab/TornadoVM)
- [TornadoVM Documentation](https://tornadovm.readthedocs.io/)
- [API Reference](https://tornadovm.readthedocs.io/en/latest/api.html)

---

**Built with ❤️ using [TornadoVM](https://github.com/beehive-lab/TornadoVM) - Making GPU programming accessible to Java developers**
