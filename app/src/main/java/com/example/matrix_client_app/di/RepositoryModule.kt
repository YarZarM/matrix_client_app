package com.example.matrix_client_app.di

import com.example.matrix_client_app.feature.auth.data.repository.AuthRepositoryImpl
import com.example.matrix_client_app.feature.rooms.data.repository.RoomRepositoryImpl
import com.example.matrix_client_app.feature.auth.domain.repository.AuthRepository
import com.example.matrix_client_app.feature.messages.data.repository.MessageRepositoryImpl
import com.example.matrix_client_app.feature.messages.domain.MessageRepository
import com.example.matrix_client_app.feature.rooms.domain.repository.RoomRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRoomRepository(
        roomRepositoryImpl: RoomRepositoryImpl
    ): RoomRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        messageRepositoryImpl: MessageRepositoryImpl
    ): MessageRepository

}