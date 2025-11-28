package com.example.matrix_client_app.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JoinedRoomsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "joined_rooms_prefs"
    )

    private companion object {
        val JOINED_ROOM_IDS = stringSetPreferencesKey("joined_room_ids")
    }

    val joinedRoomIds: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[JOINED_ROOM_IDS] ?: emptySet()
        }

    suspend fun getJoinedRoomIds(): Set<String> {
        return joinedRoomIds.first()
    }

    suspend fun isRoomJoined(roomId: String): Boolean {
        val joinedRooms = getJoinedRoomIds()
        return joinedRooms.contains(roomId)
    }

    suspend fun addJoinedRoom(roomId: String) {
        context.dataStore.edit { preferences ->
            val currentRooms = preferences[JOINED_ROOM_IDS] ?: emptySet()
            preferences[JOINED_ROOM_IDS] = currentRooms + roomId
        }
        Timber.d("Marked room as joined: $roomId")
    }

    suspend fun addJoinedRooms(roomIds: Set<String>) {
        context.dataStore.edit { preferences ->
            val currentRooms = preferences[JOINED_ROOM_IDS] ?: emptySet()
            preferences[JOINED_ROOM_IDS] = currentRooms + roomIds
        }
        Timber.d("Marked ${roomIds.size} rooms as joined")
    }

    suspend fun removeJoinedRoom(roomId: String) {
        context.dataStore.edit { preferences ->
            val currentRooms = preferences[JOINED_ROOM_IDS] ?: emptySet()
            preferences[JOINED_ROOM_IDS] = currentRooms - roomId
        }
        Timber.d("Removed room from joined: $roomId")
    }

    suspend fun clearJoinedRooms() {
        context.dataStore.edit { preferences ->
            preferences.remove(JOINED_ROOM_IDS)
        }
        Timber.d("Cleared all joined rooms")
    }

    suspend fun setJoinedRooms(roomIds: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[JOINED_ROOM_IDS] = roomIds
        }
        Timber.d("Set joined rooms: ${roomIds.size} rooms")
    }
}
