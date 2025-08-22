// Simple test to verify fetch and XMLHttpRequest polyfills are working
// This script can be executed directly in QuickJS

console.log("🧪 Testing HTTP Polyfills...");

// Test 1: Check if polyfills are available
const results = {
    testName: "HTTP Polyfills Test",
    timestamp: new Date().toISOString(),
    tests: []
};

// Check fetch availability
results.tests.push({
    name: "fetch() availability",
    passed: typeof fetch === 'function',
    details: typeof fetch === 'function' ? "✅ fetch() is available" : "❌ fetch() not found"
});

// Check XMLHttpRequest availability  
results.tests.push({
    name: "XMLHttpRequest availability",
    passed: typeof XMLHttpRequest === 'function',
    details: typeof XMLHttpRequest === 'function' ? "✅ XMLHttpRequest is available" : "❌ XMLHttpRequest not found"
});

// Check XMLHttpRequest constants
if (typeof XMLHttpRequest === 'function') {
    const constantsTest = XMLHttpRequest.UNSENT === 0 && 
                         XMLHttpRequest.OPENED === 1 && 
                         XMLHttpRequest.HEADERS_RECEIVED === 2 && 
                         XMLHttpRequest.LOADING === 3 && 
                         XMLHttpRequest.DONE === 4;
    
    results.tests.push({
        name: "XMLHttpRequest constants",
        passed: constantsTest,
        details: constantsTest ? "✅ All constants defined correctly" : "❌ Constants missing or incorrect"
    });
}

// Test basic XMLHttpRequest instantiation
try {
    const xhr = new XMLHttpRequest();
    const xhrTest = xhr.readyState === 0 && 
                    xhr.status === 0 && 
                    typeof xhr.open === 'function' && 
                    typeof xhr.send === 'function';
    
    results.tests.push({
        name: "XMLHttpRequest instantiation",
        passed: xhrTest,
        details: xhrTest ? "✅ XMLHttpRequest instance created successfully" : "❌ XMLHttpRequest instantiation failed"
    });
} catch (e) {
    results.tests.push({
        name: "XMLHttpRequest instantiation",
        passed: false,
        details: "❌ Error creating XMLHttpRequest: " + e.message
    });
}

// Test native HTTP request function availability
results.tests.push({
    name: "_nativeHttpRequest availability",
    passed: typeof _nativeHttpRequest === 'function',
    details: typeof _nativeHttpRequest === 'function' ? "✅ Native HTTP bridge available" : "❌ Native HTTP bridge missing"
});

// Calculate summary
const passedTests = results.tests.filter(test => test.passed).length;
const totalTests = results.tests.length;

results.summary = {
    passed: passedTests,
    total: totalTests,
    success: passedTests === totalTests,
    message: passedTests === totalTests ? 
        "🎉 All HTTP polyfill tests passed!" : 
        `⚠️ ${passedTests}/${totalTests} tests passed`
};

// Return formatted results
JSON.stringify(results, null, 2);
