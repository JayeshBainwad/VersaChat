package com.jsb.versachat.data.local.mapper

import com.jsb.versachat.data.local.entity.ChatSessionEntity
import com.jsb.versachat.data.local.entity.MessageEntity
import com.jsb.versachat.domain.model.ChatSession
import com.jsb.versachat.domain.model.Message
import com.jsb.versachat.domain.model.MessageRole
import com.jsb.versachat.domain.model.ResponseStyle

// Entity to Domain
fun ChatSessionEntity.toDomainModel(messages: List<Message> = emptyList()) = ChatSession(
    id = id,
    title = title,
    messages = messages,
    responseStyle = ResponseStyle.entries.find { it.name == responseStyle } ?: ResponseStyle.DETAILED,
    createdAt = createdAt,
    lastUpdated = lastUpdated
)

fun MessageEntity.toDomainModel() = Message(
    role = MessageRole.entries.find { it.value == role } ?: MessageRole.ASSISTANT,
    content = content,
    timestamp = timestamp
)

// Domain to Entity
fun ChatSession.toEntity() = ChatSessionEntity(
    id = id,
    title = title,
    responseStyle = responseStyle.name,
    createdAt = createdAt,
    lastUpdated = lastUpdated
)

fun Message.toEntity(sessionId: String) = MessageEntity(
    sessionId = sessionId,
    role = role.value,
    content = content,
    timestamp = timestamp
)