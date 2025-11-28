package com.example.matrix_client_app.util

sealed class DataResult<out T> {

    data class Success<T>(val data: T) : DataResult<T>()

    data class Error<T>(val message: String, val isAuthError: Boolean = false) : DataResult<T>()
}
