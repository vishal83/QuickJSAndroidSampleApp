#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <sstream>
#include <map>
#include <regex>
#include <algorithm>
#include <cmath>

#define LOG_TAG "QuickJS"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Include real QuickJS headers
extern "C" {
#include "quickjs/quickjs.h"
#include "quickjs/quickjs-libc.h"
}

// Global JavaVM reference for JNI calls from native threads
JavaVM *g_jvm = nullptr;

// Global references for HTTP polyfills
static jobject g_quickjsBridgeInstance = nullptr;
static jmethodID g_handleHttpRequestMethod = nullptr;

// Forward declarations
static JSValue js_http_request(JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv);
void initializeHttpPolyfill(JNIEnv *env, jobject bridgeInstance);
void addConsoleSupport(JSContext *ctx);
void addTimerPolyfills(JSContext *ctx);
void addHttpPolyfills(JSContext *ctx);

// Native HTTP request function (called from JavaScript)
static JSValue js_http_request(JSContext *ctx, JSValueConst this_val, int argc, JSValueConst *argv) {
    if (argc < 1 || !g_quickjsBridgeInstance || !g_handleHttpRequestMethod) {
        return JS_ThrowReferenceError(ctx, "HTTP service not available");
    }
    
    // Get URL from first argument
    const char *url = JS_ToCString(ctx, argv[0]);
    if (!url) {
        return JS_ThrowTypeError(ctx, "URL must be a string");
    }
    
    // Get options from second argument (or empty object)
    const char *options = "{}";
    if (argc > 1) {
        options = JS_ToCString(ctx, argv[1]);
        if (!options) {
            JS_FreeCString(ctx, url);
            return JS_ThrowTypeError(ctx, "Options must be an object");
        }
    }
    
    // Get JNI environment
    JNIEnv *env = nullptr;
    
    // This is a simplified approach - in production you'd want proper JVM attachment
    if (!g_jvm || g_jvm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        JS_FreeCString(ctx, url);
        if (argc > 1) JS_FreeCString(ctx, options);
        return JS_ThrowInternalError(ctx, "Failed to get JNI environment");
    }
    
    // Call Java method
    jstring jUrl = env->NewStringUTF(url);
    jstring jOptions = env->NewStringUTF(options);
    
    jstring jResult = (jstring)env->CallObjectMethod(g_quickjsBridgeInstance, 
        g_handleHttpRequestMethod, jUrl, jOptions);
    
    env->DeleteLocalRef(jUrl);
    env->DeleteLocalRef(jOptions);
    
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        JS_FreeCString(ctx, url);
        if (argc > 1) JS_FreeCString(ctx, options);
        return JS_ThrowInternalError(ctx, "HTTP request failed");
    }
    
    // Convert result back to JavaScript
    const char *resultStr = env->GetStringUTFChars(jResult, nullptr);
    JSValue result = JS_ParseJSON(ctx, resultStr, strlen(resultStr), "<http-response>");
    
    env->ReleaseStringUTFChars(jResult, resultStr);
    env->DeleteLocalRef(jResult);
    
    JS_FreeCString(ctx, url);
    if (argc > 1) JS_FreeCString(ctx, options);
    
    return result;
}

// Initialize HTTP polyfill references
void initializeHttpPolyfill(JNIEnv *env, jobject bridgeInstance) {
    if (g_quickjsBridgeInstance) {
        env->DeleteGlobalRef(g_quickjsBridgeInstance);
    }
    g_quickjsBridgeInstance = env->NewGlobalRef(bridgeInstance);
    
    jclass bridgeClass = env->GetObjectClass(bridgeInstance);
    g_handleHttpRequestMethod = env->GetMethodID(bridgeClass, "handleHttpRequest", 
        "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    
    if (!g_handleHttpRequestMethod) {
        LOGE("Failed to find handleHttpRequest method");
    }
}

// Add console support to QuickJS context
void addConsoleSupport(JSContext *ctx) {
    // Add console.log function
    const char *consolePolyfill = R"(
(function() {
    globalThis.console = {
        log: function(...args) {
            var message = args.map(arg => {
                if (typeof arg === 'object') {
                    try {
                        return JSON.stringify(arg, null, 2);
                    } catch (e) {
                        return '[object Object]';
                    }
                } else {
                    return String(arg);
                }
            }).join(' ');
            
            // In a real implementation, this would call a native function
            // For now, we'll just return the message
            return message;
        },
        error: function(...args) {
            return this.log('ERROR:', ...args);
        },
        warn: function(...args) {
            return this.log('WARN:', ...args);
        },
        info: function(...args) {
            return this.log('INFO:', ...args);
        }
    };
})();
)";
    
    JSValue result = JS_Eval(ctx, consolePolyfill, strlen(consolePolyfill), "<console-polyfill>", JS_EVAL_TYPE_GLOBAL);
    if (JS_IsException(result)) {
        JSValue exception = JS_GetException(ctx);
        const char *exceptionStr = JS_ToCString(ctx, exception);
        LOGE("Failed to add console support: %s", exceptionStr ? exceptionStr : "Unknown error");
        if (exceptionStr) JS_FreeCString(ctx, exceptionStr);
        JS_FreeValue(ctx, exception);
    }
    JS_FreeValue(ctx, result);
}

