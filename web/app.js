"use strict";

const DEFAULTS = {
    baseUrl: "https://api.openai.com/v1",
    apiKey: "",
    model: "gpt-4o-mini",
    temperature: 0.7,
    maxTokens: 512,
    persona: "",
    instructions: "",
};

const STORAGE_KEY = "aimessagehub-web-v1";
const els = {
    tabs: Array.from(document.querySelectorAll(".tab")),
    suggestPanel: document.getElementById("suggest-panel"),
    settingsPanel: document.getElementById("settings-panel"),
    message: document.getElementById("message"),
    contact: document.getElementById("contact"),
    paste: document.getElementById("paste"),
    clear: document.getElementById("clear"),
    generate: document.getElementById("generate"),
    status: document.getElementById("status"),
    results: document.getElementById("results"),
    baseUrl: document.getElementById("baseUrl"),
    apiKey: document.getElementById("apiKey"),
    model: document.getElementById("model"),
    temperature: document.getElementById("temperature"),
    maxTokens: document.getElementById("maxTokens"),
    persona: document.getElementById("persona"),
    instructions: document.getElementById("instructions"),
    save: document.getElementById("save"),
    saveStatus: document.getElementById("saveStatus"),
};

let settings = loadSettings();

function loadSettings() {
    try {
        const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) || "{}");
        return { ...DEFAULTS, ...stored };
    } catch {
        return { ...DEFAULTS };
    }
}

function persistSettings() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
}

function setStatus(message, type) {
    els.status.textContent = message || "";
    els.status.className = "status" + (type ? " " + type : "");
}

function bindEvents() {
    els.tabs.forEach((tab) => {
        tab.addEventListener("click", () => {
            els.tabs.forEach((item) => item.classList.toggle("is-active", item === tab));
            const target = tab.dataset.tab;
            els.suggestPanel.hidden = target !== "suggest";
            els.settingsPanel.hidden = target !== "settings";
        });
    });

    els.paste.addEventListener("click", readClipboard);
    els.clear.addEventListener("click", () => {
        els.message.value = "";
        els.contact.value = "";
        els.results.replaceChildren();
        setStatus("");
    });
    els.generate.addEventListener("click", generate);
    els.save.addEventListener("click", saveSettings);
}

function readClipboard() {
    if (!navigator.clipboard || !navigator.clipboard.readText) {
        setStatus("当前浏览器不支持读取剪贴板，请手动粘贴。", "error");
        return;
    }
    navigator.clipboard
        .readText()
        .then((text) => {
            if (text) {
                els.message.value = text;
                setStatus("已读取剪贴板");
            } else {
                setStatus("剪贴板为空");
            }
        })
        .catch(() => {
            setStatus("无法读取剪贴板，请手动粘贴。", "error");
        });
}

function collectSettings() {
    settings = {
        ...settings,
        baseUrl: els.baseUrl.value.trim(),
        apiKey: els.apiKey.value.trim(),
        model: els.model.value.trim() || DEFAULTS.model,
        temperature: Number(els.temperature.value) || DEFAULTS.temperature,
        maxTokens: Number(els.maxTokens.value) || DEFAULTS.maxTokens,
        persona: els.persona.value.trim(),
        instructions: els.instructions.value.trim(),
    };
}

function fillSettings() {
    els.baseUrl.value = settings.baseUrl;
    els.apiKey.value = settings.apiKey;
    els.model.value = settings.model;
    els.temperature.value = settings.temperature;
    els.maxTokens.value = settings.maxTokens;
    els.persona.value = settings.persona;
    els.instructions.value = settings.instructions;
}

function saveSettings() {
    collectSettings();
    persistSettings();
    els.saveStatus.textContent = "设置已保存";
    els.saveStatus.className = "status success";
}

