package com.hisaabi.hisaabi_kmp.network.interceptor

import io.ktor.client.*
import io.ktor.client.plugins.api.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Custom LoggingInterceptor for detailed API call logging
 * Provides comprehensive logging of HTTP requests and responses
 */
class LoggingInterceptor {
    
    companion object {
        private const val LOG_TAG = "API_LOGGER"
        private const val SEPARATOR = "=================================================="
        private const val REQUEST_SEPARATOR = "------------------------------"
        private const val RESPONSE_SEPARATOR = "------------------------------"
    }
    
    /**
     * Creates a Ktor client plugin for logging HTTP requests and responses
     */
    fun createPlugin(): ClientPlugin<Unit> = createClientPlugin("LoggingInterceptor") {
        onRequest { request, _ ->
            logRequest(request)
        }
        
        onResponse { response ->
            logResponse(response)
        }
    }
    
    /**
     * Logs the outgoing HTTP request
     */
    private suspend fun logRequest(request: HttpRequestBuilder) {
        val timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        println("\n$SEPARATOR")
        println("🚀 $LOG_TAG - REQUEST [$timestamp]")
        println("$REQUEST_SEPARATOR")
        println("📍 URL: ${request.url}")
        println("🔧 Method: ${request.method}")
        
        // Log headers
        if (request.headers.names().isNotEmpty()) {
            println("📋 Headers:")
            request.headers.names().forEach { name ->
                val values = request.headers.getAll(name)
                values?.forEach { value ->
                    // Mask sensitive headers
                    val maskedValue = if (name.equals("Authorization", ignoreCase = true)) {
                        maskAuthorizationHeader(value)
                    } else {
                        value
                    }
                    println("   $name: $maskedValue")
                }
            }
        }
        
        // Log body if present
        request.body?.let { body ->
            when (body) {
                is OutgoingContent -> {
                    println("📦 Content-Type: ${body.contentType}")
                    println("📏 Content-Length: ${body.contentLength}")
                }
                else -> {
                    println("📦 Body: $body")
                }
            }
        }
        
        println("$REQUEST_SEPARATOR")
    }
    
    /**
     * Logs the incoming HTTP response
     */
    private suspend fun logResponse(response: HttpResponse) {
        val timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val request = response.request
        
        println("\n$SEPARATOR")
        println("📥 $LOG_TAG - RESPONSE [$timestamp]")
        println("$RESPONSE_SEPARATOR")
        println("📍 URL: ${request.url}")
        println("🔧 Method: ${request.method}")
        println("📊 Status: ${response.status.value} ${response.status.description}")
        
        // Log response headers
        if (response.headers.names().isNotEmpty()) {
            println("📋 Response Headers:")
            response.headers.names().forEach { name ->
                val values = response.headers.getAll(name)
                values?.forEach { value ->
                    println("   $name: $value")
                }
            }
        }

        val charToPrintInLogs = 10000
        // Log response body (first 1000 characters to avoid huge logs)
        try {
            val responseText = response.bodyAsText()
            val truncatedBody = if (responseText.length > charToPrintInLogs) {
                "${responseText.take(charToPrintInLogs)}... [truncated ${responseText.length - charToPrintInLogs} more characters]"
            } else {
                responseText
            }
            println("📦 Response Body:")
            println(truncatedBody)
        } catch (e: Exception) {
            println("📦 Response Body: [Unable to read response body: ${e.message}]")
        }
        
        // Log timing information if available
        // Note: Timing information can be added here if needed in the future
        
        println("$RESPONSE_SEPARATOR")
        println("$SEPARATOR\n")
    }
    
    /**
     * Masks authorization header for security
     */
    private fun maskAuthorizationHeader(authHeader: String): String {
        return when {
            authHeader.startsWith("Bearer ") -> {
                val token = authHeader.substring(7)
                "Bearer ${token.take(8)}...${token.takeLast(4)}"
            }
            authHeader.startsWith("Basic ") -> {
                "Basic [MASKED]"
            }
            else -> {
                "[MASKED]"
            }
        }
    }
}

/**
 * Extension function to easily add logging to HttpClient
 */
fun HttpClientConfig<*>.installLoggingInterceptor() {
    install(LoggingInterceptor().createPlugin())
}