// Add timer polyfills to QuickJS context
void addTimerPolyfills(JSContext *ctx) {
    // Add setTimeout/setInterval polyfills - simple immediate execution for demo
    const char *timerPolyfill = R"(
(function() {
    // Simple setTimeout that executes immediately for demo purposes
    // In a real implementation, this would use native threading/timers
    globalThis.setTimeout = function(callback, delay) {
        // For demo purposes, execute the callback immediately
        // This allows promises with setTimeout to resolve
        try {
            callback();
        } catch (e) {
            throw e;
        }
        return 1; // return a dummy timer ID
    };
    
    globalThis.clearTimeout = function(id) {
        // No-op for demo
    };
    
    globalThis.setInterval = function(callback, delay) {
        return setTimeout(callback, delay);
    };
    
    globalThis.clearInterval = function(id) {
        clearTimeout(id);
    };
})();
)";
    
    JSValue result = JS_Eval(ctx, timerPolyfill, strlen(timerPolyfill), "<timer-polyfill>", JS_EVAL_TYPE_GLOBAL);
    if (JS_IsException(result)) {
        JSValue exception = JS_GetException(ctx);
        const char *exceptionStr = JS_ToCString(ctx, exception);
        LOGE("Failed to add timer support: %s", exceptionStr ? exceptionStr : "Unknown error");
        if (exceptionStr) JS_FreeCString(ctx, exceptionStr);
        JS_FreeValue(ctx, exception);
    }
    JS_FreeValue(ctx, result);
}

