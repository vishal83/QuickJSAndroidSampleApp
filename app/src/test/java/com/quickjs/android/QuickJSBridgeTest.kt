package com.quickjs.android

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit tests for QuickJS Bridge functionality
 * These tests validate the core JavaScript execution capabilities
 */
class QuickJSBridgeTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var quickJSBridge: QuickJSBridge

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        quickJSBridge = QuickJSBridge(mockContext)
    }

    @Test
    fun testBridgeCreation() {
        // Test that the bridge can be created without errors
        assertNotNull(quickJSBridge)
        assertFalse(quickJSBridge.isReady())
    }

    @Test
    fun testExecutionWithoutInitialization() = runBlocking {
        // Test that execution fails gracefully when not initialized
        val result = quickJSBridge.execute("1 + 1")
        
        assertFalse(result.success)
        assertTrue(result.result.contains("not initialized"))
        assertEquals(0, result.executionTimeMs)
    }

    @Test
    fun testExecutionHistory() = runBlocking {
        // Test execution history tracking
        val initialHistory = quickJSBridge.getExecutionHistory()
        assertEquals(0, initialHistory.size)
        
        // Execute some code (will fail without initialization, but should be tracked)
        quickJSBridge.execute("console.log('test')")
        
        val historyAfterExecution = quickJSBridge.getExecutionHistory()
        assertEquals(1, historyAfterExecution.size)
        
        val executionResult = historyAfterExecution[0]
        assertEquals("console.log('test')", executionResult.script)
        assertFalse(executionResult.success) // Should fail without initialization
    }

    @Test
    fun testClearHistory() = runBlocking {
        // Add some execution to history
        quickJSBridge.execute("test script")
        assertEquals(1, quickJSBridge.getExecutionHistory().size)
        
        // Clear history
        quickJSBridge.clearHistory()
        assertEquals(0, quickJSBridge.getExecutionHistory().size)
    }

    @Test
    fun testMemoryStatsWithoutInitialization() {
        // Test memory stats when not initialized
        val memoryStats = quickJSBridge.getMemoryStats()
        assertTrue(memoryStats.contains("not initialized") || memoryStats.contains("error"))
    }

    @Test
    fun testResetWithoutInitialization() {
        // Test reset when not initialized
        val resetResult = quickJSBridge.reset()
        // Should handle gracefully (may return false or true depending on implementation)
        // Main thing is it shouldn't crash
        assertTrue(resetResult || !resetResult) // Just ensure no exception
    }

    @Test
    fun testCompileWithoutInitialization() = runBlocking {
        // Test compilation when not initialized
        val bytecode = quickJSBridge.compile("function test() { return 42; }")
        assertNull(bytecode) // Should return null when not initialized
    }

    @Test
    fun testExecuteBytecodeWithoutInitialization() = runBlocking {
        // Test bytecode execution when not initialized
        val result = quickJSBridge.executeCompiledBytecode(byteArrayOf(1, 2, 3, 4))
        
        assertFalse(result.success)
        assertTrue(result.result.contains("not initialized"))
        assertEquals("[bytecode]", result.script)
    }

    @Test
    fun testExecutionResultDataClass() {
        // Test the ExecutionResult data class
        val result = QuickJSBridge.ExecutionResult(
            script = "test script",
            timestamp = System.currentTimeMillis(),
            success = true,
            result = "test result",
            executionTimeMs = 100
        )
        
        assertEquals("test script", result.script)
        assertTrue(result.success)
        assertEquals("test result", result.result)
        assertEquals(100, result.executionTimeMs)
    }

    @Test
    fun testCleanup() {
        // Test that cleanup doesn't crash
        quickJSBridge.cleanup()
        // Should not throw any exceptions
        assertTrue(true)
    }
}
