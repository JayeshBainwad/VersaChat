package com.jsb.versachat.data.repository

import android.util.Log
import com.jsb.versachat.data.api.GroqApi
import com.jsb.versachat.data.model.ChatRequest
import com.jsb.versachat.data.model.toApiMessage
import com.jsb.versachat.domain.model.Message
import com.jsb.versachat.domain.model.ResponseStyle
import com.jsb.versachat.domain.repository.ChatRepository
import com.jsb.versachat.domain.util.Result
import com.jsb.versachat.domain.util.safeSuspendCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val api: GroqApi
) : ChatRepository {

    companion object {
        private const val TAG = "ChatRepositoryImpl"
        private const val LOW_TOKEN_THRESHOLD = 1000
    }

    override suspend fun getChatResponse(
        messages: List<Message>,
        responseStyle: ResponseStyle
    ): Result<String> = withContext(Dispatchers.IO) {

        // Using safeSuspendCall utility
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

                    // Check remaining tokens
                    val remainingTokens = response.headers()["x-ratelimit-remaining-tokens"]?.toIntOrNull()
                    remainingTokens?.let { tokens ->
                        Log.d(TAG, "Remaining tokens: $tokens")
                        if (tokens < LOW_TOKEN_THRESHOLD) {
                            Log.w(TAG, "Low tokens warning: $tokens")
                        }
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
}