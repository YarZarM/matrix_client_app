package com.example.matrix_client_app.feature.rooms.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matrix_client_app.core.data.local.JoinedRoomsManager
import com.example.matrix_client_app.core.data.local.TokenManager
import com.example.matrix_client_app.feature.rooms.domain.repository.RoomRepository
import com.example.matrix_client_app.util.DataResult
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
    private val roomRepository: RoomRepository,
    private val joinedRoomsManager: JoinedRoomsManager,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _state = MutableStateFlow(RoomState())
    val state: StateFlow<RoomState> = _state.asStateFlow()

    private val _navigationEvents = Channel<RoomListNavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    init {
        loadJoinedRoomsFromStorage()
        loadRooms()
    }

    private fun loadJoinedRoomsFromStorage() {
        viewModelScope.launch {
            Timber.d("Loading joined rooms from storage...")
            val joinedRoomIds = joinedRoomsManager.getJoinedRoomIds()
            Timber.d("Found ${joinedRoomIds.size} previously joined rooms")

            _state.update { it.copy(
                joinedRoomIds = joinedRoomIds
            )}
        }
    }

    fun onEvent(event: RoomEvent) {
        when (event) {
            is RoomEvent.LoadRooms -> loadRooms()
            is RoomEvent.RefreshRooms -> refreshRooms()
            is RoomEvent.JoinRoom -> joinRoom(event.roomId)
            is RoomEvent.OpenRoom -> openRoom(event.roomId)
            is RoomEvent.DismissError -> dismissError()
            is RoomEvent.Logout -> logoutRoom()
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

    private fun refreshRooms() {
        _state.update { it.copy(
            isRefreshing = true,
            error = null
        )}

        viewModelScope.launch {
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
                is DataResult.Success -> {
                    Timber.d("Successfully joined room: $roomId")

                    _state.update { currentState ->
                        currentState.copy(
                            joinedRoomIds = currentState.joinedRoomIds + roomId
                        )
                    }

                    joinedRoomsManager.addJoinedRoom(roomId)

                    val room = _state.value.rooms.find { it.roomId == roomId }
                    val roomName = room?.getDisplayName() ?: roomId

                    _navigationEvents.send(
                        RoomListNavigationEvent.NavigateToRoom(roomId = roomId, roomName = roomName)
                    )
                }

                is DataResult.Error -> {
                    Timber.e("Failed to join room: ${result.message}")
                    _state.update { it.copy(
                        error = result.message
                    )}
                }
            }
        }
    }

    private fun openRoom(roomId: String) {
        viewModelScope.launch {
            Timber.d("Opening room: $roomId")

            val room = _state.value.rooms.find { it.roomId == roomId }
            val roomName = room?.getDisplayName() ?: roomId

            _navigationEvents.send(
                RoomListNavigationEvent.NavigateToRoom(roomId = roomId, roomName = roomName)
            )
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun logoutRoom() {
        viewModelScope.launch {
            try {
                Timber.d("Logging out user...")

                tokenManager.clearAll()

                joinedRoomsManager.clearJoinedRooms()

                Timber.d("Logout successful")

                _navigationEvents.send(RoomListNavigationEvent.NavigateToLogin)

            } catch (e: Exception) {
                Timber.e(e, "Error during logout")
                _navigationEvents.send(RoomListNavigationEvent.NavigateToLogin)
            }
        }
    }
}

sealed class RoomListNavigationEvent {
    data class NavigateToRoom(val roomId: String, val roomName: String) : RoomListNavigationEvent()
    object NavigateToLogin : RoomListNavigationEvent()
}