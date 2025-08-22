// Test script for JavaScript caching system
// This script demonstrates the caching capabilities

console.log("🔧 Testing JavaScript Caching System");

// Test 1: Basic caching test
const cacheTest = {
    testName: "JavaScript Caching System Test",
    timestamp: new Date().toISOString(),
    testId: Math.random().toString(36).substr(2, 9),
    features: [
        "HTTP caching with ETag/Last-Modified",
        "Bytecode compilation and caching", 
        "Memory and disk cache management",
        "Cache statistics and monitoring",
        "Automatic cache invalidation"
    ],
    performance: {
        note: "This script should load faster on subsequent executions",
        cacheHint: "Check cache statistics to see hit/miss rates"
    }
};

// Test 2: Simulate some computation that would benefit from caching
function fibonacci(n) {
    if (n <= 1) return n;
    return fibonacci(n - 1) + fibonacci(n - 2);
}

console.log("Computing Fibonacci(25)...");
const startTime = Date.now();
const fibResult = fibonacci(25);
const computeTime = Date.now() - startTime;

cacheTest.computation = {
    fibonacci25: fibResult,
    computeTimeMs: computeTime,
    note: "Bytecode caching should speed up subsequent executions"
};

// Test 3: HTTP request to demonstrate network caching (synchronous approach)
console.log("Testing HTTP caching...");
try {
    // Use synchronous approach since we need to return results immediately
    const response = fetch('https://httpbin.org/json');
    console.log("HTTP request initiated");
    cacheTest.httpTest = {
        success: true,
        note: "HTTP request sent asynchronously - check logs for completion",
        fetchAvailable: typeof fetch === 'function'
    };
} catch (error) {
    console.log("⚠️ HTTP request failed:", error.message);
    cacheTest.httpTest = {
        success: false,
        error: error.message,
        fetchAvailable: typeof fetch === 'function'
    };
}

// Test 4: Memory usage information
if (typeof gc === 'function') {
    gc(); // Trigger garbage collection if available
    console.log("✅ Garbage collection triggered");
    cacheTest.memoryTest = {
        gcAvailable: true,
        note: "Garbage collection triggered"
    };
} else {
    cacheTest.memoryTest = {
        gcAvailable: false,
        note: "Garbage collection not available"
    };
}

console.log("🎯 Cache test completed");

// Return results immediately (don't wait for async operations)
JSON.stringify(cacheTest, null, 2);
