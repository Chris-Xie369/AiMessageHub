"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");
const {
    appendHistory,
    parseTranscript,
    buildUserMessage,
} = require("../core.js");

test("appendHistory appends messages and caps at 20", () => {
    let history = [];
    for (let index = 0; index < 25; index += 1) {
        history = appendHistory(history, { role: "user", text: `msg-${index}` });
    }
    assert.equal(history.length, 20);
    assert.equal(history[0].text, "msg-5");
    assert.equal(history[19].text, "msg-24");
});

test("parseTranscript converts 我/对方 lines into assistant/user messages", () => {
    const messages = parseTranscript(
        "对方：在吗\n我：在的\n对方：晚上一起吃饭吗"
    );
    assert.deepEqual(messages, [
        { role: "user", text: "在吗" },
        { role: "assistant", text: "在的" },
        { role: "user", text: "晚上一起吃饭吗" },
    ]);
});

test("parseTranscript joins continuation lines into the previous message", () => {
    const messages = parseTranscript("对方：第一行\n第二行");
    assert.deepEqual(messages, [{ role: "user", text: "第一行\n第二行" }]);
});

test("buildUserMessage includes history and the latest message", () => {
    const history = [
        { role: "user", text: "周末有空吗" },
        { role: "assistant", text: "有的" },
    ];
    const prompt = buildUserMessage("Alice", history, "那周五见");
    assert.ok(prompt.includes("Alice"));
    assert.ok(prompt.includes("周末有空吗"));
    assert.ok(prompt.includes("有的"));
    assert.ok(prompt.includes("那周五见"));
});

