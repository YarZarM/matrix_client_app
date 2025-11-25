package com.example.matrix_client_app.feature.messages.data.repository

import com.example.matrix_client_app.core.data.local.TokenManager
import com.example.matrix_client_app.core.data.remote.api.MatrixApiService
import com.example.matrix_client_app.feature.messages.data.model.ClientEvent
import com.example.matrix_client_app.feature.messages.domain.MessageRepository
import com.example.matrix_client_app.util.DataResult
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val api: MatrixApiService,
    private val tokenManager: TokenManager
) : MessageRepository {

    override suspend fun getRoomMessages(
        roomId: String,
        limit: Int
    ): DataResult<List<ClientEvent>> {
        return try {
            val accessToken = tokenManager.getAccessToken()
            val homeserverUrl = tokenManager.getHomeserverUrl()
            val authHeader = "Bearer $accessToken"
            val fullUrl = "$homeserverUrl/_matrix/client/v3/rooms/$roomId/messages"

            val response = api.getMessageList(
                baseUrl = fullUrl,
                authorization = authHeader,
                from = null,
                dir = "b",
                limit = limit
            )

            val messages = response.chunk.reversed()

            DataResult.Success(messages)

        } catch (e: HttpException) {
            when (e.code()) {
                401 -> DataResult.Error("Session expired. Please log in again")
                403 -> DataResult.Error("You don't have permission to view this room")
                404 -> DataResult.Error("Room not found")
                429 -> DataResult.Error("Too many requests. Try again later")
                else -> DataResult.Error("Failed to load messages: ${e.message()}")
            }
        } catch (e: IOException) {
            DataResult.Error("Network error. Check your connection")
        } catch (e: Exception) {
            DataResult.Error("Failed to load messages: ${e.message ?: "Unknown error"}")
        }
    }
}