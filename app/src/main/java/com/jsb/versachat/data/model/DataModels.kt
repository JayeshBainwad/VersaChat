package com.jsb.versachat.data.model

import com.jsb.versachat.domain.model.Message as DomainMessage
import com.jsb.versachat.domain.model.MessageRole
import com.jsb.versachat.domain.model.ResponseStyle

data class ChatRequest(
    val model: String = "openai/gpt-oss-120b",
//    val model: String = "meta-llama/llama-4-scout-17b-16e-instruct",
    val messages: List<ApiMessage>,
    val max_tokens: Int = 512,
    val temperature: Double = 0.7,
    val stop: List<String>? = null // Optional stop sequences for better control
)

data class ApiMessage(
    val role: String,
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>,
    val error: ApiError? = null
)

data class Choice(
    val message: ApiMessage,
    val finish_reason: String? = null
)

data class ApiError(
    val message: String,
    val type: String? = null,
    val code: String? = null
)

fun DomainMessage.toApiMessage() = ApiMessage(
    role = this.role.value,
    content = this.content
)

fun ApiMessage.toDomainMessage() = DomainMessage(
    role = MessageRole.entries.find { it.value == this.role } ?: MessageRole.ASSISTANT,
    content = this.content
)

// Helper function to build messages with system prompt
fun buildMessagesWithSystemPrompt(
    userMessages: List<DomainMessage>,
    responseStyle: ResponseStyle
): List<ApiMessage> {
    val messages = mutableListOf<ApiMessage>()

    // Add system message first
    messages.add(ApiMessage(
        role = MessageRole.SYSTEM.value,
        content = responseStyle.systemPrompt
    ))

    // Add conversation history (limit to last 20 messages for context)
    messages.addAll(
        userMessages.takeLast(20).map { it.toApiMessage() }
    )

    return messages
}