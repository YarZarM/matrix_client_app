package com.example.matrix_client_app.di

import com.example.matrix_client_app.feature.auth.data.repository.AuthRepositoryImpl
import com.example.matrix_client_app.feature.auth.domain.repository.AuthRepository
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

    // Future bindings will go here:
    // @Binds abstract fun bindRoomRepository(impl: RoomRepositoryImpl): RoomRepository
    // @Binds abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository
}