package com.example.matrix_client_app.feature.auth.data.repository

import com.example.matrix_client_app.core.data.local.TokenManager
import com.example.matrix_client_app.core.data.remote.api.MatrixApiService
import com.example.matrix_client_app.feature.auth.data.model.LoginRequest
import com.example.matrix_client_app.feature.auth.domain.repository.AuthRepository
import com.example.matrix_client_app.util.DataResult
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
    ): DataResult<Unit> {
        return try {
            val request = LoginRequest(
                type = "m.login.password",
                user = username,
                password = password
            )

            val fullUrl = "$homeserverUrl/_matrix/client/v3/login"

            val response = api.login(
                baseUrl = fullUrl,
                request = request
            )

            tokenManager.saveAccessToken(response.accessToken)
            tokenManager.saveUserId(response.userId)
            tokenManager.saveHomeserverUrl(homeserverUrl)

            DataResult.Success(Unit)

        } catch (e: HttpException) {
            when (e.code()) {
                401, 403 -> DataResult.Error("Invalid username or password")
                429 -> DataResult.Error("Too many attempts. Please try again later")
                500, 502, 503 -> DataResult.Error("Server error. Please try again later")
                else -> DataResult.Error("Login failed: ${e.message()}")
            }
        } catch (e: IOException) {
            DataResult.Error("Network error. Please check your connection")
        } catch (e: Exception) {
            DataResult.Error("Login failed: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun logout(): DataResult<Unit> {
        return try {
            tokenManager.clearAll()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Success(Unit)
        }
    }
}