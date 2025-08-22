// Simple test to verify return value works
console.log("Testing simple return");

const testObj = {
    message: "Hello from simple test",
    timestamp: new Date().toISOString(),
    success: true
};

console.log("About to return result");

// This should be returned when wrapped in IIFE
return JSON.stringify(testObj, null, 2);
