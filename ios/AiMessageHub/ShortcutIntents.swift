import AppIntents
import UIKit

enum ShortcutSuggestionRunner {
    static func run(text: String, contact: String = "联系人") async -> SuggestionResult {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        let resolvedContact = contact.trimmingCharacters(in: .whitespacesAndNewlines)
        let message = ChatMessage(
            app: "shortcut",
            conversationId: "shortcut",
            contact: resolvedContact.isEmpty ? "联系人" : resolvedContact,
            text: trimmed
        )
        return await SuggestionProvider().suggest(
            contact: resolvedContact.isEmpty ? "联系人" : resolvedContact,
            history: [message],
            settings: AppConfig.settings
        )
    }
}

enum ShortcutIntentError: LocalizedError {
    case emptyText
    case generationFailed(String)

    var errorDescription: String? {
        switch self {
        case .emptyText:
            return "剪贴板或输入文本为空"
        case .generationFailed(let message):
            return message
        }
    }
}

struct GenerateReplyIntent: AppIntent {
    static var title: LocalizedStringResource = "生成回复建议"
    static var description = IntentDescription("基于指定文本生成 AI 回复建议，并把建议复制到剪贴板。")

    @Parameter(title: "文本")
    var text: String

    @Parameter(title: "联系人", default: "联系人")
    var contact: String

    func perform() async throws -> some IntentResult & ProvidesDialog {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            throw ShortcutIntentError.emptyText
        }
        let result = await ShortcutSuggestionRunner.run(text: trimmed, contact: contact)
        guard result.isSuccess else {
            throw ShortcutIntentError.generationFailed(result.error ?? "生成失败")
        }
        UIPasteboard.general.string = result.variants.joined(separator: "\n")
        return .result(dialog: "已复制 \(result.variants.count) 条回复建议")
    }
}

struct AnalyzeClipboardIntent: AppIntent {
    static var title: LocalizedStringResource = "分析剪贴板生成回复"
    static var description = IntentDescription("读取剪贴板文本，生成 AI 回复建议并复制。")

    func perform() async throws -> some IntentResult & ProvidesDialog {
        let text = UIPasteboard.general.string ?? ""
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            throw ShortcutIntentError.emptyText
        }
        let result = await ShortcutSuggestionRunner.run(text: trimmed)
        guard result.isSuccess else {
            throw ShortcutIntentError.generationFailed(result.error ?? "生成失败")
        }
        UIPasteboard.general.string = result.variants.joined(separator: "\n")
        return .result(dialog: "已复制 \(result.variants.count) 条回复建议")
    }
}

struct AiMessageHubShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: GenerateReplyIntent(),
            phrases: [
                "用 \(.applicationName) 生成回复",
                "用 \(.applicationName) 写回复建议",
            ],
            shortTitle: "AI 回复建议",
            systemImageName: "sparkles"
        )
    }
}