// Add HTTP polyfills to QuickJS context
void addHttpPolyfills(JSContext *ctx) {
    // Add native HTTP request function
    JSValue global = JS_GetGlobalObject(ctx);
    JS_SetPropertyStr(ctx, global, "_nativeHttpRequest", 
        JS_NewCFunction(ctx, js_http_request, "_nativeHttpRequest", 2));
    
    // Add fetch polyfill
    const char *fetchPolyfill = R"(
(function() {
    // Fetch API polyfill
    globalThis.fetch = function(url, options) {
        options = options || {};
        
        return new Promise(function(resolve, reject) {
            try {
                var requestOptions = {
                    method: options.method || 'GET',
                    headers: options.headers || {},
                    body: options.body || null,
                    timeout: options.timeout || 30000,
                    redirect: options.redirect || 'follow',
                    credentials: options.credentials || 'same-origin'
                };
                
                var response = _nativeHttpRequest(url, JSON.stringify(requestOptions));
                
                if (response && response.status !== undefined) {
                    // Create Response object
                    var responseObj = {
                        status: response.status,
                        statusText: response.statusText,
                        ok: response.ok,
                        redirected: response.redirected,
                        url: response.url,
                        type: response.type,
                        headers: new Map(Object.entries(response.headers || {})),
                        
                        text: function() {
                            return Promise.resolve(response.body || '');
                        },
                        
                        json: function() {
                            return Promise.resolve(JSON.parse(response.body || '{}'));
                        },
                        
                        blob: function() {
                            return Promise.reject(new Error('Blob not supported'));
                        },
                        
                        arrayBuffer: function() {
                            return Promise.reject(new Error('ArrayBuffer not supported'));
                        }
                    };
                    
                    resolve(responseObj);
                } else {
                    reject(new Error('Network request failed'));
                }
            } catch (e) {
                reject(e);
            }
        });
    };
    
    // XMLHttpRequest polyfill
    globalThis.XMLHttpRequest = function() {
        this.readyState = 0;
        this.status = 0;
        this.statusText = '';
        this.responseText = '';
        this.responseXML = null;
        this.onreadystatechange = null;
        this._method = 'GET';
        this._url = '';
        this._headers = {};
        this._body = null;
        
        this.open = function(method, url, async) {
            this._method = method;
            this._url = url;
            this.readyState = 1;
            if (this.onreadystatechange) this.onreadystatechange();
        };
        
        this.setRequestHeader = function(header, value) {
            this._headers[header] = value;
        };
        
        this.send = function(body) {
            var self = this;
            this._body = body;
            this.readyState = 2;
            if (this.onreadystatechange) this.onreadystatechange();
            
            try {
                var options = {
                    method: this._method,
                    headers: this._headers,
                    body: this._body
                };
                
                var response = _nativeHttpRequest(this._url, JSON.stringify(options));
                
                this.status = response.status || 0;
                this.statusText = response.statusText || '';
                this.responseText = response.body || '';
                this.readyState = 4;
                
                if (this.onreadystatechange) this.onreadystatechange();
            } catch (e) {
                this.status = 0;
                this.statusText = 'Error';
                this.responseText = '';
                this.readyState = 4;
                if (this.onreadystatechange) this.onreadystatechange();
            }
        };
        
        this.abort = function() {
            this.readyState = 0;
        };
        
        this.getAllResponseHeaders = function() {
            return '';
        };
        
        this.getResponseHeader = function(header) {
            return null;
        };
    };
    
    // Constants
    globalThis.XMLHttpRequest.UNSENT = 0;
    globalThis.XMLHttpRequest.OPENED = 1;
    globalThis.XMLHttpRequest.HEADERS_RECEIVED = 2;
    globalThis.XMLHttpRequest.LOADING = 3;
    globalThis.XMLHttpRequest.DONE = 4;
})();
)";
    
    JSValue result = JS_Eval(ctx, fetchPolyfill, strlen(fetchPolyfill), "<fetch-polyfill>", JS_EVAL_TYPE_GLOBAL);
    if (JS_IsException(result)) {
        JSValue exception = JS_GetException(ctx);
        const char *exceptionStr = JS_ToCString(ctx, exception);
        LOGE("Failed to add HTTP polyfills: %s", exceptionStr ? exceptionStr : "Unknown error");
        if (exceptionStr) JS_FreeCString(ctx, exceptionStr);
        JS_FreeValue(ctx, exception);
    }
    JS_FreeValue(ctx, result);
    JS_FreeValue(ctx, global);
}

// Real QuickJS Engine implementation
class QuickJSEngine {
public:
    JSRuntime *runtime;  // Made public for memory stats access
private:
    JSContext *context;
    bool initialized;
    
public:
    QuickJSEngine() : runtime(nullptr), context(nullptr), initialized(false) {
    }
    
    bool initialize() {
        LOGI("Initializing QuickJS Engine");

        runtime = JS_NewRuntime();
        if (!runtime) {
            LOGE("Failed to create QuickJS runtime");
            return false;
        }

        // Set memory limits for mobile environment
        JS_SetMemoryLimit(runtime, 64 * 1024 * 1024); // 64MB limit
        JS_SetGCThreshold(runtime, 1024 * 1024);       // 1MB GC threshold

        context = JS_NewContext(runtime);
        if (!context) {
            LOGE("Failed to create QuickJS context");
            JS_FreeRuntime(runtime);
            runtime = nullptr;
            return false;
        }

        js_std_add_helpers(context, 0, nullptr);
        addConsoleSupport(context);
        addTimerPolyfills(context);
        addHttpPolyfills(context);

        initialized = true;
        LOGI("QuickJS Engine initialized successfully with memory management and HTTP polyfills");
        return true;
    }
    
