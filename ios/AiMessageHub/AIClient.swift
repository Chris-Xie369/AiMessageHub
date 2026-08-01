import Foundation

protocol AIClientProtocol {
    func chatCompletion(
        system: String,
        user: String,
        settings: AppSettings
    ) async throws -> [String]
}

enum AIClientError: LocalizedError {
    case missingAPIKey
    case invalidResponse

    var errorDescription: String? {
        switch self {
        case .missingAPIKey:
            return "请先在设置中填写 API Key"
        case .invalidResponse:
            return "AI 服务返回了无效响应"
        }
    }
}

struct AIClient: AIClientProtocol {
    func chatCompletion(
        system: String,
        user: String,
        settings: AppSettings
    ) async throws -> [String] {
        guard !settings.apiKey.isEmpty else {
            throw AIClientError.missingAPIKey
        }
        let base = settings.baseURL.trimmingCharacters(in: .whitespacesAndNewlines)
        guard let url = URL(string: base + "/chat/completions") else {
            throw AIClientError.invalidResponse
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.timeoutInterval = 60
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(settings.apiKey)", forHTTPHeaderField: "Authorization")

        let body: [String: Any] = [
            "model": settings.model,
            "temperature": settings.temperature,
            "max_tokens": settings.maxTokens,
            "messages": [
                ["role": "system", "content": system],
                ["role": "user", "content": user],
            ],
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: request)
        guard
            let http = response as? HTTPURLResponse,
            (200..<300).contains(http.statusCode),
            let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
            let choices = json["choices"] as? [[String: Any]]
        else {
            let message = String(data: data, encoding: .utf8) ?? ""
            throw URLError(.badServerResponse, userInfo: [NSLocalizedDescriptionKey: message])
        }

        let variants = choices.compactMap { choice -> String? in
            guard
                let message = choice["message"] as? [String: Any],
                let content = message["content"] as? String
            else {
                return nil
            }
            let trimmed = content.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? nil : trimmed
        }
        guard !variants.isEmpty else {
            throw AIClientError.invalidResponse
        }
        return Array(variants.prefix(3))
    }
}

