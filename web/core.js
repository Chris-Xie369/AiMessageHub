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

if (typeof module !== "undefined" && module.exports) {
    module.exports = { chatEndpoint, parseVariants };
}

