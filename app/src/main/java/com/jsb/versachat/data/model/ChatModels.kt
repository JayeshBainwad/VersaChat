package com.jsb.versachat.data.model

data class ChatRequest(
    val model: String = "meta-llama/llama-4-scout-17b-16e-instruct",
    val messages: List<Message>,
    val max_tokens: Int = 512,
    val temperature: Double = 0.7
)

data class Message(
    val role: String, // "user", "system", or "assistant"
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
