package com.example.matrix_client_app.feature.rooms.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PublicRoomsResponse(
    @Json(name = "chunk")
    val chunk: List<PublicRoom>,

    @Json(name = "next_batch")
    val nextBatch: String? = null,

    @Json(name = "total_room_count_estimate")
    val totalRoomCountEstimate: Int? = null
)

@JsonClass(generateAdapter = true)
data class JoinRoomRequest(
    @Json(name = "user_id")
    val userId: String ?= null,
)

@JsonClass(generateAdapter = true)
data class PublicRoom(
    @Json(name = "room_id")
    val roomId: String,

    @Json(name = "name")
    val name: String? = null,

    @Json(name = "topic")
    val topic: String? = null,

    @Json(name = "num_joined_members")
    val numJoinedMembers: Int = 0,

    @Json(name = "avatar_url")
    val avatarUrl: String? = null,

    @Json(name = "world_readable")
    val isWorldReadable: Boolean = false,

    @Json(name = "guest_can_join")
    val guestCanJoin: Boolean = false,

    @Json(name = "join_rule")
    val joinRule: String? = null
) {
    fun getDisplayName(): String = name ?: roomId

    fun getDisplayTopic(): String = topic ?: "No description"
}

@JsonClass(generateAdapter = true)
data class JoinRoomResponse(
    @Json(name = "room_id")
    val roomId: String
)