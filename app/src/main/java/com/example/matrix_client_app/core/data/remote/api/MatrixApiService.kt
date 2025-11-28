package com.example.matrix_client_app.core.data.remote.api

import com.example.matrix_client_app.feature.rooms.data.model.JoinRoomResponse
import com.example.matrix_client_app.feature.auth.data.model.LoginRequest
import com.example.matrix_client_app.feature.auth.data.model.LoginResponse
import com.example.matrix_client_app.feature.messages.data.model.MessageResponse
import com.example.matrix_client_app.feature.rooms.data.model.PublicRoomsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url
import retrofit2.http.Query

interface MatrixApiService {

    @POST
    suspend fun login(
        @Url baseUrl: String,
        @Body request: LoginRequest
    ): LoginResponse

    @GET
    suspend fun getPublciRooms(
        @Url baseUrl: String,
        @Query("limit") limit: Int ?= null
    ): PublicRoomsResponse

//    @GET
//    suspend fun getPublciRooms(
//        @Url baseUrl: String,
//        @Body request: PublicRoomRequest
//    ): PublicRoomsResponse

    @POST
    suspend fun joinPublicRooms(
        @Url baseUrl: String,
        @Header("Authorization") authorization: String,
    ): JoinRoomResponse

    @GET
    suspend fun getMessageList(
        @Url baseUrl: String,
        @Header("Authorization") authorization: String,
        @Query("from") from: String? = null,
        @Query("dir") dir: String = "b",
        @Query("limit") limit: Int? = null
    ): MessageResponse

}