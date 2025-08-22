package com.quickjs.android

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Bridge class for QuickJS JavaScript engine integration
 * Provides methods to initialize QuickJS, execute JavaScript code, and manage resources
 * QuickJS is a lightweight, fast JavaScript engine with ES2023 support
 */
class QuickJSBridge(private val context: android.content.Context) {

    companion object {
        private const val TAG = "QuickJSBridge"

        // Load the native library
        init {
            try {
                System.loadLibrary("quickjs")
                Log.i(TAG, "Native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library", e)
            }
        }
    }

    // Network service for remote JavaScript loading
    private val networkService = NetworkService()
    private val httpService = HttpService()
    
    // Execution history for remote scripts
    private val executionHistory = mutableListOf<RemoteExecutionResult>()
    
    /**
     * Data class representing the result of remote JavaScript execution
     */
    data class RemoteExecutionResult(
        val url: String,
        val fileName: String,
        val timestamp: Long,
        val success: Boolean,
        val result: String,
        val executionTimeMs: Long,
        val contentLength: Int
    )

    /**
     * Data class representing execution result
     */
    data class ExecutionResult(
        val success: Boolean,
        val result: String,
        val executionTimeMs: Long,
        val error: String? = null
    )

    /**
     * Callback interface for remote JavaScript execution
     */
    interface RemoteExecutionCallback {
        fun onProgress(message: String)
        fun onSuccess(result: RemoteExecutionResult)
        fun onError(url: String, error: String)
    }

    // Native method declarations
    private external fun initializeQuickJS(): Boolean
    private external fun executeScript(script: String): String
    private external fun cleanupQuickJS()
    private external fun isInitialized(): Boolean
    private external fun resetContext(): Boolean
    
    // Bytecode compilation and execution methods
    private external fun compileScript(script: String): ByteArray?
    private external fun executeBytecode(bytecode: ByteArray): String
    
    // HTTP polyfill native methods
    private external fun nativeHttpRequest(url: String, optionsJson: String): String

    private var initialized = false

