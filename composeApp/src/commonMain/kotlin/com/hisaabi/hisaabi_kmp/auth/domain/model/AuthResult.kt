package com.hisaabi.hisaabi_kmp.auth.domain.model

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

inline fun <T> AuthResult<T>.onSuccess(action: (T) -> Unit): AuthResult<T> {
    if (this is AuthResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> AuthResult<T>.onError(action: (String) -> Unit): AuthResult<T> {
    if (this is AuthResult.Error) {
        action(message)
    }
    return this
}

inline fun <T> AuthResult<T>.onLoading(action: () -> Unit): AuthResult<T> {
    if (this is AuthResult.Loading) {
        action()
    }
    return this
}
