# LoggingInterceptor Implementation

## Overview

A comprehensive HTTP logging interceptor has been implemented to provide detailed logging of all API calls in the Hisaabi KMP application. This interceptor automatically logs all HTTP requests and responses with detailed information.

## Features

### 🚀 Request Logging
- **URL and HTTP Method**: Complete endpoint information
- **Headers**: All request headers (with sensitive data masked)
- **Request Body**: Content type, length, and body data
- **Timestamps**: Precise timing information

### 📥 Response Logging
- **Status Code**: HTTP status and description
- **Response Headers**: All response headers
- **Response Body**: Full response content (truncated if too large)
- **Timing Information**: Request timeout details

### 🔒 Security Features
- **Authorization Header Masking**: Bearer tokens are masked for security
- **Sensitive Data Protection**: Basic auth and other sensitive headers are protected

## Implementation

### Files Created/Modified

1. **`LoggingInterceptor.kt`** - Main interceptor implementation
2. **`AuthModule.kt`** - Updated to use the new interceptor
3. **`LoggingInterceptorTest.kt`** - Test class for demonstration

### Integration

The LoggingInterceptor is automatically installed in the main HttpClient configuration in `AuthModule.kt`. Since all modules share the same HttpClient instance, logging is applied to all API calls throughout the application.

```kotlin
// In AuthModule.kt
HttpClient {
    // ... other configurations
    
    // Install custom logging interceptor for detailed API logging
    install(LoggingInterceptor().createPlugin())
    
    // ... other configurations
}
```

## Log Output Format

### Request Logs
```
==================================================
🚀 API_LOGGER - REQUEST [2024-01-15T10:30:45.123]
------------------------------
📍 URL: http://52.20.167.4:5000/login
🔧 Method: POST
📋 Headers:
   Content-Type: application/json
   Authorization: Bearer abc12345...xyz9
📦 Content-Type: application/json
📏 Content-Length: 45
------------------------------
```

### Response Logs
```
==================================================
📥 API_LOGGER - RESPONSE [2024-01-15T10:30:45.456]
------------------------------
📍 URL: http://52.20.167.4:5000/login
🔧 Method: POST
📊 Status: 200 OK
📋 Response Headers:
   Content-Type: application/json
   Set-Cookie: session=abc123
📦 Response Body:
{"success": true, "token": "jwt_token_here", "user": {...}}
------------------------------
==================================================
```

## Usage

The interceptor works automatically once installed. No additional configuration is needed. All API calls will be logged with the detailed format shown above.

### Testing

To test the logging functionality, you can use the provided test class:

```kotlin
val test = LoggingInterceptorTest()
test.runTest()
```

## Benefits

1. **Debugging**: Easy to debug API issues with complete request/response information
2. **Monitoring**: Track all API calls and their performance
3. **Security**: Sensitive data is automatically masked
4. **Development**: Better development experience with detailed logs
5. **Troubleshooting**: Quick identification of API problems

## Configuration

The interceptor can be customized by modifying the `LoggingInterceptor.kt` file:

- **Log Level**: Modify the logging level in the interceptor
- **Body Truncation**: Adjust the response body truncation limit (currently 1000 characters)
- **Header Masking**: Add more headers to the masking list
- **Timestamp Format**: Customize the timestamp format

## Notes

- The interceptor is applied to all HTTP clients in the application
- Logs are printed to the console using `println()`
- Response bodies are truncated to prevent huge log outputs
- Authorization headers are automatically masked for security
- The interceptor runs on the IO dispatcher to avoid blocking the main thread
