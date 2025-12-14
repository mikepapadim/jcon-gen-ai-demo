# 🚀 Your First Java Program on GPU in 5 Minutes

**Run Java code on OpenCL GPUs with zero code changes!** This demo shows how TornadoVM makes GPU programming as simple as writing regular Java.

## 🎯 What You'll Learn

- Run a Java program on your GPU in literally 3 commands
- See parallel vector addition execute on OpenCL devices
- No CUDA knowledge required. No complex setup. Just Java.

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

```bash
# Build the project
mvn clean package -DskipTests

# Run on GPU!
java @tornado-argfile -cp target/jcon-demo-1.0-SNAPSHOT.jar org.example.JconBackup
```

**That's it!** Your Java code is now running on the GPU.

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

## 📊 What You'll See

```
Welcome to TornadoVM
Output after first vectorAdd:
30.0 30.0 30.0 30.0 30.0 ... (100 elements)

Output after kernelContext vectorAdd:
50.0 50.0 50.0 50.0 50.0 ... (100 elements)
```

## 🧠 Two Programming Models

This demo showcases **both** TornadoVM programming approaches:

### 1. **@Parallel Annotation** (Easiest)
Just add `@Parallel` to your loops. TornadoVM handles the rest.

### 2. **KernelContext API** (More Control)
Write explicit GPU kernels with fine-grained control over grid/thread configuration:

```java
public static void vectorAddKernel(KernelContext ctx, FloatArray a, FloatArray b, FloatArray c) {
    int i = ctx.globalIdx;  // Like CUDA threadIdx
    c.set(i, a.get(i) + b.get(i));
}
```

## 🎓 What's Inside JconBackup.java?

The demo includes:
- ✅ Parallel vector addition with `@Parallel`
- ✅ Kernel-based implementation with `KernelContext`
- ✅ Grid scheduling configuration
- ✅ Performance profiling setup
- ✅ Device selection and execution plans

Check out [`src/main/java/org/JconBackup.java`](src/main/java/org/JconBackup.java) to see the full implementation.

## 🛠️ Requirements

- **Java 21+** with preview features enabled
- **OpenCL-compatible device** (Intel/AMD/NVIDIA GPU, or CPU)
- **Maven 3.6+**

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