    /**
     * Initialize the QuickJS JavaScript engine
     * @return true if initialization was successful, false otherwise
     */
    fun initialize(): Boolean {
        Log.i(TAG, "Initializing QuickJS Bridge")

        if (initialized) {
            Log.w(TAG, "QuickJS Bridge already initialized")
            return true
        }

        try {
            initialized = initializeQuickJS()
            Log.i(TAG, "QuickJS Bridge initialization: ${if (initialized) "SUCCESS" else "FAILED"}")

            if (!initialized) {
                Log.e(TAG, "QuickJS initialization failed - native function returned false")
            }

            return initialized
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "QuickJS initialization failed - native library not loaded", e)
            initialized = false
            return false
        } catch (e: Exception) {
            Log.e(TAG, "QuickJS initialization failed - unexpected error", e)
            initialized = false
            return false
        }
    }

    /**
     * Execute JavaScript code in QuickJS engine
     * @param jsCode The JavaScript code to execute
     * @param isolatedExecution Whether to execute in an isolated context to avoid variable conflicts
     * @return The result of the JavaScript execution as a string
     */
    fun runJavaScript(jsCode: String, isolatedExecution: Boolean = false): String {
        if (!initialized) {
            val error = "❌ QuickJS Bridge not initialized. Call initialize() first."
            Log.e(TAG, error)
            return error
        }

        if (jsCode.isBlank()) {
            val error = "❌ JavaScript code cannot be empty"
            Log.e(TAG, error)
            return error
        }

        if (jsCode.length > 10000) {
            val error = "❌ JavaScript code too long (max 10,000 characters)"
            Log.e(TAG, error)
            return error
        }

        Log.i(TAG, "Executing JavaScript in QuickJS: $jsCode")

        try {
            // Wrap in IIFE if isolated execution is requested
            val finalCode = if (isolatedExecution) {
                // For isolated execution, wrap in IIFE
                "(function() { \n$jsCode\n })();"
            } else {
                jsCode
            }
            
            val result = executeScript(finalCode)
            Log.i(TAG, "QuickJS result: $result")

            if (result.startsWith("Error:") || result.startsWith("JavaScript Error:")) {
                Log.w(TAG, "JavaScript execution returned error: $result")
            }

            return result
        } catch (e: UnsatisfiedLinkError) {
            val error = "❌ Native library error during JavaScript execution"
            Log.e(TAG, error, e)
            return error
        } catch (e: Exception) {
            val error = "❌ Unexpected error during JavaScript execution: ${e.message}"
            Log.e(TAG, error, e)
            return error
        }
    }

    /**
     * Test various JavaScript operations specific to QuickJS features
     * @return Map of test results showcasing QuickJS capabilities
     */
    fun runTestSuite(): Map<String, String> {
        val results = mutableMapOf<String, String>()

        // Test 1: Simple arithmetic
        results["arithmetic"] = runJavaScript("2 + 3 * 4")

        // Test 2: String manipulation
        results["string"] = runJavaScript("'Hello ' + 'World'")

        // Test 3: JSON creation
        results["json"] = runJavaScript("JSON.stringify({ name: 'QuickJS', value: 42 })")

        // Test 4: Function definition and call
        results["function"] = runJavaScript("function add(a, b) { return a + b; } add(15, 25)")

        // Test 5: Array operations
        results["array"] = runJavaScript("[1, 2, 3].map(x => x * 2).join(', ')")

        // Test 6: ES2023 features (QuickJS specific)
        results["es2023_destructuring"] = runJavaScript("const [a, b] = [10, 20]; a + b")

        // Test 7: Template literals
        results["template_literals"] = runJavaScript("const name = 'QuickJS'; `Hello \${name}!`")

        // Test 8: Arrow functions
        results["arrow_functions"] = runJavaScript("const square = x => x * x; square(7)")

        return results
    }

    /**
     * Reset QuickJS context to clear variables and avoid conflicts
     */
    fun resetQuickJSContext(): Boolean {
        if (!initialized) {
            Log.w(TAG, "Cannot reset context: QuickJS not initialized")
            return false
        }
        
        Log.i(TAG, "Resetting QuickJS context to clear variables")
        return resetContext() // Call native method
    }

    /**
     * Get engine information
     */
    fun getEngineInfo(): String {
        return "QuickJS Engine v2025-04-26\n" +
               "ES2023 Support: Full\n" +
               "Memory Management: Enabled\n" +
               "HTTP Polyfills: Enabled\n" +
               "Console Support: Enabled"
    }

    /**
     * Get memory statistics
     */
    fun getMemoryStats(): String {
        if (!initialized) {
            return "QuickJS not initialized"
        }
        return "QuickJS Memory Statistics:\n" +
               "Memory Management: Enabled\n" +
               "Limit: 64MB\n" +
               "GC Threshold: 1MB\n" +
               "Status: Active"
    }

    /**
     * Compile JavaScript to bytecode for caching
     */
    fun compileJavaScriptToBytecode(script: String): ByteArray? {
        return try {
            if (!initialized) {
                Log.e(TAG, "QuickJS not initialized for bytecode compilation")
                return null
            }
            val bytecode = compileScript(script)
            if (bytecode != null) {
                Log.i(TAG, "Successfully compiled ${script.length} chars to ${bytecode.size} bytes of bytecode")
            }
            bytecode
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compile JavaScript to bytecode", e)
            null
        }
    }

    /**
     * Execute bytecode directly
     */
    fun executeCompiledBytecode(bytecode: ByteArray): ExecutionResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            if (!initialized) {
                ExecutionResult(
                    success = false,
                    result = "",
                    executionTimeMs = 0,
                    error = "QuickJS not initialized"
                )
            } else {
                val result = executeBytecode(bytecode)
                val executionTime = System.currentTimeMillis() - startTime
                
                ExecutionResult(
                    success = !result.startsWith("Error:"),
                    result = result,
                    executionTimeMs = executionTime
                )
            }
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            ExecutionResult(
                success = false,
                result = "",
                executionTimeMs = executionTime,
                error = e.message
            )
        }
    }

    /**
     * Execute remote JavaScript from URL
     */
    fun executeRemoteJavaScript(url: String, callback: RemoteExecutionCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                callback.onProgress("🔄 Downloading JavaScript from $url...")
                
                val startTime = System.currentTimeMillis()
                val content = networkService.downloadText(url)
                
                if (content.isNullOrEmpty()) {
                    callback.onError(url, "Failed to download content")
                    return@launch
                }
                
                callback.onProgress("✅ Downloaded ${content.length} characters. Executing...")
                
                val result = withContext(Dispatchers.Main) {
                    runJavaScript(content)
                }
                
                val executionTime = System.currentTimeMillis() - startTime
                val fileName = url.substringAfterLast("/").ifEmpty { "remote_script.js" }
                
                val remoteResult = RemoteExecutionResult(
                    url = url,
                    fileName = fileName,
                    timestamp = System.currentTimeMillis(),
                    success = !result.startsWith("Error:") && !result.startsWith("❌"),
                    result = result,
                    executionTimeMs = executionTime,
                    contentLength = content.length
                )
                
                executionHistory.add(0, remoteResult)
                
                callback.onSuccess(remoteResult)
                
            } catch (e: Exception) {
                Log.e(TAG, "Remote JavaScript execution failed", e)
                callback.onError(url, "Execution failed: ${e.message}")
            }
        }
    }

    /**
     * Get execution history
     */
    fun getExecutionHistory(): List<RemoteExecutionResult> = executionHistory.toList()

    /**
     * Clear execution history
     */
    fun clearExecutionHistory() {
        executionHistory.clear()
    }

    /**
     * Get popular JavaScript URLs for testing
     */
    fun getPopularJavaScriptUrls(): Map<String, String> {
        return mapOf(
            "Basic Remote Script" to "http://localhost:8000/test_remote_script.js",
            "HTTP Polyfills Test" to "http://localhost:8000/test_fetch_polyfill.js",
            "Cache System (Fast)" to "http://localhost:8000/test_cache_system_fast.js",
            "Cache System (Full)" to "http://localhost:8000/test_cache_system.js",
            "Bytecode Demo" to "http://localhost:8000/test_bytecode_demo.js",
            "Cache Statistics" to "http://localhost:8000/test_cache_stats.js",
            "Simple Fetch Test" to "http://localhost:8000/simple_fetch_test.js",
            "Simple Return Test" to "http://localhost:8000/test_simple_return.js",
            "Lodash Library" to "https://cdn.jsdelivr.net/npm/lodash@4.17.21/lodash.min.js",
            "Moment.js Library" to "https://cdn.jsdelivr.net/npm/moment@2.29.4/moment.min.js",
            "Test Bytecode Compilation" to "BYTECODE_TEST"
        )
    }

    /**
     * Build local server URL
     */
    fun buildLocalServerUrl(ip: String, port: String, filename: String = "test_remote_script.js"): String {
        return "http://$ip:$port/$filename"
    }

    /**
     * Test remote execution with a simple script
     */
    fun testRemoteExecution(callback: RemoteExecutionCallback) {
        val testScript = """
            // Simple remote execution test
            const greeting = "Hello from Remote QuickJS!";
            const numbers = [1, 2, 3, 4, 5];
            const sum = numbers.reduce((a, b) => a + b, 0);
            greeting + " Sum: " + sum;
        """.trimIndent()
        
        // Simulate remote execution
        CoroutineScope(Dispatchers.IO).launch {
            try {
                callback.onProgress("🔄 Running remote execution test...")
                
                val startTime = System.currentTimeMillis()
                
                val result = withContext(Dispatchers.Main) {
                    runJavaScript(testScript)
                }
                
                val executionTime = System.currentTimeMillis() - startTime
                
                val remoteResult = RemoteExecutionResult(
                    url = "internal://test",
                    fileName = "remote_test.js",
                    timestamp = System.currentTimeMillis(),
                    success = !result.startsWith("Error:") && !result.startsWith("❌"),
                    result = result,
                    executionTimeMs = executionTime,
                    contentLength = testScript.length
                )
                
                executionHistory.add(0, remoteResult)
                callback.onSuccess(remoteResult)
                
            } catch (e: Exception) {
                callback.onError("internal://test", "Test failed: ${e.message}")
            }
        }
    }

    /**
     * Run bytecode test
     */
    fun runBytecodeTest(callback: RemoteExecutionCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                callback.onProgress("🔄 Testing bytecode compilation...")
                
                val testScript = """
                    // Bytecode compilation test
                    const factorial = (n) => n <= 1 ? 1 : n * factorial(n - 1);
                    const result = factorial(5);
                    "Factorial of 5 is: " + result;
                """.trimIndent()
                
                val startTime = System.currentTimeMillis()
                
                // Compile to bytecode
                val bytecode = compileJavaScriptToBytecode(testScript)
                if (bytecode == null) {
                    callback.onError("bytecode://test", "Failed to compile to bytecode")
                    return@launch
                }
                
                callback.onProgress("✅ Compiled to ${bytecode.size} bytes. Executing bytecode...")
                
                // Execute bytecode
                val executionResult = executeCompiledBytecode(bytecode)
                val totalTime = System.currentTimeMillis() - startTime
                
                val remoteResult = RemoteExecutionResult(
                    url = "bytecode://test",
                    fileName = "bytecode_test.js",
                    timestamp = System.currentTimeMillis(),
                    success = executionResult.success,
                    result = executionResult.result,
                    executionTimeMs = totalTime,
                    contentLength = testScript.length
                )
                
                executionHistory.add(0, remoteResult)
                callback.onSuccess(remoteResult)
                
            } catch (e: Exception) {
                callback.onError("bytecode://test", "Bytecode test failed: ${e.message}")
            }
        }
    }

    /**
     * Handle HTTP requests from JavaScript (called by native code)
     */
    fun handleHttpRequest(url: String, optionsJson: String): String {
        Log.i(TAG, "Handling HTTP request: $url")
        // Return a mock response for now
        return """
        {
            "status": 200,
            "statusText": "OK",
            "ok": true,
            "redirected": false,
            "url": "$url",
            "type": "basic",
            "headers": {},
            "body": "Mock response from QuickJS HTTP polyfill"
        }
        """.trimIndent()
    }

    /**
     * Cleanup QuickJS resources
     */
    fun cleanup() {
        Log.i(TAG, "Cleaning up QuickJS Bridge")

        if (!initialized) {
            Log.w(TAG, "QuickJS Bridge not initialized, nothing to cleanup")
            return
        }

        try {
            cleanupQuickJS()
            initialized = false
            Log.i(TAG, "QuickJS Bridge cleanup completed successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "QuickJS cleanup failed - native library error", e)
        } catch (e: Exception) {
            Log.e(TAG, "QuickJS cleanup failed - unexpected error", e)
        }
    }

    /**
     * Check if QuickJS is initialized
     */
    fun isQuickJSInitialized(): Boolean {
        return initialized && try {
            isInitialized()
        } catch (e: Exception) {
            Log.w(TAG, "Error checking initialization status", e)
            false
        }
    }
}