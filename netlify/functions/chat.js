exports.handler = async (event) => {
    if (event.httpMethod !== "POST") {
        return {
            statusCode: 405,
            body: JSON.stringify({ error: { message: "Method Not Allowed" } }),
        };
    }

    let request;
    try {
        request = JSON.parse(event.body || "{}");
    } catch {
        return {
            statusCode: 400,
            body: JSON.stringify({ error: { message: "Invalid JSON" } }),
        };
    }

    const { baseUrl, apiKey, payload } = request;
    if (!apiKey || !payload) {
        return {
            statusCode: 400,
            body: JSON.stringify({ error: { message: "Missing apiKey or payload" } }),
        };
    }

    const endpoint = String(baseUrl || "https://api.openai.com/v1")
        .replace(/\/+$/, "") + "/chat/completions";

    try {
        const upstream = await fetch(endpoint, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${apiKey}`,
            },
            body: JSON.stringify(payload),
        });
        const text = await upstream.text();
        return {
            statusCode: upstream.status,
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                "Cache-Control": "no-store",
            },
            body: text,
        };
    } catch (error) {
        return {
            statusCode: 502,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                error: { message: String(error.message || "Upstream request failed") },
            }),
        };
    }
};

