package com.example.matrix_client_app.feature.rooms.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.matrix_client_app.feature.rooms.data.model.PublicRoom
import com.example.matrix_client_app.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
    navController: NavController,
    viewModel: RoomViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is RoomListNavigationEvent.NavigateToRoom -> {
                    navController.navigate(Screen.MessageList.createRoute(event.roomId))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Public Rooms") },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(RoomEvent.RefreshRooms) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Loading state (initial load)
                state.isLoading && state.rooms.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Error state
                state.error != null -> {
                    ErrorMessage(
                        message = state.error!!,
                        onDismiss = { viewModel.onEvent(RoomEvent.DismissError) },
                        onRetry = { viewModel.onEvent(RoomEvent.LoadRooms) }
                    )
                }

                // Empty state
                state.rooms.isEmpty() -> {
                    Text(
                        text = "No public rooms found",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Content: List of rooms
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.rooms) { room ->
                            RoomItem(
                                room = room,
                                isJoined = state.joinedRoomIds.contains(room.roomId),
                                onJoinClick = {
                                    viewModel.onEvent(RoomEvent.JoinRoom(room.roomId))
                                },
                                onRoomClick = {
                                    viewModel.onEvent(RoomEvent.OpenRoom(room.roomId))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single room item in the list
 */
@Composable
fun RoomItem(
    room: PublicRoom,
    isJoined: Boolean,
    onJoinClick: () -> Unit,
    onRoomClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRoomClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Room name
            Text(
                text = room.getDisplayName(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Room topic
            Text(
                text = room.getDisplayTopic(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Member count and join button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${room.numJoinedMembers} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isJoined) {
                    AssistChip(
                        onClick = onRoomClick,
                        label = { Text("Open") }
                    )
                } else {
                    Button(
                        onClick = onJoinClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Join")
                    }
                }
            }
        }
    }
}

/**
 * Error message with retry button
 */
@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Dismiss")
            }

            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
