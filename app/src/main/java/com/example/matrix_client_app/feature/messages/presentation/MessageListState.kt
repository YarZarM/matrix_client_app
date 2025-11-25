package com.example.matrix_client_app.feature.messages.presentation

import com.example.matrix_client_app.feature.messages.data.model.ClientEvent

data class MessageListState(
    val messages: List<ClientEvent> = emptyList(),
    val roomId: String = "",
    val roomName: String = "",
    val isLoading: Boolean = false,
    val error: String ?= null,
    val isRefreshing: Boolean = false
)