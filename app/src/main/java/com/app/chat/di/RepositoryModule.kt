package com.app.chat.di

import com.app.chat.data.repository.AuthRepositoryImpl
import com.app.chat.data.repository.ChatRepositoryImpl
import com.app.chat.data.repository.UserRepositoryImpl
import com.app.chat.domain.repository.AuthRepository
import com.app.chat.domain.repository.ChatRepository
import com.app.chat.domain.repository.UserRepository
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
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}
