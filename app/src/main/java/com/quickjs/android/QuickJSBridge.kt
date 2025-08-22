package com.quickjs.android

import android.util.Log

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

    // Native method declarations
    private external fun initializeQuickJS(): Boolean
    private external fun executeScript(script: String): String
    private external fun cleanupQuickJS()
    private external fun isInitialized(): Boolean
    private external fun resetContext(): Boolean
    
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
        return "Memory statistics available through native calls"
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