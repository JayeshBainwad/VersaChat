package com.jsb.versachat.data.local

import com.jsb.versachat.data.local.dao.ChatSessionDao
import com.jsb.versachat.data.local.dao.MessageDao
import com.jsb.versachat.data.local.entity.ChatSessionEntity
import com.jsb.versachat.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    private val sessionDao: ChatSessionDao,
    private val messageDao: MessageDao
) {

    // Session operations
    fun getAllSessions(): Flow<List<ChatSessionEntity>> = sessionDao.getAllSessions()

    suspend fun getSessionById(sessionId: String): ChatSessionEntity? = sessionDao.getSessionById(sessionId)

    suspend fun insertSession(session: ChatSessionEntity) = sessionDao.insertSession(session)

    suspend fun updateSession(session: ChatSessionEntity) = sessionDao.updateSession(session)

    suspend fun deleteSession(sessionId: String) = sessionDao.deleteSessionById(sessionId)

    // Message operations
    fun getMessagesBySession(sessionId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesBySession(sessionId)

    suspend fun getMessagesBySessionSync(sessionId: String): List<MessageEntity> =
        messageDao.getMessagesBySessionSync(sessionId)

    suspend fun insertMessage(message: MessageEntity): Long = messageDao.insertMessage(message)

    suspend fun insertMessages(messages: List<MessageEntity>) = messageDao.insertMessages(messages)

    suspend fun getMessageCount(sessionId: String): Int = messageDao.getMessageCountBySession(sessionId)
}