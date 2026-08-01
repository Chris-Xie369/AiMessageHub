"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");
const { chatEndpoint, parseVariants } = require("../core.js");

test("chatEndpoint appends /chat/completions to a root base URL", () => {
    assert.equal(
        chatEndpoint("https://api.deepseek.com"),
        "https://api.deepseek.com/chat/completions"
    );
});

test("chatEndpoint preserves /v1 prefix", () => {
    assert.equal(
        chatEndpoint("https://api.deepseek.com/v1"),
        "https://api.deepseek.com/v1/chat/completions"
    );
});

test("chatEndpoint does not duplicate an existing /chat/completions path", () => {
    assert.equal(
        chatEndpoint("https://api.deepseek.com/chat/completions"),
        "https://api.deepseek.com/chat/completions"
    );
});

test("parseVariants splits, trims, deduplicates, and caps at three", () => {
    const variants = parseVariants(["A\n---\nB\n---\nB\n---\nC\n---\nD"]);
    assert.deepEqual(variants, ["A", "B", "C"]);
});

