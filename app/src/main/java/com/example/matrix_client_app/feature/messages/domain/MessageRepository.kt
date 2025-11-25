package com.example.matrix_client_app.feature.messages.domain

import com.example.matrix_client_app.feature.messages.data.model.ClientEvent
import com.example.matrix_client_app.util.DataResult

interface MessageRepository {
    suspend fun getRoomMessages(
        roomId: String,
        limit: Int = 50
    ): DataResult<List<ClientEvent>>
}