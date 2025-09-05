package com.jsb.versachat.di

import android.content.Context
import androidx.room.Room
import com.jsb.versachat.data.api.GroqApi
import com.jsb.versachat.data.api.RetrofitInstance
import com.jsb.versachat.data.local.LocalDataSource
import com.jsb.versachat.data.local.dao.ChatSessionDao
import com.jsb.versachat.data.local.dao.MessageDao
import com.jsb.versachat.data.local.database.VersaChatDatabase
import com.jsb.versachat.data.repository.ChatRepositoryImpl
import com.jsb.versachat.domain.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Retrofit ---
    @Provides
    @Singleton
    fun provideRetrofitInstance(): RetrofitInstance = RetrofitInstance()

    @Provides
    @Singleton
    fun provideGroqApi(retrofitInstance: RetrofitInstance): GroqApi = retrofitInstance.api

    // --- Room Database ---
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VersaChatDatabase {
        return Room.databaseBuilder(
            context,
            VersaChatDatabase::class.java,
            VersaChatDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development only
            .build()
    }

    @Provides
    fun provideSessionDao(database: VersaChatDatabase): ChatSessionDao = database.sessionDao()

    @Provides
    fun provideMessageDao(database: VersaChatDatabase): MessageDao = database.messageDao()

    // --- LocalDataSource ---
    @Provides
    @Singleton
    fun provideLocalDataSource(
        sessionDao: ChatSessionDao,
        messageDao: MessageDao
    ): LocalDataSource = LocalDataSource(sessionDao, messageDao)

    // --- Repository ---
    @Provides
    @Singleton
    fun provideChatRepository(
        api: GroqApi,
        localDataSource: LocalDataSource
    ): ChatRepository = ChatRepositoryImpl(api, localDataSource)
}
