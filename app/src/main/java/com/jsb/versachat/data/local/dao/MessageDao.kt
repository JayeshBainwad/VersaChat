package com.jsb.versachat.data.local.dao

import androidx.room.*
import com.jsb.versachat.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesBySessionSync(sessionId: String): List<MessageEntity>

    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySession(sessionId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE sessionId = :sessionId")
    suspend fun getMessageCountBySession(sessionId: String): Int

    @Query("DELETE FROM messages WHERE sessionId = :sessionId AND role = 'assistant' AND timestamp = (SELECT MAX(timestamp) FROM messages WHERE sessionId = :sessionId AND role = 'assistant')")
    suspend fun deleteLastAIMessage(sessionId: String)

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId AND role = 'assistant' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastAIMessage(sessionId: String): MessageEntity?
}