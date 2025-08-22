// Test script for HTTP polyfills (fetch and XMLHttpRequest)
console.log("🌐 Testing HTTP Polyfills");

// Test 1: Check if polyfills are available
const polyfillTest = {
    testName: "HTTP Polyfill Test",
    timestamp: new Date().toISOString(),
    fetchAvailable: typeof fetch === 'function',
    xmlHttpRequestAvailable: typeof XMLHttpRequest === 'function'
};

console.log("fetch available:", polyfillTest.fetchAvailable);
console.log("XMLHttpRequest available:", polyfillTest.xmlHttpRequestAvailable);

// Test 2: Simple fetch test (to httpbin.org for testing)
if (polyfillTest.fetchAvailable) {
    console.log("🔄 Testing fetch polyfill...");
    
    // Note: This is an async operation, but QuickJS execution is synchronous
    // The fetch will be initiated but the result won't be waited for
    fetch('https://httpbin.org/json')
        .then(response => response.json())
        .then(data => {
            console.log("✅ Fetch successful:", data);
        })
        .catch(error => {
            console.log("❌ Fetch error:", error.message);
        });
    
    polyfillTest.fetchTest = {
        initiated: true,
        note: "Fetch request initiated (async, result may not be captured)"
    };
} else {
    polyfillTest.fetchTest = {
        initiated: false,
        reason: "fetch not available"
    };
}

// Test 3: XMLHttpRequest test
if (polyfillTest.xmlHttpRequestAvailable) {
    console.log("🔄 Testing XMLHttpRequest polyfill...");
    
    try {
        const xhr = new XMLHttpRequest();
        xhr.open('GET', 'https://httpbin.org/json', false); // Synchronous for testing
        xhr.send();
        
        if (xhr.status === 200) {
            console.log("✅ XMLHttpRequest successful");
            polyfillTest.xhrTest = {
                success: true,
                status: xhr.status,
                responseLength: xhr.responseText.length
            };
        } else {
            console.log("❌ XMLHttpRequest failed:", xhr.status);
            polyfillTest.xhrTest = {
                success: false,
                status: xhr.status
            };
        }
    } catch (error) {
        console.log("❌ XMLHttpRequest error:", error.message);
        polyfillTest.xhrTest = {
            success: false,
            error: error.message
        };
    }
} else {
    polyfillTest.xhrTest = {
        success: false,
        reason: "XMLHttpRequest not available"
    };
}

console.log("🎯 HTTP polyfill test completed");

// Return results
JSON.stringify(polyfillTest, null, 2);