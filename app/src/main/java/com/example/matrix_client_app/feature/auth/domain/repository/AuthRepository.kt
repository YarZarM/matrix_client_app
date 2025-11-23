package com.example.matrix_client_app.feature.auth.domain.repository

import com.example.matrix_client_app.util.Result

interface AuthRepository {

    suspend fun login(
        homeserverUrl: String,
        username: String,
        password: String
    ): Result

    suspend fun logout(): Result
}
