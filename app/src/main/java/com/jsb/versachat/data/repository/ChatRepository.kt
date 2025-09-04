package com.jsb.versachat.data.repository

import com.jsb.versachat.data.api.RetrofitInstance
import com.jsb.versachat.data.model.ChatRequest
import com.jsb.versachat.data.model.Message

class ChatRepository {
    private val api = RetrofitInstance.api

    suspend fun getResponse(messages: List<Message>): String {
        val request = ChatRequest(messages = messages)
        return try {
            val response = api.createChatCompletion(request)
            response.choices.firstOrNull()?.message?.content ?: "No response received"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}