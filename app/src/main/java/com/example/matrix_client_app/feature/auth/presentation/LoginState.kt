package com.example.matrix_client_app.feature.auth.presentation

import com.example.matrix_client_app.util.Constants

data class LoginState(

    val homeserverUrl: String = Constants.DEFAULT_HOMESERVER,

    val username: String = Constants.USERNAME,

    val password: String = Constants.PASSWORD,

    val isLoading: Boolean = false,

    val error: String? = null,

    val isSuccess: Boolean = false
) {
    fun isValid(): Boolean {
        return homeserverUrl.isNotBlank() &&
                username.isNotBlank() &&
                password.isNotBlank()
    }
}