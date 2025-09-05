package com.jsb.versachat.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.jsb.versachat.data.local.dao.ChatSessionDao
import com.jsb.versachat.data.local.dao.MessageDao
import com.jsb.versachat.data.local.entity.ChatSessionEntity
import com.jsb.versachat.data.local.entity.MessageEntity

@Database(
    entities = [ChatSessionEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VersaChatDatabase : RoomDatabase() {

    abstract fun sessionDao(): ChatSessionDao
    abstract fun messageDao(): MessageDao

    companion object {
        const val DATABASE_NAME = "versachat_database"
    }
}