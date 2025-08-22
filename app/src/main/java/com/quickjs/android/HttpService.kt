package com.quickjs.android

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * HTTP service for making network requests from JavaScript
 * Provides HTTP functionality for fetch() and XMLHttpRequest polyfills
 */
class HttpService {
    private val TAG = "HttpService"
    
    // OkHttp client with reasonable timeouts
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Make an HTTP request
     * @param url The URL to request
     * @param method HTTP method (GET, POST, etc.)
     * @param headers Request headers
     * @param body Request body (for POST/PUT requests)
     * @param timeoutMs Request timeout in milliseconds
     * @return Response object
     */
    fun makeRequest(
        url: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        timeoutMs: Int = 30000
    ): Response {
        Log.d(TAG, "Making $method request to: $url")
        
        try {
            // Create request builder
            val requestBuilder = Request.Builder()
                .url(url)
            
            // Add headers
            headers.forEach { (name, value) ->
                requestBuilder.addHeader(name, value)
            }
            
            // Set method and body
            when (method.uppercase()) {
                "GET" -> requestBuilder.get()
                "POST" -> {
                    val requestBody = body?.toRequestBody("application/json".toMediaType())
                        ?: "".toRequestBody("text/plain".toMediaType())
                    requestBuilder.post(requestBody)
                }
                "PUT" -> {
                    val requestBody = body?.toRequestBody("application/json".toMediaType())
                        ?: "".toRequestBody("text/plain".toMediaType())
                    requestBuilder.put(requestBody)
                }
                "DELETE" -> requestBuilder.delete()
                "PATCH" -> {
                    val requestBody = body?.toRequestBody("application/json".toMediaType())
                        ?: "".toRequestBody("text/plain".toMediaType())
                    requestBuilder.patch(requestBody)
                }
                "HEAD" -> requestBuilder.head()
                else -> requestBuilder.get() // Default to GET for unknown methods
            }
            
            // Create client with custom timeout if needed
            val requestClient = if (timeoutMs != 30000) {
                client.newBuilder()
                    .connectTimeout(timeoutMs.toLong(), TimeUnit.MILLISECONDS)
                    .readTimeout(timeoutMs.toLong(), TimeUnit.MILLISECONDS)
                    .writeTimeout(timeoutMs.toLong(), TimeUnit.MILLISECONDS)
                    .build()
            } else {
                client
            }
            
            // Execute request
            val request = requestBuilder.build()
            val response = requestClient.newCall(request).execute()
            
            Log.d(TAG, "Request completed: ${response.code} ${response.message}")
            return response
            
        } catch (e: Exception) {
            Log.e(TAG, "HTTP request failed: $url", e)
            throw e
        }
    }

    /**
     * Make a simple GET request
     * @param url The URL to request
     * @param headers Optional headers
     * @return Response body as string
     */
    fun get(url: String, headers: Map<String, String> = emptyMap()): String {
        return makeRequest(url, "GET", headers).use { response ->
            response.body?.string() ?: ""
        }
    }

    /**
     * Make a simple POST request
     * @param url The URL to request
     * @param body Request body
     * @param headers Optional headers
     * @return Response body as string
     */
    fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): String {
        return makeRequest(url, "POST", headers, body).use { response ->
            response.body?.string() ?: ""
        }
    }

    /**
     * Check if a URL is reachable
     * @param url The URL to check
     * @return true if the URL returns a successful response
     */
    fun isReachable(url: String): Boolean {
        return try {
            makeRequest(url, "HEAD").use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.w(TAG, "URL not reachable: $url", e)
            false
        }
    }
}
