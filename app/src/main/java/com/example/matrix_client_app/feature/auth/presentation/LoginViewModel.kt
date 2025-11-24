package com.example.matrix_client_app.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matrix_client_app.feature.auth.domain.repository.AuthRepository
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // UI State (survives rotation)
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    // Navigation events (one-time, doesn't survive rotation)
    private val _navigationEvents = Channel<LoginNavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UpdateHomeserverUrl -> updateHomeserverUrl(event.url)
            is LoginEvent.UpdateUsername -> updateUsername(event.username)
            is LoginEvent.UpdatePassword -> updatePassword(event.password)
            is LoginEvent.Login -> login()
            is LoginEvent.DismissError -> dismissError()
        }
    }

    private fun updateHomeserverUrl(url: String) {
        _state.update { it.copy(homeserverUrl = url) }
    }

    private fun updateUsername(username: String) {
        _state.update { it.copy(username = username) }
    }

    private fun updatePassword(password: String) {
        _state.update { it.copy(password = password) }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun login() {
        viewModelScope.launch {
            val currentState = _state.value

            // Step 1: Validate input
            val validationError = validateInput(
                homeserverUrl = currentState.homeserverUrl,
                username = currentState.username,
                password = currentState.password
            )

            if (validationError != null) {
                _state.update { it.copy(error = validationError) }
                return@launch
            }

            // Step 2: Show loading
            _state.update { it.copy(
                isLoading = true,
                error = null
            )}

            Timber.d("Attempting login for user: ${currentState.username}")

            // Step 3: Call repository
            val result = authRepository.login(
                homeserverUrl = cleanHomeserverUrl(currentState.homeserverUrl),
                username = currentState.username,
                password = currentState.password
            )

            // Step 4: Handle result
            when (result) {
                is Result.Success -> {
                    Timber.d("Login successful!")

                    _state.update { it.copy(
                        isLoading = false,
                        isSuccess = true
                    )}

                    // Step 5: Navigate to room list
                    _navigationEvents.send(LoginNavigationEvent.NavigateToRoomList)
                }

                is Result.Error -> {
                    Timber.e("Login failed: ${result.message}")

                    _state.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }

    private fun validateInput(
        homeserverUrl: String,
        username: String,
        password: String
    ): String? {
        return when {
            homeserverUrl.isBlank() ->
                "Homeserver URL is required"

            !homeserverUrl.startsWith("http://") && !homeserverUrl.startsWith("https://") ->
                "Homeserver URL must start with http:// or https://"

            username.isBlank() ->
                "Username is required"

            username.length < 2 ->
                "Username must be at least 2 characters"

            password.isBlank() ->
                "Password is required"

            else -> null // All valid!
        }
    }

    private fun cleanHomeserverUrl(url: String): String {
        return url.trim()           // Remove spaces
            .removeSuffix("/")      // Remove trailing slash
            .removeSuffix("/")      // Remove double slash
    }
}

//sealed class LoginNavigationEvent {
//    object NavigateToRoomList : LoginNavigationEvent()
//}