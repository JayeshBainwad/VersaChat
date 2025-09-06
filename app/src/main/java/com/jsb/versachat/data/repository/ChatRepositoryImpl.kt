package com.jsb.versachat.data.repository

import android.util.Log
import com.jsb.versachat.data.api.GroqApi
import com.jsb.versachat.data.local.LocalDataSource
import com.jsb.versachat.data.local.mapper.toDomainModel
import com.jsb.versachat.data.local.mapper.toEntity
import com.jsb.versachat.data.model.ChatRequest
import com.jsb.versachat.data.model.buildMessagesWithSystemPrompt
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

            // Build messages with system prompt for the response style
            val messagesWithSystem = buildMessagesWithSystemPrompt(messages, responseStyle)

            val request = ChatRequest(
                messages = messagesWithSystem,
                max_tokens = responseStyle.maxTokens,
                temperature = responseStyle.temperature,
                // Add stop sequences for better control over response completion
                stop = when (responseStyle) {
                    ResponseStyle.SHORT -> listOf("\n\n", "---", "###")
                    ResponseStyle.DETAILED -> listOf("---", "###")
                    ResponseStyle.EXPLANATORY -> listOf("---", "###")
                }
            )

            Log.d(TAG, "Request: max_tokens=${request.max_tokens}, temperature=${request.temperature}")
            Log.d(TAG, "System prompt: ${responseStyle.systemPrompt.take(100)}...")

            val response = api.createChatCompletion(request)

            when {
                response.isSuccessful -> {
                    val body = response.body()
                        ?: throw Exception("Empty response from server")

                    if (body.error != null) {
                        throw Exception(body.error.message)
                    }

                    val choice = body.choices.firstOrNull()
                        ?: throw Exception("No choices in response")

                    val content = choice.message.content
                    if (content.isNullOrBlank()) {
                        throw Exception("No content received from AI")
                    }

                    // Log the finish reason for debugging
                    Log.d(TAG, "Response received - finish_reason: ${choice.finish_reason}")
                    Log.d(TAG, "Response length: ${content.length} characters")

                    // Clean up the response if needed
                    val cleanedContent = cleanupResponse(content, responseStyle)

                    Log.d(TAG, "Successfully received and cleaned response")
                    cleanedContent
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

    /**
     * Clean up the AI response to ensure it's properly formatted and complete
     */
    private fun cleanupResponse(content: String, responseStyle: ResponseStyle): String {
        var cleaned = content.trim()

        // Remove any trailing incomplete sentences if the response was cut off
        if (!cleaned.endsWith(".") && !cleaned.endsWith("!") && !cleaned.endsWith("?") && !cleaned.endsWith(":")) {
            val lastSentenceEnd = maxOf(
                cleaned.lastIndexOf('.'),
                cleaned.lastIndexOf('!'),
                cleaned.lastIndexOf('?')
            )

            if (lastSentenceEnd > cleaned.length * 0.7) { // Only if we're cutting less than 30%
                cleaned = cleaned.substring(0, lastSentenceEnd + 1)
            }
        }

        // Ensure minimum response quality for each style
        when (responseStyle) {
            ResponseStyle.SHORT -> {
                // For short responses, ensure we have at least one complete sentence
                if (cleaned.length < 10) {
                    Log.w(TAG, "Short response too brief, might be incomplete")
                }
            }
            ResponseStyle.DETAILED -> {
                // For detailed responses, ensure we have substantial content
                if (cleaned.split(' ').size < 50) {
                    Log.w(TAG, "Detailed response seems too brief")
                }
            }
            ResponseStyle.EXPLANATORY -> {
                // For explanatory responses, ensure we have comprehensive content
                if (cleaned.split(' ').size < 100) {
                    Log.w(TAG, "Explanatory response seems too brief")
                }
            }
        }

        return cleaned
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

    override suspend fun deleteLastAIMessage(sessionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeSuspendCall(TAG) {
            localDataSource.deleteLastAIMessage(sessionId)
        }
    }
}