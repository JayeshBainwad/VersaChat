package com.jsb.versachat.domain.repository

import com.jsb.versachat.domain.model.ChatSession
import com.jsb.versachat.domain.model.Message
import com.jsb.versachat.domain.model.ResponseStyle
import com.jsb.versachat.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // Remote operations
    suspend fun getChatResponse(messages: List<Message>, responseStyle: ResponseStyle): Result<String>

    // Local operations
    fun getAllSessions(): Flow<List<ChatSession>>
    suspend fun getSessionById(sessionId: String): ChatSession?
    suspend fun saveSession(session: ChatSession): Result<Unit>
    suspend fun updateSession(session: ChatSession): Result<Unit>
    suspend fun deleteSession(sessionId: String): Result<Unit>
    suspend fun saveMessage(sessionId: String, message: Message): Result<Unit>
}