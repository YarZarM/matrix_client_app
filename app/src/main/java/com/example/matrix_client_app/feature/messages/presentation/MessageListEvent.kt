package com.example.matrix_client_app.feature.messages.presentation

sealed class MessageListEvent {
    object LoadMessages: MessageListEvent()

    object RefreshMessages: MessageListEvent()

    object DismissError: MessageListEvent()
}