package com.example.matrix_client_app.feature.auth.presentation

sealed class LoginEvent {

    object Login : LoginEvent()

    data class UpdateHomeserverUrl(val url: String) : LoginEvent()

    data class UpdateUsername(val username: String) : LoginEvent()

    data class UpdatePassword(val password: String) : LoginEvent()

    object DismissError : LoginEvent()
}

sealed class LoginNavigationEvent {
    object NavigateToRoomList : LoginNavigationEvent()
}
