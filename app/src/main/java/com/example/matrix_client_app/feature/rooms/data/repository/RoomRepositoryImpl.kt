package com.example.matrix_client_app.feature.rooms.data.repository

import com.example.matrix_client_app.core.data.local.TokenManager
import com.example.matrix_client_app.core.data.remote.api.MatrixApiService
import com.example.matrix_client_app.feature.rooms.domain.repository.RoomRepository
import com.example.matrix_client_app.feature.rooms.data.model.PublicRoom
import com.example.matrix_client_app.util.DataResult
import okio.IOException
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val api: MatrixApiService,
    private val tokenManager: TokenManager
): RoomRepository {

    override suspend fun getPublicRooms(limit: Int?): DataResult<List<PublicRoom>> {
        return try {
            val homeserverUrl = tokenManager.getHomeserverUrl()
            val fullUrl = "$homeserverUrl/_matrix/client/v3/publicRooms"
            Timber.d("getPublicRooms: homeserverUrl from storage = '$homeserverUrl'")
            Timber.d("getPublicRooms: Full URL = '$fullUrl'")

            val response = api.getPublciRooms(
                baseUrl = fullUrl,
                limit = limit
            )

            DataResult.Success(response.chunk)
        } catch (e: Exception) {
            DataResult.Error("Failed to load rooms: ${e.message ?: "UnknownError"}")
        }
    }

    override suspend fun joinRoom(roomId: String): DataResult<Unit> {
        return try {
            val accessToken = tokenManager.getAccessToken()
            val homeserverUrl = tokenManager.getHomeserverUrl()
            val authHeader = "Bearer $accessToken"

            val fullUrl = "$homeserverUrl/_matrix/client/v3/rooms/${roomId}/join"

            api.joinPublicRooms(
                baseUrl = fullUrl,
                authorization = authHeader
            )

            DataResult.Success(Unit)
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> DataResult.Error("Session expired. Please log in again")
                403 -> DataResult.Error("You are not allowed to join this room")
                404 -> DataResult.Error("Room not found")
                429 -> DataResult.Error("Too many requests. Try again later")
                else -> DataResult.Error("Failed to join room: ${e.message()}")
            }
        } catch (e: IOException) {
            DataResult.Error("Network error. Check your connection")
        } catch (e: Exception) {
            DataResult.Error("Failed to join room: ${e.message ?: "Unknown error"}")
        }
    }
}