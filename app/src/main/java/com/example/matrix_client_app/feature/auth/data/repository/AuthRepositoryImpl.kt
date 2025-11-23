package com.example.matrix_client_app.feature.auth.data.repository

import com.example.matrix_client_app.core.data.local.TokenManager
import com.example.matrix_client_app.core.data.remote.api.MatrixApiService
import com.example.matrix_client_app.feature.auth.data.model.LoginRequest
import com.example.matrix_client_app.feature.auth.domain.repository.AuthRepository
import com.example.matrix_client_app.util.Result
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: MatrixApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(
        homeserverUrl: String,
        username: String,
        password: String
    ): Result {
        return try {
            // Step 1: Create request body
            val request = LoginRequest(
                type = "m.login.password",
                user = username,
                password = password
            )

            // Step 2: Build full URL
            // Example: https://matrix.org/_matrix/client/v3/login
            val fullUrl = "$homeserverUrl/_matrix/client/v3/login"

            // Step 3: Make API call (suspend function - waits for response)
            val response = api.login(
                baseUrl = fullUrl,
                request = request
            )

            // Step 4: Save tokens securely
            tokenManager.saveAccessToken(response.accessToken)
            tokenManager.saveUserId(response.userId)
            tokenManager.saveHomeserverUrl(homeserverUrl)

            // Step 5: Return success
            Result.Success

        } catch (e: HttpException) {
            // API returned error (401, 403, 500, etc.)
            when (e.code()) {
                401, 403 -> Result.Error("Invalid username or password")
                429 -> Result.Error("Too many attempts. Please try again later")
                500, 502, 503 -> Result.Error("Server error. Please try again later")
                else -> Result.Error("Login failed: ${e.message()}")
            }
        } catch (e: IOException) {
            // Network error (no internet, timeout, etc.)
            Result.Error("Network error. Please check your connection")
        } catch (e: Exception) {
            // Any other unexpected error
            Result.Error("Login failed: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun logout(): Result {
        return try {
            tokenManager.clearAll()
            Result.Success
        } catch (e: Exception) {
            // Even if clearing fails, treat as success
            // User can't be logged in without token
            Result.Success
        }
    }
}