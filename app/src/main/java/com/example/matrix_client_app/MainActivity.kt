package com.example.matrix_client_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.matrix_client_app.core.data.local.TokenManager
import com.example.matrix_client_app.navigation.NavGraph
import com.example.matrix_client_app.navigation.Screen
import com.example.matrix_client_app.ui.theme.Matrix_client_appTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Matrix_client_appTheme {
                var isCheckingAuth by remember { mutableStateOf(true) }
                var startDestination by remember { mutableStateOf(Screen.Login.route) }

                LaunchedEffect(Unit) {
                    if (tokenManager.isLoggedIn()) {
                        startDestination = Screen.RoomList.route
                    } else {
                        startDestination = Screen.Login.route
                    }
                    isCheckingAuth = false
                }

                if (!isCheckingAuth) {
                    NavGraph(startDestination = startDestination)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Matrix_client_appTheme {
        Greeting("Android")
    }
}