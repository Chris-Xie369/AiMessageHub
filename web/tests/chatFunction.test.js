"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");
const { handler } = require("../../netlify/functions/chat.js");

test("Netlify Function routes DeepSeek root base URL", async () => {
    let seenUrl = "";
    const originalFetch = global.fetch;
    global.fetch = async (url) => {
        seenUrl = String(url);
        return {
            status: 200,
            text: async () => JSON.stringify({ choices: [{ message: { content: "ok" } }] }),
        };
    };
    try {
        const result = await handler({
            httpMethod: "POST",
            body: JSON.stringify({
                baseUrl: "https://api.deepseek.com",
                apiKey: "sk-test",
                payload: { model: "deepseek-chat", messages: [] },
            }),
        });
        assert.equal(result.statusCode, 200);
        assert.equal(seenUrl, "https://api.deepseek.com/chat/completions");
    } finally {
        global.fetch = originalFetch;
    }
});

test("Netlify Function preserves an already complete endpoint", async () => {
    let seenUrl = "";
    const originalFetch = global.fetch;
    global.fetch = async (url) => {
        seenUrl = String(url);
        return {
            status: 200,
            text: async () => "{}",
        };
    };
    try {
        await handler({
            httpMethod: "POST",
            body: JSON.stringify({
                baseUrl: "https://api.deepseek.com/chat/completions",
                apiKey: "sk-test",
                payload: {},
            }),
        });
        assert.equal(seenUrl, "https://api.deepseek.com/chat/completions");
    } finally {
        global.fetch = originalFetch;
    }
});

