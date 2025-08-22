# QuickJS Integration for Android

This directory contains the QuickJS JavaScript engine integration for the V8 Engine Android App.

## About QuickJS

QuickJS is a small and embeddable JavaScript engine created by Fabrice Bellard. It provides:

- **ES2023 specification support** including modules, async/await, and proxies
- **Ultra-lightweight footprint** (367 KiB for hello world)
- **Fast startup time** (<300 microseconds)
- **Low memory consumption**
- **Deterministic garbage collection**

## Current Implementation

The current implementation uses a **mock QuickJS engine** that demonstrates the integration pattern.
To use the real QuickJS engine, you'll need to:

## How to Integrate Real QuickJS

### Step 1: Download QuickJS Source

```bash
cd app/src/main/cpp/quickjs
curl -L https://bellard.org/quickjs/quickjs-2025-04-26.tar.xz -o quickjs-latest.tar.xz
tar -xf quickjs-latest.tar.xz
mv quickjs-*/* .
```

### Step 2: Build QuickJS for Android

Create a `build_quickjs.sh` script:

```bash
#!/bin/bash
# Build QuickJS for Android NDK

export ANDROID_NDK_ROOT=${ANDROID_NDK_ROOT:-$HOME/Android/Sdk/ndk/26.1.10909125}
export API_LEVEL=24

# Build for different architectures
for ARCH in arm64-v8a armeabi-v7a x86_64; do
    echo "Building for $ARCH..."
    
    case $ARCH in
        arm64-v8a)
            TARGET=aarch64-linux-android
            ;;
        armeabi-v7a)
            TARGET=armv7a-linux-androideabi
            ;;
        x86_64)
            TARGET=x86_64-linux-android
            ;;
    esac
    
    export CC=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/darwin-x86_64/bin/${TARGET}${API_LEVEL}-clang
    export AR=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/darwin-x86_64/bin/llvm-ar
    
    make clean
    make libquickjs.a
    
    mkdir -p lib/$ARCH
    cp libquickjs.a lib/$ARCH/
done
```

### Step 3: Update CMakeLists.txt

```cmake
# Add QuickJS configuration
set(QUICKJS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/quickjs)
set(QUICKJS_INCLUDE_DIR ${QUICKJS_DIR})
set(QUICKJS_LIB_DIR ${QUICKJS_DIR}/lib/${ANDROID_ABI})

# Include QuickJS headers
include_directories(${QUICKJS_INCLUDE_DIR})

# Link QuickJS library
target_link_libraries(${CMAKE_PROJECT_NAME}
    ${QUICKJS_LIB_DIR}/libquickjs.a
    # ... other libraries
)
```

### Step 4: Replace Mock Implementation

Update `quickjs_integration.cpp` to use real QuickJS APIs:

```cpp
#include "quickjs.h"

class RealQuickJSEngine {
private:
    JSRuntime* runtime;
    JSContext* context;
    
public:
    bool initialize() {
        runtime = JS_NewRuntime();
        if (!runtime) return false;
        
        context = JS_NewContext(runtime);
        return context != nullptr;
    }
    
    std::string executeScript(const std::string& script) {
        JSValue result = JS_Eval(context, script.c_str(), script.length(), 
                                "<input>", JS_EVAL_TYPE_GLOBAL);
        
        if (JS_IsException(result)) {
            // Handle exception
            return "Error: JavaScript execution failed";
        }
        
        const char* str = JS_ToCString(context, result);
        std::string resultStr(str);
        JS_FreeCString(context, str);
        JS_FreeValue(context, result);
        
        return resultStr;
    }
    
    void cleanup() {
        if (context) {
            JS_FreeContext(context);
            context = nullptr;
        }
        if (runtime) {
            JS_FreeRuntime(runtime);
            runtime = nullptr;
        }
    }
};
```

## Features Supported

The QuickJS integration provides:

- ✅ **JavaScript Execution**: Run ES2023 compliant JavaScript code
- ✅ **ByteTransfer Integration**: Seamless data exchange with the byte transfer system
- ✅ **Memory Management**: Efficient memory usage with deterministic GC
- ✅ **Module Support**: ES6 modules and modern JavaScript features
- ✅ **Performance Testing**: Comprehensive test suites
- ✅ **Error Handling**: Robust error management and logging

## Performance Characteristics

| Metric | QuickJS | V8 |
|--------|---------|-----|
| Binary Size | 367 KiB | ~30 MB |
| Startup Time | <300μs | Medium |
| Memory Usage | Low | Higher |
| ES Support | ES2023 | Latest |
| Use Case | Embedded/Lightweight | High-performance |

## API Usage

```kotlin
// Initialize QuickJS
val quickJSBridge = QuickJSBridge()
val success = quickJSBridge.initialize()

// Execute JavaScript
val result = quickJSBridge.runJavaScript("const x = 42; x * 2")

// Run test suite
val testResults = quickJSBridge.runTestSuite()

// Cleanup
quickJSBridge.cleanup()
```

## License

QuickJS is released under the MIT license.