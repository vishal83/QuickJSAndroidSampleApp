package com.quickjs.android

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for HTTP service functionality
 * These tests validate HTTP request capabilities used by JavaScript polyfills
 */
class HttpServiceTest {

    private lateinit var httpService: HttpService
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        httpService = HttpService()
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testServiceCreation() {
        // Test that the service can be created
        assertNotNull(httpService)
    }

    @Test
    fun testSimpleGetRequest() {
        // Setup mock response
        mockWebServer.enqueue(
            MockResponse()
                .setBody("Hello, World!")
                .setResponseCode(200)
        )

        val url = mockWebServer.url("/test").toString()
        
        // Make GET request
        val response = httpService.get(url)
        
        assertEquals("Hello, World!", response)
    }

    @Test
    fun testSimplePostRequest() {
        // Setup mock response
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{\"success\": true}")
                .setResponseCode(200)
        )

        val url = mockWebServer.url("/api/test").toString()
        val requestBody = "{\"message\": \"test\"}"
        
        // Make POST request
        val response = httpService.post(url, requestBody)
        
        assertEquals("{\"success\": true}", response)
        
        // Verify the request was made correctly
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals(requestBody, recordedRequest.body.readUtf8())
    }

    @Test
    fun testGetRequestWithHeaders() {
        // Setup mock response
        mockWebServer.enqueue(
            MockResponse()
                .setBody("Success")
                .setResponseCode(200)
        )

        val url = mockWebServer.url("/test").toString()
        val headers = mapOf(
            "Authorization" to "Bearer token123",
            "Content-Type" to "application/json"
        )
        
        // Make GET request with headers
        val response = httpService.get(url, headers)
        
        assertEquals("Success", response)
        
        // Verify headers were sent
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer token123", recordedRequest.getHeader("Authorization"))
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"))
    }

    @Test
    fun testMakeRequestWithDifferentMethods() {
        // Test GET
        mockWebServer.enqueue(MockResponse().setBody("GET response").setResponseCode(200))
        val getResponse = httpService.makeRequest(
            mockWebServer.url("/get").toString(),
            "GET"
        )
        assertEquals(200, getResponse.code)
        assertEquals("GET response", getResponse.body?.string())
        getResponse.close()

        // Test POST
        mockWebServer.enqueue(MockResponse().setBody("POST response").setResponseCode(201))
        val postResponse = httpService.makeRequest(
            mockWebServer.url("/post").toString(),
            "POST",
            emptyMap(),
            "test body"
        )
        assertEquals(201, postResponse.code)
        assertEquals("POST response", postResponse.body?.string())
        postResponse.close()

        // Test PUT
        mockWebServer.enqueue(MockResponse().setBody("PUT response").setResponseCode(200))
        val putResponse = httpService.makeRequest(
            mockWebServer.url("/put").toString(),
            "PUT",
            emptyMap(),
            "put body"
        )
        assertEquals(200, putResponse.code)
        assertEquals("PUT response", putResponse.body?.string())
        putResponse.close()

        // Test DELETE
        mockWebServer.enqueue(MockResponse().setBody("DELETE response").setResponseCode(204))
        val deleteResponse = httpService.makeRequest(
            mockWebServer.url("/delete").toString(),
            "DELETE"
        )
        assertEquals(204, deleteResponse.code)
        deleteResponse.close()
    }

    @Test
    fun testErrorHandling() {
        // Setup mock error response
        mockWebServer.enqueue(
            MockResponse()
                .setBody("Not Found")
                .setResponseCode(404)
        )

        val url = mockWebServer.url("/notfound").toString()
        
        // Make request that will return 404
        val response = httpService.makeRequest(url, "GET")
        
        assertEquals(404, response.code)
        assertEquals("Not Found", response.body?.string())
        response.close()
    }

    @Test
    fun testIsReachable() {
        // Setup mock response for reachability test
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
        )

        val url = mockWebServer.url("/health").toString()
        
        // Test reachability
        val isReachable = httpService.isReachable(url)
        
        assertTrue(isReachable)
        
        // Verify HEAD request was made
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("HEAD", recordedRequest.method)
    }

    @Test
    fun testIsNotReachable() {
        // Don't enqueue any response, causing a connection error
        val url = "http://localhost:9999/nonexistent"
        
        // Test reachability to non-existent server
        val isReachable = httpService.isReachable(url)
        
        assertFalse(isReachable)
    }

    @Test
    fun testCustomTimeout() {
        // Setup slow response
        mockWebServer.enqueue(
            MockResponse()
                .setBody("Slow response")
                .setResponseCode(200)
                .setBodyDelay(100, java.util.concurrent.TimeUnit.MILLISECONDS)
        )

        val url = mockWebServer.url("/slow").toString()
        
        // Make request with custom timeout (should succeed)
        val response = httpService.makeRequest(
            url = url,
            method = "GET",
            timeoutMs = 5000 // 5 second timeout
        )
        
        assertEquals(200, response.code)
        assertEquals("Slow response", response.body?.string())
        response.close()
    }
}
