# 🚀 QuickJS Android App - Quick Start Guide

## Overview
This is a standalone Android application extracted from the original V8EngineAndroidApp project, focusing specifically on QuickJS JavaScript engine integration. The app provides a clean, modern interface for executing JavaScript code directly on Android devices.

## What's Included

### ✅ Core Components
- **QuickJS Engine**: Real QuickJS JavaScript engine with ES2023 support
- **JNI Bridge**: Native C++ to Kotlin integration
- **HTTP Polyfills**: Built-in fetch() and XMLHttpRequest support
- **Modern UI**: Jetpack Compose with Material 3 design
- **Test Suite**: Comprehensive unit tests for all components

### ✅ Key Features
- Execute JavaScript code with real-time results
- Pre-built example scripts (Basic, Async, HTTP, Complex)
- Memory usage monitoring and statistics
- Execution history tracking
- Bytecode compilation support
- Context reset functionality

## Build Instructions

### 1. Prerequisites
```bash
# Ensure you have:
# - Android Studio with NDK support
# - Android SDK with API 24+ 
# - Git for version control
```

### 2. Build the Project
```bash
cd /Users/visgupta/devel/git/QuickJSAndroidApp

# Build debug version
./gradlew assembleDebug

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

### 3. Project Structure
```
QuickJSAndroidApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/quickjs/android/
│   │   │   ├── QuickJSBridge.kt        # Main JavaScript engine interface
│   │   │   ├── HttpService.kt          # Network service for HTTP polyfills
│   │   │   ├── MainActivity.kt         # Main UI with Jetpack Compose
│   │   │   └── ui/theme/               # Material 3 theming
│   │   ├── cpp/
│   │   │   ├── quickjs_integration.cpp # Native QuickJS integration
│   │   │   ├── CMakeLists.txt          # Native build configuration
│   │   │   └── quickjs/                # QuickJS engine source files
│   │   ├── res/                        # Android resources
│   │   └── AndroidManifest.xml         # App configuration
│   └── src/test/
│       └── java/com/quickjs/android/   # Unit tests
├── gradle/                             # Gradle wrapper
├── build.gradle.kts                    # Root build configuration
├── settings.gradle.kts                 # Project settings
└── README.md                           # Full documentation
```

## Key Differences from Original Project

### ✅ Simplified Focus
- **Removed**: Enterprise caching system, V8 engine, byte transfer system
- **Kept**: QuickJS engine, HTTP polyfills, modern UI, comprehensive testing
- **Added**: Cleaner architecture, focused documentation, streamlined build

### ✅ Improved Architecture
- Standalone QuickJS implementation without dependencies on V8 or caching
- Simplified package structure (`com.quickjs.android` vs `com.visgupta.example.v8integrationandroidapp`)
- Focused test suite with only relevant functionality
- Clean separation of concerns

### ✅ Modern Development Practices
- Latest Gradle and Android build tools
- Material 3 design system
- Jetpack Compose UI
- Comprehensive documentation
- MIT license for open source use

## Usage Examples

### Basic JavaScript Execution
```javascript
const greeting = "Hello from QuickJS!";
const numbers = [1, 2, 3, 4, 5];
const sum = numbers.reduce((a, b) => a + b, 0);
return `${greeting} Sum: ${sum}`;
```

### HTTP Requests
```javascript
async function fetchData() {
    const response = await fetch('https://jsonplaceholder.typicode.com/posts/1');
    const data = await response.json();
    return `Post title: ${data.title}`;
}
fetchData();
```

### Modern JavaScript Features
```javascript
class Calculator {
    constructor(name) {
        this.name = name;
    }
    
    add(a, b) {
        return a + b;
    }
}

const calc = new Calculator("QuickJS Calculator");
return `Result: ${calc.add(15, 25)}`;
```

## Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run on Device
```bash
./gradlew connectedAndroidTest
```

## Next Steps

1. **Customize**: Modify the UI or add new JavaScript APIs
2. **Extend**: Add more polyfills or native functions
3. **Deploy**: Build release version and distribute
4. **Contribute**: Submit improvements back to the community

## Support

- Check the full [README.md](README.md) for detailed documentation
- Review the [LICENSE](LICENSE) for usage terms
- Explore the test files for usage examples

---

**Ready to run JavaScript on Android with QuickJS! 🚀**
