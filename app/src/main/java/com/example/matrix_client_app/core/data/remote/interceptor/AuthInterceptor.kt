package com.example.matrix_client_app.core.data.remote.interceptor

import com.example.matrix_client_app.core.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.url.encodedPath.contains("login")) {
            return chain.proceed(originalRequest)
        }

        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            tokenManager.getAccessToken()
        }

        if (token == null) {
            Timber.w("No access token available")
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        Timber.d("Added auth token to request: ${originalRequest.url}")

        return chain.proceed(authenticatedRequest)
    }
}