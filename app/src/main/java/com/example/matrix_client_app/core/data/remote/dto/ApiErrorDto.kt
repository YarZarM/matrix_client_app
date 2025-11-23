package com.example.matrix_client_app.core.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiErrorDto(
    @Json(name = "errcode")
    val errorCode: String?,

    @Json(name = "error")
    val errorMessage: String?
)