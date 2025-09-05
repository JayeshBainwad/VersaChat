package com.jsb.versachat.data.repository

import android.util.Log
import com.jsb.versachat.data.api.GroqApi
import com.jsb.versachat.data.local.LocalDataSource
import com.jsb.versachat.data.local.mapper.toDomainModel
import com.jsb.versachat.data.local.mapper.toEntity
import com.jsb.versachat.data.model.ChatRequest
import com.jsb.versachat.data.model.toApiMessage
import com.jsb.versachat.domain.model.ChatSession
import com.jsb.versachat.domain.model.Message
import com.jsb.versachat.domain.model.ResponseStyle
import com.jsb.versachat.domain.repository.ChatRepository
import com.jsb.versachat.domain.util.Result
import com.jsb.versachat.domain.util.safeSuspendCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val api: GroqApi,
    private val localDataSource: LocalDataSource
) : ChatRepository {

    companion object {
        private const val TAG = "ChatRepositoryImpl"
        private const val LOW_TOKEN_THRESHOLD = 1000
    }

    override suspend fun getChatResponse(
        messages: List<Message>,
        responseStyle: ResponseStyle
    ): Result<String> = withContext(Dispatchers.IO) {
        safeSuspendCall(TAG) {
            Log.d(TAG, "Sending chat request with ${messages.size} messages, style: ${responseStyle.displayName}")

            if (messages.isEmpty()) {
                throw IllegalArgumentException("Messages list cannot be empty")
            }

            val request = ChatRequest(
                messages = messages.map { it.toApiMessage() },
                max_tokens = responseStyle.maxTokens,
                temperature = responseStyle.temperature
            )

            val response = api.createChatCompletion(request)

            when {
                response.isSuccessful -> {
                    val body = response.body()
                        ?: throw Exception("Empty response from server")

                    if (body.error != null) {
                        throw Exception(body.error.message)
                    }

                    val content = body.choices.firstOrNull()?.message?.content
                    if (content.isNullOrBlank()) {
                        throw Exception("No content received from AI")
                    }

                    Log.d(TAG, "Successfully received response")
                    content
                }
                else -> {
                    val errorMessage = when (response.code()) {
                        400 -> "Invalid request. Please check your input."
                        401 -> "Authentication failed. Please check your API key."
                        403 -> "Access forbidden. Please check your permissions."
                        429 -> "Rate limit exceeded. Please try again later."
                        500 -> "Server error. Please try again later."
                        503 -> "Service temporarily unavailable. Please try again later."
                        else -> "Request failed with code ${response.code()}"
                    }
                    throw Exception(errorMessage)
                }
            }
        }
    }

    override fun getAllSessions(): Flow<List<ChatSession>> {
        return localDataSource.getAllSessions().map { sessionEntities ->
            sessionEntities.map { sessionEntity ->
                val messages = localDataSource.getMessagesBySessionSync(sessionEntity.id)
                    .map { it.toDomainModel() }
                sessionEntity.toDomainModel(messages)
            }
        }
    }

    override suspend fun getSessionById(sessionId: String): ChatSession? = withContext(Dispatchers.IO) {
        val sessionEntity = localDataSource.getSessionById(sessionId) ?: return@withContext null
        val messages = localDataSource.getMessagesBySessionSync(sessionId)
            .map { it.toDomainModel() }
        sessionEntity.toDomainModel(messages)
    }

    override suspend fun saveSession(session: ChatSession): Result<Unit> = withContext(Dispatchers.IO) {
        safeSuspendCall(TAG) {
            localDataSource.insertSession(session.toEntity())
            // Save messages if any
            if (session.messages.isNotEmpty()) {
                val messageEntities = session.messages.map { it.toEntity(session.id) }
                localDataSource.insertMessages(messageEntities)
            }
        }
    }

    override suspend fun updateSession(session: ChatSession): Result<Unit> = withContext(Dispatchers.IO) {
        safeSuspendCall(TAG) {
            localDataSource.updateSession(session.toEntity())
        }
    }

    override suspend fun deleteSession(sessionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeSuspendCall(TAG) {
            localDataSource.deleteSession(sessionId)
        }
    }

    override suspend fun saveMessage(sessionId: String, message: Message): Result<Unit> = withContext(Dispatchers.IO) {
        safeSuspendCall(TAG) {
            localDataSource.insertMessage(message.toEntity(sessionId))

            // Update session's lastUpdated timestamp
            val session = localDataSource.getSessionById(sessionId)
            session?.let {
                localDataSource.updateSession(it.copy(lastUpdated = System.currentTimeMillis()))
            }
            Unit
        }
    }
}