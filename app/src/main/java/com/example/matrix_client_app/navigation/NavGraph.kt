package com.example.matrix_client_app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.matrix_client_app.feature.auth.presentation.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object RoomList : Screen("room_list")
    object MessageList : Screen("message_list/{roomId}") {
        fun createRoute(roomId: String) = "message_list/$roomId"
    }
}

@Composable
fun NavGraph(
    startDestination: String = Screen.Login.route
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.RoomList.route) {
            // RoomListScreen will be added here
            // feature.rooms.presentation.list.RoomListScreen(navController = navController)
        }

        composable(Screen.MessageList.route) { backStackEntry ->
            // MessageListScreen will be added here
            // val roomId = backStackEntry.arguments?.getString("roomId")
            // feature.messages.presentation.MessageListScreen(navController = navController, roomId = roomId)
        }
    }
}