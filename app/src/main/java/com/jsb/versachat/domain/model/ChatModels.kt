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

enum class ResponseStyle(
    val displayName: String,
    val maxTokens: Int,
    val temperature: Double,
    val systemPrompt: String
) {
    SHORT(
        displayName = "Short",
        maxTokens = 300, // Increased slightly to ensure complete responses
        temperature = 0.5,
        systemPrompt = """You are a helpful AI assistant. Provide concise, direct answers under 100-200 words maximum. 
            |Focus only on the essential information. Keep responses under 150 words when possible. 
            |Always complete your thoughts - never cut off mid-sentence.""".trimMargin()
    ),

    DETAILED(
        displayName = "Detailed",
        maxTokens = 800, // Increased for better response quality
        temperature = 0.7,
        systemPrompt = """You are a helpful AI assistant. Provide comprehensive answers with sufficient detail and context. 
            |Aim for 2-4 paragraphs (100-200 words). Include relevant explanations and examples where helpful. 
            |Ensure your response is complete and well-structured.""".trimMargin()
    ),

    EXPLANATORY(
        displayName = "Explanatory",
        maxTokens = 1200, // Increased for thorough explanations
        temperature = 0.9,
        systemPrompt = """You are a helpful AI assistant. Provide in-depth, thorough explanations with examples and detailed analysis. 
            |Break down complex concepts step-by-step. Include context, background information, and practical examples. 
            |Aim for 3-5 paragraphs (200-400 words). Ensure your explanation is educational and complete.""".trimMargin()
    )
}