package com.aimessagehub.app.domain

import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityNodes {
    fun findEditText(root: AccessibilityNodeInfo): AccessibilityNodeInfo? =
        findFirst(root) {
            it.className?.toString()?.contains("EditText", ignoreCase = true) == true ||
                it.viewIdResourceName?.endsWith("editText", ignoreCase = true) == true
        }

    fun findSendButton(root: AccessibilityNodeInfo): AccessibilityNodeInfo? =
        findFirst(root) {
            val description = it.contentDescription?.toString().orEmpty()
            val text = it.text?.toString().orEmpty()
            description.contains("发送", ignoreCase = true) ||
                text.contains("发送", ignoreCase = true) ||
                description.equals("Send", ignoreCase = true) ||
                text.equals("Send", ignoreCase = true)
        }

    fun collectText(root: AccessibilityNodeInfo, max: Int = 40): List<String> {
        val result = mutableListOf<String>()
        collectTextInternal(root, result, max)
        return result
    }

    private fun collectTextInternal(
        node: AccessibilityNodeInfo,
        result: MutableList<String>,
        max: Int,
    ) {
        if (result.size >= max) return
        val text = node.text?.toString()?.trim().orEmpty()
        if (text.isNotEmpty()) result += text
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            collectTextInternal(child, result, max)
        }
    }

    fun findFirst(
        node: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo? {
        if (predicate(node)) return node
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            findFirst(child, predicate)?.let { return it }
        }
        return null
    }
}

