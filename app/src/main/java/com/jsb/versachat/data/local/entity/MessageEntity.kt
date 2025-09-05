package com.jsb.versachat.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])] // Add this index
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val role: String,
    val content: String,
    val timestamp: Long
)