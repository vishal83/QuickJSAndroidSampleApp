package com.quickjs.android

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Network service for downloading JavaScript code from remote URLs
 */
class NetworkService {
    
    companion object {
        private const val TAG = "NetworkService"
        private const val TIMEOUT_MS = 30000 // 30 seconds
        private const val MAX_CONTENT_LENGTH = 10 * 1024 * 1024 // 10MB limit
    }
    
    /**
     * Download text content from a URL
     */
    suspend fun downloadText(url: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Downloading from: $url")
            
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("User-Agent", "QuickJS-Android/1.0")
                setRequestProperty("Accept", "application/javascript, text/javascript, */*")
            }
            
            val responseCode = connection.responseCode
            Log.i(TAG, "Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val contentLength = connection.contentLength
                if (contentLength > MAX_CONTENT_LENGTH) {
                    Log.e(TAG, "Content too large: $contentLength bytes (max: $MAX_CONTENT_LENGTH)")
                    return@withContext null
                }
                
                val content = connection.inputStream.bufferedReader().use { it.readText() }
                Log.i(TAG, "Downloaded ${content.length} characters")
                content
            } else {
                Log.e(TAG, "HTTP error: $responseCode - ${connection.responseMessage}")
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error downloading from $url", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error downloading from $url", e)
            null
        }
    }
    
    /**
     * Check if a URL is reachable
     */
    suspend fun isUrlReachable(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "HEAD"
                connectTimeout = 5000 // 5 seconds for connectivity check
                readTimeout = 5000
            }
            
            val responseCode = connection.responseCode
            responseCode in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "URL not reachable: $url", e)
            false
        }
    }
}