    std::string executeScript(const std::string& script) {
        if (!initialized || !context) {
            return "Error: QuickJS not initialized";
        }
        
        LOGI("Executing QuickJS script: %s", script.c_str());

        // Evaluate the JavaScript code
        JSValue result = JS_Eval(context, script.c_str(), script.length(),
                "<input>", JS_EVAL_TYPE_GLOBAL);

        if (JS_IsException(result)) {
            // Handle JavaScript exceptions
            JSValue exception = JS_GetException(context);
            std::string error = "JavaScript Error: ";
            
            // Try to get error message
            const char *exceptionStr = JS_ToCString(context, exception);
            if (exceptionStr) {
                error += exceptionStr;
                JS_FreeCString(context, exceptionStr);
            } else {
                // If direct conversion fails, try to get more details
                JSValue nameVal = JS_GetPropertyStr(context, exception, "name");
                JSValue messageVal = JS_GetPropertyStr(context, exception, "message");
                
                const char *name = JS_ToCString(context, nameVal);
                const char *message = JS_ToCString(context, messageVal);
                
                if (name && message) {
                    error += name;
                    error += ": ";
                    error += message;
                } else if (name) {
                    error += name;
                } else if (message) {
                    error += message;
                } else {
                    error += "Unknown error (exception object could not be converted to string)";
                }
                
                if (name) JS_FreeCString(context, name);
                if (message) JS_FreeCString(context, message);
                JS_FreeValue(context, nameVal);
                JS_FreeValue(context, messageVal);
            }
            
            JS_FreeValue(context, exception);
            JS_FreeValue(context, result);
            LOGE("JavaScript execution error: %s", error.c_str());
            return error;
        }

        result = js_std_await(context, result);
        
        // Check if awaiting resulted in an exception (promise rejection)
        if (JS_IsException(result)) {
            JSValue exception = JS_GetException(context);
            std::string error = "Promise Rejection: ";
            
            // Try to get error message
            const char *exceptionStr = JS_ToCString(context, exception);
            if (exceptionStr) {
                error += exceptionStr;
                JS_FreeCString(context, exceptionStr);
            } else {
                // If direct conversion fails, try to get more details
                JSValue nameVal = JS_GetPropertyStr(context, exception, "name");
                JSValue messageVal = JS_GetPropertyStr(context, exception, "message");
                
                const char *name = JS_ToCString(context, nameVal);
                const char *message = JS_ToCString(context, messageVal);
                
                if (name && message) {
                    error += name;
                    error += ": ";
                    error += message;
                } else if (name) {
                    error += name;
                } else if (message) {
                    error += message;
                } else {
                    error += "Unknown error (promise rejection could not be converted to string)";
                }
                
                if (name) JS_FreeCString(context, name);
                if (message) JS_FreeCString(context, message);
                JS_FreeValue(context, nameVal);
                JS_FreeValue(context, messageVal);
            }
            
            JS_FreeValue(context, exception);
            JS_FreeValue(context, result);
            LOGE("Promise rejection error: %s", error.c_str());
            return error;
        }

        // Convert result to string
        const char *resultStr = JS_ToCString(context, result);
        std::string resultString;
        if (resultStr) {
            resultString = resultStr;
            JS_FreeCString(context, resultStr);
        } else {
            resultString = "undefined";
        }

        JS_FreeValue(context, result);

        LOGI("JavaScript result: %s", resultString.c_str());
        return resultString;
    }
    
    bool resetContext() {
        LOGI("Resetting QuickJS context");
        
        if (!runtime) {
            LOGE("Cannot reset context: runtime not initialized");
            return false;
        }
        
        // Free the old context
        if (context) {
            JS_FreeContext(context);
            context = nullptr;
        }
        
        // Create a new context
        context = JS_NewContext(runtime);
        if (!context) {
            LOGE("Failed to create new QuickJS context");
            initialized = false;
            return false;
        }
        
        js_std_add_helpers(context, 0, nullptr);
        addConsoleSupport(context);
        addTimerPolyfills(context);
        addHttpPolyfills(context);
        
        LOGI("QuickJS context reset successfully");
        return true;
    }
    
    void cleanup() {
        LOGI("Cleaning up QuickJS Engine");

        if (context) {
            JS_FreeContext(context);
            context = nullptr;
        }

        if (runtime) {
            JS_FreeRuntime(runtime);
            runtime = nullptr;
        }

        initialized = false;
        LOGI("QuickJS cleanup complete");
    }
    
    bool isInitialized() const {
        return initialized && runtime && context;
    }
    
    JSContext* getContext() const {
        return context;
    }
};

static QuickJSEngine *g_quickjsEngine = nullptr;

