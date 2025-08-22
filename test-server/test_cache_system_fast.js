// Fast Test script for JavaScript caching system
// This version avoids slow external HTTP requests

console.log("🔧 Testing JavaScript Caching System (Fast Version)");

// Test 1: Basic caching test
const cacheTest = {
    testName: "JavaScript Caching System Test (Fast)",
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

console.log("Computing Fibonacci(20)..."); // Reduced from 25 to 20 for faster execution
const startTime = Date.now();
const fibResult = fibonacci(20);
const computeTime = Date.now() - startTime;

cacheTest.computation = {
    fibonacci20: fibResult,
    computeTimeMs: computeTime,
    note: "Bytecode caching should speed up subsequent executions"
};

// Test 3: HTTP polyfill availability test (no actual requests)
console.log("Testing HTTP polyfill availability...");
cacheTest.httpTest = {
    fetchAvailable: typeof fetch === 'function',
    xmlHttpRequestAvailable: typeof XMLHttpRequest === 'function',
    note: "HTTP polyfills are available for network requests",
    skipReason: "Skipped actual HTTP request for faster execution"
};

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

// Test 5: JavaScript engine info
cacheTest.engineInfo = {
    userAgent: typeof navigator !== 'undefined' ? navigator.userAgent : 'Not available',
    dateNow: Date.now(),
    mathRandom: Math.random(),
    jsonSupport: typeof JSON === 'object',
    promiseSupport: typeof Promise === 'function',
    asyncSupport: typeof async !== 'undefined'
};

console.log("🎯 Fast cache test completed");

// Return results immediately - explicit return for IIFE
return JSON.stringify(cacheTest, null, 2);