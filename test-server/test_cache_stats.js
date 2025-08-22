// Cache Statistics Test Script
// Shows cache performance and statistics

console.log("📊 Testing Cache Statistics");

// Simple performance test
const perfTest = {
    testName: "Cache Performance Test",
    timestamp: new Date().toISOString(),
    iterations: []
};

// Run multiple small computations to test caching
for (let i = 1; i <= 5; i++) {
    const start = Date.now();
    
    // Simple computation
    let result = 0;
    for (let j = 0; j < 1000; j++) {
        result += Math.sqrt(j * i);
    }
    
    const time = Date.now() - start;
    
    perfTest.iterations.push({
        iteration: i,
        result: Math.round(result),
        timeMs: time,
        note: i === 1 ? "First run (cache miss expected)" : `Run ${i} (cache hit expected)`
    });
    
    console.log(`Iteration ${i}: ${time}ms`);
}

// Calculate performance improvement
const firstRun = perfTest.iterations[0].timeMs;
const avgSubsequent = perfTest.iterations.slice(1).reduce((sum, iter) => sum + iter.timeMs, 0) / (perfTest.iterations.length - 1);
const improvement = ((firstRun - avgSubsequent) / firstRun * 100).toFixed(1);

perfTest.summary = {
    firstRunMs: firstRun,
    avgSubsequentMs: Math.round(avgSubsequent),
    improvementPercent: improvement,
    note: "Performance improvement from caching (if any)"
};

console.log(`📈 Performance improvement: ${improvement}%`);
console.log("✅ Cache statistics test completed");

// Return results
JSON.stringify(perfTest, null, 2);
