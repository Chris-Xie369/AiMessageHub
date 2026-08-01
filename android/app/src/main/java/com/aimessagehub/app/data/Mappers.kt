package com.aimessagehub.app.data

import com.aimessagehub.app.data.local.ContactPolicyEntity
import com.aimessagehub.app.data.local.ConversationEntity
import com.aimessagehub.app.data.local.MessageEntity
import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.domain.ChatMessage
import com.aimessagehub.app.domain.ContactPolicy
import com.aimessagehub.app.domain.Conversation
import com.aimessagehub.app.domain.MessageDirection

fun MessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    app = AppSource.valueOf(app),
    conversationId = conversationId,
    conversationTitle = conversationTitle,
    sender = sender,
    text = text,
    timestamp = timestamp,
    direction = MessageDirection.valueOf(direction),
)

fun ChatMessage.toEntity(): MessageEntity = MessageEntity(
    id = id,
    app = app.name,
    conversationId = conversationId,
    conversationTitle = conversationTitle,
    sender = sender,
    text = text,
    timestamp = timestamp,
    direction = direction.name,
)

fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    app = AppSource.valueOf(app),
    title = title,
    lastMessageAt = lastMessageAt,
    lastPreview = lastPreview,
)

fun ContactPolicyEntity.toDomain(): ContactPolicy = ContactPolicy(
    app = AppSource.valueOf(app),
    contactId = contactId,
    contactName = contactName,
    autoReplyEnabled = autoReplyEnabled,
)