function buildMessages(messageText) {
    const persona = settings.persona.trim() || "自然、简洁、符合中文日常聊天习惯";
    const rules = settings.instructions.trim() ||
        "不要暴露你是 AI；不要编造事实；回复控制在 80 字以内；不要使用 Markdown。";
    const contact = els.contact.value.trim() || "对方";
    return [
        {
            role: "system",
            content: `你是用户个人聊天助手，帮助用户理解消息并草拟回复。回复风格：${persona}。额外要求：${rules}。只返回回复草稿本身，每条候选之间用「---」分隔。`,
        },
        {
            role: "user",
            content: `正在与「${contact}」聊天。最近消息：${messageText}。请给出 3 个不同角度的回复候选，使用「---」分隔。`,
        },
    ];
}

async function generate() {
    const messageText = els.message.value.trim();
    if (!messageText) {
        setStatus("请先粘贴消息文本。", "error");
        return;
    }
    collectSettings();
    persistSettings();
    if (!settings.apiKey) {
        setStatus("请先在设置中填写 API Key。", "error");
        els.settingsPanel.hidden = false;
        els.suggestPanel.hidden = true;
        els.tabs.forEach((item) => item.classList.toggle("is-active", item.dataset.tab === "settings"));
        return;
    }

    setStatus("正在生成建议…");
    els.generate.disabled = true;
    try {
        const response = await fetch(settings.baseUrl.replace(/\/+$/, "") + "/chat/completions", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: "Bearer " + settings.apiKey,
            },
            body: JSON.stringify({
                model: settings.model,
                temperature: settings.temperature,
                max_tokens: settings.maxTokens,
                messages: buildMessages(messageText),
            }),
        });
        const body = await response.json();
        if (!response.ok) {
            const detail = body && body.error && body.error.message ? body.error.message : "请求失败";
            throw new Error(`HTTP ${response.status}: ${detail}`);
        }
        const choices = Array.isArray(body.choices) ? body.choices : [];
        const raw = choices
            .map((choice) => choice && choice.message && choice.message.content)
            .filter(Boolean);
        const variants = parseVariants(raw);
        if (variants.length === 0) {
            throw new Error("AI 没有返回可用回复");
        }
        renderVariants(variants);
        setStatus(`已生成 ${variants.length} 条建议`);
    } catch (error) {
        setStatus(error.message || "生成失败，请检查网络或设置。", "error");
    } finally {
        els.generate.disabled = false;
    }
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

function renderVariants(variants) {
    els.results.replaceChildren();
    variants.forEach((variant, index) => {
        const card = document.createElement("section");
        card.className = "suggestion";
        card.setAttribute("aria-label", `建议 ${index + 1}`);
        const text = document.createElement("p");
        text.textContent = variant;
        const button = document.createElement("button");
        button.type = "button";
        button.className = "copy-button";
        button.textContent = "复制";
        button.addEventListener("click", () => copyText(variant, button));
        card.append(text, button);
        els.results.append(card);
    });
}

async function copyText(text, button) {
    try {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            await navigator.clipboard.writeText(text);
        } else {
            fallbackCopy(text);
        }
        const previous = button.textContent;
        button.textContent = "已复制";
        setTimeout(() => {
            button.textContent = previous;
        }, 1200);
    } catch {
        fallbackCopy(text);
    }
}

function fallbackCopy(text) {
    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.setAttribute("readonly", "");
    textarea.style.position = "fixed";
    textarea.style.opacity = "0";
    document.body.appendChild(textarea);
    textarea.select();
    try {
        document.execCommand("copy");
    } catch {
        setStatus("复制失败，请长按文本手动复制。", "error");
    }
    textarea.remove();
}

function handleUrlParams() {
    const params = new URLSearchParams(window.location.search);
    const text = params.get("text");
    const contact = params.get("contact");
    const auto = params.get("auto") === "1";
    if (text) els.message.value = text;
    if (contact) els.contact.value = contact;
    if (auto && text && settings.apiKey) {
        setTimeout(generate, 300);
    }
}

if ("serviceWorker" in navigator) {
    navigator.serviceWorker.register("./sw.js").catch(() => {});
}

fillSettings();
bindEvents();
handleUrlParams();

