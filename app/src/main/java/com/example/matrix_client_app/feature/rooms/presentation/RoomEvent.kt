package com.example.matrix_client_app.feature.rooms.presentation

sealed class RoomEvent {

    object LoadRooms : RoomEvent()

    object RefreshRooms : RoomEvent()

    data class JoinRoom(val roomId: String) : RoomEvent()

    data class OpenRoom(val roomId: String) : RoomEvent()

    object DismissError : RoomEvent()

    object Logout: RoomEvent()
}
