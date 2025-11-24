package com.example.matrix_client_app.feature.rooms.presentation

import com.example.matrix_client_app.feature.rooms.data.model.PublicRoom

data class RoomState(
    val rooms: List<PublicRoom> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val joinedRoomIds: Set<String> = emptySet()
)