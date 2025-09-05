package com.jsb.versachat.domain.model

data class ChatSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val messages: List<Message> = emptyList(),
    val responseStyle: ResponseStyle = ResponseStyle.DETAILED,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class Message(
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole(val value: String) {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system")
}

enum class ResponseStyle(val displayName: String, val maxTokens: Int, val temperature: Double) {
    SHORT("Short", 100, 0.5),
    DETAILED("Detailed", 1000, 0.7),
    EXPLANATORY("Explanatory", 1500, 0.9)
}