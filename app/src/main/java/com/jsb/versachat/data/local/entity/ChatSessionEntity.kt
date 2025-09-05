package com.jsb.versachat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val responseStyle: String,
    val createdAt: Long,
    val lastUpdated: Long
)