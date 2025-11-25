package com.example.matrix_client_app.feature.messages.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matrix_client_app.feature.messages.domain.MessageRepository
import com.example.matrix_client_app.util.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MessageListViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val roomId: String = checkNotNull(savedStateHandle["roomId"]) {
        "roomId is required for MessageListViewModel"
    }

    private val _state = MutableStateFlow(MessageListState(roomId = roomId))
    val state: StateFlow<MessageListState> = _state.asStateFlow()

    init {
        // Load messages when ViewModel is created
        loadMessages()
    }

    fun onEvent(event: MessageListEvent) {
        when (event) {
            is MessageListEvent.LoadMessages -> loadMessages()
            is MessageListEvent.RefreshMessages -> refreshMessages()
            is MessageListEvent.DismissError -> dismissError()
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _state.update { it.copy(
                isLoading = true,
                error = null
            )}

            Timber.d("Loading messages for room: $roomId")

            when (val result = messageRepository.getRoomMessages(roomId, limit = 50)) {
                is DataResult.Success -> {
                    Timber.d("Loaded ${result.data.size} messages")
                    _state.update { it.copy(
                        messages = result.data,
                        isLoading = false,
                        // Set room name to room ID for now (we'd need another API call to get real name)
                        roomName = roomId.substringAfter("!").substringBefore(":")
                    )}
                }

                is DataResult.Error -> {
                    Timber.e("Failed to load messages: ${result.message}")
                    _state.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }

    private fun refreshMessages() {
        viewModelScope.launch {
            _state.update { it.copy(
                isRefreshing = true,
                error = null
            )}

            Timber.d("Refreshing messages for room: $roomId")

            when (val result = messageRepository.getRoomMessages(roomId, limit = 50)) {
                is DataResult.Success -> {
                    Timber.d("Refreshed ${result.data.size} messages")
                    _state.update { it.copy(
                        messages = result.data,
                        isRefreshing = false
                    )}
                }

                is DataResult.Error -> {
                    Timber.e("Failed to refresh messages: ${result.message}")
                    _state.update { it.copy(
                        isRefreshing = false,
                        error = result.message
                    )}
                }
            }
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}