package com.example.matrix_client_app.feature.rooms.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matrix_client_app.feature.rooms.domain.repository.RoomRepository
import com.example.matrix_client_app.util.DataResult
import com.example.matrix_client_app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    // UI State
    private val _state = MutableStateFlow(RoomState())
    val state: StateFlow<RoomState> = _state.asStateFlow()

    // Navigation events
    private val _navigationEvents = Channel<RoomListNavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    init {
        // Load rooms when ViewModel is created
        loadRooms()
    }

    fun onEvent(event: RoomEvent) {
        when (event) {
            is RoomEvent.LoadRooms -> loadRooms()
            is RoomEvent.RefreshRooms -> refreshRooms()
            is RoomEvent.JoinRoom -> joinRoom(event.roomId)
            is RoomEvent.OpenRoom -> openRoom(event.roomId)
            is RoomEvent.DismissError -> dismissError()
        }
    }

    private fun loadRooms() {
        viewModelScope.launch {
            _state.update { it.copy(
                isLoading = true,
                error = null
            )}

            Timber.d("Loading public rooms...")

            when (val result = roomRepository.getPublicRooms(limit = 20)) {
                is DataResult.Success -> {
                    Timber.d("Loaded ${result.data.size} rooms")
                    _state.update { it.copy(
                        rooms = result.data,
                        isLoading = false
                    )}
                }

                is DataResult.Error -> {
                    Timber.e("Failed to load rooms: ${result.message}")
                    _state.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }

    /**
     * Refresh rooms (pull to refresh)
     */
    private fun refreshRooms() {
        viewModelScope.launch {
            _state.update { it.copy(
                isRefreshing = true,
                error = null
            )}

            Timber.d("Refreshing rooms...")

            when (val result = roomRepository.getPublicRooms(limit = 20)) {
                is DataResult.Success -> {
                    Timber.d("Refreshed ${result.data.size} rooms")
                    _state.update { it.copy(
                        rooms = result.data,
                        isRefreshing = false
                    )}
                }

                is DataResult.Error -> {
                    Timber.e("Failed to refresh rooms: ${result.message}")
                    _state.update { it.copy(
                        isRefreshing = false,
                        error = result.message
                    )}
                }
            }
        }
    }

    private fun joinRoom(roomId: String) {
        viewModelScope.launch {
            Timber.d("Joining room: $roomId")

            when (val result = roomRepository.joinRoom(roomId)) {
                is Result.Success -> {
                    Timber.d("Successfully joined room: $roomId")

                    // Add to joined rooms set
                    _state.update { currentState ->
                        currentState.copy(
                            joinedRoomIds = currentState.joinedRoomIds + roomId
                        )
                    }

                    // Navigate to the room
                    _navigationEvents.send(
                        RoomListNavigationEvent.NavigateToRoom(roomId)
                    )
                }

                is Result.Error -> {
                    Timber.e("Failed to join room: ${result.message}")
                    _state.update { it.copy(
                        error = result.message
                    )}
                }
            }
        }
    }

    /**
     * Open a room (navigate to messages screen)
     *
     * @param roomId The room ID to open
     */
    private fun openRoom(roomId: String) {
        viewModelScope.launch {
            Timber.d("Opening room: $roomId")
            _navigationEvents.send(
                RoomListNavigationEvent.NavigateToRoom(roomId)
            )
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}

sealed class RoomListNavigationEvent {
    data class NavigateToRoom(val roomId: String) : RoomListNavigationEvent()
}