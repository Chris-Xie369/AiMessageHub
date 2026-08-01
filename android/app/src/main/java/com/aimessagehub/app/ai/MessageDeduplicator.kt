package com.aimessagehub.app.ai

import com.aimessagehub.app.domain.ChatMessage

class MessageDeduplicator(
    private val capacity: Int = 500,
) {
    private val seen = object : LinkedHashMap<String, Long>(capacity, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>): Boolean =
            size > capacity
    }

    fun isDuplicate(message: ChatMessage): Boolean = synchronized(this) {
        val key = "${message.app.name}:${message.conversationId}:${message.id}:${message.timestamp}"
        if (seen.containsKey(key)) {
            true
        } else {
            seen[key] = System.currentTimeMillis()
            false
        }
    }
}
