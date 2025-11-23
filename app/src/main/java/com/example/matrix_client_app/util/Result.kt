package com.example.matrix_client_app.util

sealed class Result {

    object Success : Result()

    data class Error(val message: String) : Result()
}

sealed class DataResult<out T> {

    data class Success<T>(val data: T) : DataResult<T>()

    data class Error<T>(val message: String) : DataResult<T>()
}
