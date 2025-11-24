package com.example.matrix_client_app.feature.rooms.domain.repository

import com.example.matrix_client_app.feature.rooms.data.model.PublicRoom
import com.example.matrix_client_app.util.DataResult
import com.example.matrix_client_app.util.Result

interface RoomRepository {

    suspend fun getPublicRooms(limit: Int? = 20): DataResult<List<PublicRoom>>

    suspend fun joinRoom(roomId: String): Result
}