extern "C" {

// Initialize QuickJS Engine
JNIEXPORT jboolean JNICALL
Java_com_quickjs_android_QuickJSBridge_initializeQuickJS(JNIEnv *env, jobject thiz) {
    LOGI("JNI: Initializing QuickJS Engine with HTTP polyfills");
    
    // Store JavaVM reference for HTTP requests
    if (!g_jvm) {
        env->GetJavaVM(&g_jvm);
    }
    
    if (g_quickjsEngine == nullptr) {
        g_quickjsEngine = new QuickJSEngine();
    }
    
    // Initialize HTTP polyfill references
    initializeHttpPolyfill(env, thiz);
    
    return g_quickjsEngine->initialize() ? JNI_TRUE : JNI_FALSE;
}

// Execute JavaScript code in QuickJS
JNIEXPORT jstring JNICALL
Java_com_quickjs_android_QuickJSBridge_executeScript(JNIEnv *env, jobject thiz, jstring script) {
    if (g_quickjsEngine == nullptr) {
        return env->NewStringUTF("Error: QuickJS not initialized");
    }
    
    const char* scriptStr = env->GetStringUTFChars(script, nullptr);
    std::string result = g_quickjsEngine->executeScript(std::string(scriptStr));
    env->ReleaseStringUTFChars(script, scriptStr);
    
    return env->NewStringUTF(result.c_str());
}

// Cleanup QuickJS Engine
JNIEXPORT void JNICALL
Java_com_quickjs_android_QuickJSBridge_cleanupQuickJS(JNIEnv *env, jobject thiz) {
    LOGI("JNI: Cleaning up QuickJS Engine");
    
    if (g_quickjsEngine != nullptr) {
        g_quickjsEngine->cleanup();
        delete g_quickjsEngine;
        g_quickjsEngine = nullptr;
    }
}

// Check if QuickJS is initialized
JNIEXPORT jboolean JNICALL
Java_com_quickjs_android_QuickJSBridge_isInitialized(JNIEnv *env, jobject thiz) {
    return (g_quickjsEngine != nullptr && g_quickjsEngine->isInitialized()) ? JNI_TRUE : JNI_FALSE;
}

// Reset context JNI function
JNIEXPORT jboolean JNICALL
Java_com_quickjs_android_QuickJSBridge_resetContext(JNIEnv *env, jobject thiz) {
    return g_quickjsEngine ? g_quickjsEngine->resetContext() : false;
}

// Compile JavaScript to bytecode JNI function
JNIEXPORT jbyteArray JNICALL
Java_com_quickjs_android_QuickJSBridge_compileScript(JNIEnv *env, jobject thiz, jstring script) {
    if (!g_quickjsEngine || !g_quickjsEngine->isInitialized()) {
        LOGE("QuickJS not initialized for compilation");
        return nullptr;
    }
    
    const char *scriptStr = env->GetStringUTFChars(script, nullptr);
    if (!scriptStr) {
        LOGE("Failed to get script string");
        return nullptr;
    }
    
    LOGI("Compiling JavaScript to real QuickJS bytecode");
    
    JSContext *context = g_quickjsEngine->getContext();
    if (!context) {
        LOGE("Failed to get QuickJS context");
        env->ReleaseStringUTFChars(script, scriptStr);
        return nullptr;
    }
    
    // Compile script to bytecode using QuickJS API
    JSValue compiledObj = JS_Eval(context, scriptStr, strlen(scriptStr), 
                                  "<bytecode>", JS_EVAL_FLAG_COMPILE_ONLY);
    
    env->ReleaseStringUTFChars(script, scriptStr);
    
    if (JS_IsException(compiledObj)) {
        // Handle compilation error
        JSValue exception = JS_GetException(context);
        const char *errorStr = JS_ToCString(context, exception);
        LOGE("Bytecode compilation failed: %s", errorStr ? errorStr : "Unknown error");
        if (errorStr) JS_FreeCString(context, errorStr);
        JS_FreeValue(context, exception);
        JS_FreeValue(context, compiledObj);
        return nullptr;
    }
    
    // Serialize compiled object to bytecode
    size_t bytecodeSize;
    uint8_t *bytecodeData = JS_WriteObject(context, &bytecodeSize, compiledObj, 
                                          JS_WRITE_OBJ_BYTECODE);
    
    JS_FreeValue(context, compiledObj);
    
    if (!bytecodeData) {
        LOGE("Failed to serialize bytecode");
        return nullptr;
    }
    
    // Create Java byte array
    jbyteArray result = env->NewByteArray(bytecodeSize);
    if (result) {
        env->SetByteArrayRegion(result, 0, bytecodeSize, 
            reinterpret_cast<const jbyte*>(bytecodeData));
        LOGI("Real bytecode created: %zu bytes", bytecodeSize);
    } else {
        LOGE("Failed to create Java byte array");
    }
    
    // Free the bytecode data
    js_free(context, bytecodeData);
    
    return result;
}

// Execute bytecode JNI function
JNIEXPORT jstring JNICALL
Java_com_quickjs_android_QuickJSBridge_executeBytecode(JNIEnv *env, jobject thiz, jbyteArray bytecode) {
    if (!g_quickjsEngine || !g_quickjsEngine->isInitialized()) {
        LOGE("QuickJS not initialized for bytecode execution");
        return env->NewStringUTF("Error: QuickJS not initialized");
    }
    
    if (!bytecode) {
        LOGE("Null bytecode provided");
        return env->NewStringUTF("Error: Null bytecode");
    }
    
    jsize bytecodeLength = env->GetArrayLength(bytecode);
    if (bytecodeLength <= 0) {
        LOGE("Empty bytecode provided");
        return env->NewStringUTF("Error: Empty bytecode");
    }
    
    // Get bytecode data
    jbyte* bytecodeData = env->GetByteArrayElements(bytecode, nullptr);
    if (!bytecodeData) {
        LOGE("Failed to get bytecode data");
        return env->NewStringUTF("Error: Failed to get bytecode data");
    }
    
    LOGI("Executing real QuickJS bytecode");
    
    JSContext *context = g_quickjsEngine->getContext();
    if (!context) {
        LOGE("Failed to get QuickJS context");
        env->ReleaseByteArrayElements(bytecode, bytecodeData, JNI_ABORT);
        return env->NewStringUTF("Error: Failed to get QuickJS context");
    }
    
    // Deserialize bytecode to JSValue
    JSValue compiledObj = JS_ReadObject(context, 
                                       reinterpret_cast<const uint8_t*>(bytecodeData), 
                                       bytecodeLength, 
                                       JS_READ_OBJ_BYTECODE);
    
    env->ReleaseByteArrayElements(bytecode, bytecodeData, JNI_ABORT);
    
    if (JS_IsException(compiledObj)) {
        JSValue exception = JS_GetException(context);
        const char *errorStr = JS_ToCString(context, exception);
        LOGE("Bytecode deserialization failed: %s", errorStr ? errorStr : "Unknown error");
        std::string error = "Error: Bytecode deserialization failed";
        if (errorStr) {
            error += ": ";
            error += errorStr;
            JS_FreeCString(context, errorStr);
        }
        JS_FreeValue(context, exception);
        JS_FreeValue(context, compiledObj);
        return env->NewStringUTF(error.c_str());
    }
    
    // Execute the bytecode function
    JSValue result = JS_EvalFunction(context, compiledObj);
    
    if (JS_IsException(result)) {
        // Handle execution error
        JSValue exception = JS_GetException(context);
        const char *errorStr = JS_ToCString(context, exception);
        LOGE("Bytecode execution failed: %s", errorStr ? errorStr : "Unknown error");
        std::string error = "Error: Bytecode execution failed";
        if (errorStr) {
            error += ": ";
            error += errorStr;
            JS_FreeCString(context, errorStr);
        }
        JS_FreeValue(context, exception);
        JS_FreeValue(context, result);
        return env->NewStringUTF(error.c_str());
    }
    
    // Wait for promises if needed (same as regular execution)
    result = js_std_await(context, result);
    
    // Check if awaiting resulted in an exception (promise rejection)
    if (JS_IsException(result)) {
        JSValue exception = JS_GetException(context);
        const char *errorStr = JS_ToCString(context, exception);
        LOGE("Bytecode promise rejection: %s", errorStr ? errorStr : "Unknown error");
        std::string error = "Promise Rejection: ";
        if (errorStr) {
            error += errorStr;
            JS_FreeCString(context, errorStr);
        } else {
            error += "Unknown error";
        }
        JS_FreeValue(context, exception);
        JS_FreeValue(context, result);
        return env->NewStringUTF(error.c_str());
    }
    
    // Convert result to string
    const char *resultStr = JS_ToCString(context, result);
    std::string resultString;
    if (resultStr) {
        resultString = resultStr;
        JS_FreeCString(context, resultStr);
    } else {
        resultString = "undefined";
    }
    
    JS_FreeValue(context, result);
    
    LOGI("Bytecode execution result: %s", resultString.c_str());
    return env->NewStringUTF(resultString.c_str());
}

// HTTP request JNI function
JNIEXPORT jstring JNICALL
Java_com_quickjs_android_QuickJSBridge_nativeHttpRequest(JNIEnv *env, jobject thiz, jstring url, jstring options) {
    // This function is not directly used but kept for compatibility
    // The actual HTTP requests go through js_http_request -> handleHttpRequest
    return env->NewStringUTF("{}");
}

}