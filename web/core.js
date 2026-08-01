"use strict";

function chatEndpoint(baseUrl) {
    const normalized = String(baseUrl || "https://api.openai.com/v1")
        .trim()
        .replace(/\/+$/, "");
    if (/\/chat\/completions$/i.test(normalized)) {
        return normalized;
    }
    return normalized + "/chat/completions";
}

function parseVariants(raw) {
    const seen = new Set();
    const result = [];
    for (const item of raw) {
        for (const part of String(item).split("---")) {
            const text = part.trim();
            if (text && !seen.has(text)) {
                seen.add(text);
                result.push(text);
            }
            if (result.length >= 3) return result;
        }
    }
    return result;
}

const HISTORY_LIMIT = 20;

function appendHistory(history, message) {
    const next = Array.isArray(history) ? history.slice() : [];
    next.push(message);
    return next.slice(-HISTORY_LIMIT);
}

function parseTranscript(text) {
    const lines = String(text || "")
        .split(/\r?\n/)
        .map((line) => line.trim())
        .filter(Boolean);
    const messages = [];
    let current = null;

    for (const line of lines) {
        const match = line.match(/^(我|对方)\s*[:：]\s*(.*)$/);
        if (!match) {
            if (current) {
                current.text += "\n" + line;
            } else {
                messages.push({ role: "user", text: line });
                current = messages[messages.length - 1];
            }
            continue;
        }
        const role = match[1] === "我" ? "assistant" : "user";
        const text = match[2].trim();
        if (text) {
            messages.push({ role, text });
            current = messages[messages.length - 1];
        }
    }
    return messages;
}

function buildUserMessage(contact, history, currentText) {
    const name = String(contact || "").trim() || "对方";
    const lines = (Array.isArray(history) ? history : [])
        .slice(-HISTORY_LIMIT)
        .map((message) => {
            const role = message.role === "assistant" ? "我" : name;
            return `${role}：${message.text}`;
        });
    const historyBlock = lines.length ? lines.join("\n") : "（暂无历史）";
    return [
        `正在与「${name}」聊天。`,
        `历史对话：\n${historyBlock}`,
        `最新消息：${String(currentText || "").trim()}`,
        "请给出 3 个不同角度的回复候选，使用「---」分隔。",
    ].join("\n");
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        chatEndpoint,
        parseVariants,
        appendHistory,
        parseTranscript,
        buildUserMessage,
        HISTORY_LIMIT,
    };
}
