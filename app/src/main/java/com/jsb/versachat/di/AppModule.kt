package com.jsb.versachat.di

import com.jsb.versachat.data.api.GroqApi
import com.jsb.versachat.data.api.RetrofitInstance
import com.jsb.versachat.data.repository.ChatRepositoryImpl
import com.jsb.versachat.domain.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofitInstance(): RetrofitInstance = RetrofitInstance()

    @Provides
    @Singleton
    fun provideGroqApi(retrofitInstance: RetrofitInstance): GroqApi = retrofitInstance.api

    @Provides
    @Singleton
    fun provideChatRepository(api: GroqApi): ChatRepository = ChatRepositoryImpl(api)
}