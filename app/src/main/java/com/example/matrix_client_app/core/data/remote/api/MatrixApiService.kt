package com.example.matrix_client_app.core.data.remote.api

import com.example.matrix_client_app.feature.auth.data.model.LoginRequest
import com.example.matrix_client_app.feature.auth.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface MatrixApiService {

    @POST
    suspend fun login(
        @Url baseUrl: String,
        @Body request: LoginRequest
    ): LoginResponse

    // TODO: Add more endpoints as needed
    // - getPublicRooms()
    // - joinRoom()
    // - getRoomMessages()
    // - sendMessage()
}