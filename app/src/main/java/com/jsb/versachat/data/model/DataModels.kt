package com.jsb.versachat.data.model

import com.jsb.versachat.domain.model.Message as DomainMessage
import com.jsb.versachat.domain.model.MessageRole

data class ChatRequest(
    val model: String = "openai/gpt-oss-120b",
//    val model: String = "meta-llama/llama-4-scout-17b-16e-instruct",
    val messages: List<ApiMessage>,
    val max_tokens: Int = 512,
    val temperature: Double = 0.7
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

fun ApiMessage.toDomainMessage() = DomainMessage( //Function "toDomainMessage" is never used
    role = MessageRole.entries.find { it.value == this.role } ?: MessageRole.ASSISTANT,
    content = this.content
)