# 🚀 QuickJS Android App

A standalone Android application showcasing QuickJS JavaScript engine integration with modern Android development practices. This app demonstrates how to embed a lightweight, ES2023-compatible JavaScript engine directly into Android applications.

## ✨ Features

### 🎯 Core Functionality
- **QuickJS Integration**: Lightweight JavaScript engine with ES2023 support
- **Native JNI Bridge**: Seamless integration between Kotlin and C++
- **HTTP Polyfills**: Built-in `fetch()` and `XMLHttpRequest` support
- **Modern UI**: Clean Jetpack Compose interface

### 🚀 JavaScript Capabilities
- **ES2023 Support**: Modern JavaScript features including async/await, classes, modules
- **Bytecode Compilation**: Pre-compile JavaScript for faster execution
- **Memory Management**: Configurable memory limits and garbage collection
- **Context Isolation**: Clean execution environment with reset capability

### 🛡️ Production Features
- **Error Handling**: Robust error management and logging
- **Performance Monitoring**: Execution time tracking and memory usage stats
- **HTTP Networking**: Real network requests from JavaScript
- **Execution History**: Track and review all JavaScript executions

## 📱 Screenshots

The app provides an intuitive interface with:
- **Execute Tab**: Write and run JavaScript code with example templates
- **History Tab**: Review previous executions with results and timing
- **Status Card**: Monitor engine status and memory usage
- **Real-time Results**: Immediate feedback with syntax highlighting

## 🏗️ Architecture

### Native Layer (C++)
- **QuickJS Engine**: Real QuickJS runtime with mobile optimizations
- **JNI Bridge**: Efficient communication between native and Kotlin code
- **HTTP Polyfills**: Native implementation of web APIs
- **Memory Management**: 64MB limit with 1MB GC threshold

### Kotlin Layer
- **QuickJSBridge**: Main interface for JavaScript execution
- **HttpService**: OkHttp-based networking for JavaScript polyfills
- **MainActivity**: Jetpack Compose UI with modern Material Design

### UI Layer (Compose)
- **Modern Design**: Material 3 with dynamic theming
- **Responsive Layout**: Optimized for various screen sizes
- **Interactive Examples**: Pre-built JavaScript examples
- **Real-time Feedback**: Live execution results and error handling

## 🚀 Quick Start

### Prerequisites
- Android Studio with NDK support
- Android device or emulator (API 24+)
- Git for cloning the repository

### Build and Run
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd QuickJSAndroidApp
   ```

2. **Build the app**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

4. **Run tests**
   ```bash
   ./gradlew test
   ```

## 🧪 JavaScript Examples

### Basic Execution
```javascript
// Basic JavaScript execution
const greeting = "Hello from QuickJS!";
const numbers = [1, 2, 3, 4, 5];
const sum = numbers.reduce((a, b) => a + b, 0);
console.log(greeting);
return `${greeting} Sum: ${sum}`;
```

### Async Operations
```javascript
// Async/await example
async function delay(ms) {
    return new Promise(resolve => {
        setTimeout(resolve, ms);
    });
}

async function asyncExample() {
    console.log("Starting async operation...");
    await delay(100);
    return "Async completed: " + new Date().toISOString();
}

asyncExample();
```

### HTTP Requests
```javascript
// HTTP request using fetch API
async function fetchExample() {
    try {
        const response = await fetch('https://jsonplaceholder.typicode.com/posts/1');
        const data = await response.json();
        return `Post title: ${data.title}`;
    } catch (error) {
        return `Error: ${error.message}`;
    }
}

fetchExample();
```

### Complex Operations
```javascript
// Modern JavaScript features
class Calculator {
    constructor(name) {
        this.name = name;
        this.history = [];
    }
    
    add(a, b) {
        const result = a + b;
        this.history.push(`${a} + ${b} = ${result}`);
        return result;
    }
}

const calc = new Calculator("QuickJS Calculator");
const result = calc.add(15, 25);
return `Calculator: ${calc.name}, Result: ${result}`;
```

## 📊 Performance

### Engine Characteristics
- **Startup Time**: < 300 microseconds
- **Memory Footprint**: ~367 KiB for basic operations
- **Execution Speed**: Optimized for mobile devices
- **ES2023 Compliance**: Full modern JavaScript support

### Optimization Features
- **Bytecode Compilation**: Pre-compile scripts for faster execution
- **Memory Limits**: Configurable limits prevent memory issues
- **Garbage Collection**: Deterministic GC with mobile optimization
- **Context Reset**: Clean slate for new executions

## 🛠️ Configuration

### Memory Settings
```cpp
// Native memory limits (in quickjs_integration.cpp)
JS_SetMemoryLimit(runtime, 64 * 1024 * 1024); // 64MB limit
JS_SetGCThreshold(runtime, 1024 * 1024);       // 1MB GC threshold
```

### HTTP Settings
```kotlin
// HTTP client configuration (in HttpService.kt)
private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
```

## 🧪 Testing

### Unit Tests
```bash
# Run unit tests
./gradlew test

# Run with coverage
./gradlew testDebugUnitTest
```

### Integration Tests
```bash
# Run on connected device
./gradlew connectedAndroidTest
```

### Test Coverage
- **QuickJSBridge**: Core functionality and error handling
- **HttpService**: Network operations and error scenarios
- **UI Components**: User interface and interaction testing

## 📚 API Reference

### QuickJSBridge
```kotlin
class QuickJSBridge(context: Context) {
    fun initialize(): Boolean
    suspend fun execute(script: String): ExecutionResult
    suspend fun compile(script: String): ByteArray?
    suspend fun executeBytecode(bytecode: ByteArray): ExecutionResult
    fun reset(): Boolean
    fun getMemoryStats(): String
    fun cleanup()
}
```

### ExecutionResult
```kotlin
data class ExecutionResult(
    val script: String,
    val timestamp: Long,
    val success: Boolean,
    val result: String,
    val executionTimeMs: Long
)
```

### HttpService
```kotlin
class HttpService {
    fun makeRequest(url: String, method: String, headers: Map<String, String>, body: String?): Response
    fun get(url: String, headers: Map<String, String>): String
    fun post(url: String, body: String, headers: Map<String, String>): String
    fun isReachable(url: String): Boolean
}
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run tests: `./gradlew test`
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **QuickJS**: Created by Fabrice Bellard - https://bellard.org/quickjs/
- **Android NDK**: For native development support
- **OkHttp**: For HTTP networking capabilities
- **Jetpack Compose**: For modern Android UI development

## 🔗 Related Projects

- [Original V8EngineAndroidApp](../V8EngineAndroidApp) - Full-featured app with caching system
- [QuickJS Official](https://bellard.org/quickjs/) - Official QuickJS documentation
- [Android NDK Guide](https://developer.android.com/ndk) - Native development resources

---

**Built with ❤️ for demonstrating JavaScript engine integration on Android**
