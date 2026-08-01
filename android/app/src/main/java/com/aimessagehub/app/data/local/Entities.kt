package com.aimessagehub.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val app: String,
    val conversationId: String,
    val conversationTitle: String,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val direction: String,
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val app: String,
    val title: String,
    val lastMessageAt: Long,
    val lastPreview: String,
)

@Entity(tableName = "contact_policies", primaryKeys = ["app", "contactId"])
data class ContactPolicyEntity(
    val app: String,
    val contactId: String,
    val contactName: String,
    val autoReplyEnabled: Boolean,
)
