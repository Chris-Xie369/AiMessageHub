import Foundation

struct SuggestionProvider {
    private let client: any AIClientProtocol

    init(client: any AIClientProtocol = AIClient()) {
        self.client = client
    }

    func suggest(
        contact: String,
        history: [ChatMessage],
        settings: AppSettings
    ) async -> SuggestionResult {
        do {
            let raw = try await client.chatCompletion(
                system: PromptBuilder.system(
                    persona: settings.persona,
                    instructions: settings.instructions
                ),
                user: PromptBuilder.user(contact: contact, history: history),
                settings: settings
            )
            let variants = raw
                .flatMap { $0.components(separatedBy: "---") }
                .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
                .filter { !$0.isEmpty }
                .reduce(into: [String]()) { result, item in
                    if !result.contains(item) {
                        result.append(item)
                    }
                }
                .prefix(3)
            return SuggestionResult(variants: Array(variants), error: nil)
        } catch {
            return SuggestionResult(variants: [], error: error.localizedDescription)
        }
    }
}

