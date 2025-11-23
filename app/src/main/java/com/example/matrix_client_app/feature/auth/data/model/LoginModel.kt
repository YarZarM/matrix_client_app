package com.example.matrix_client_app.feature.auth.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "type")
    val type: String = "m.login.password",

    @Json(name = "user")
    val user: String,

    @Json(name = "password")
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "user_id")
    val userId: String,

    @Json(name = "access_token")
    val accessToken: String,

    @Json(name = "device_id")
    val deviceId: String? = null
)