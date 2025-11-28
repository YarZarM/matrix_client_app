package com.example.matrix_client_app.feature.auth.domain.repository

import com.example.matrix_client_app.util.DataResult

interface AuthRepository {

    suspend fun login(
        homeserverUrl: String,
        username: String,
        password: String
    ): DataResult<Unit>

    suspend fun logout(): DataResult<Unit>
}
