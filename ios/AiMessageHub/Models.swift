import Foundation
import SwiftData

enum ExecutionPolicy: String, CaseIterable, Codable {
    case suggest
    case oneTap
    case autoWhitelist
}

struct ChatMessage: Identifiable, Codable {
    let id: UUID
    let app: String
    let conversationId: String
    let contact: String
    let text: String
    let timestamp: Date
    let isOutgoing: Bool

    init(
        id: UUID = UUID(),
        app: String,
        conversationId: String,
        contact: String,
        text: String,
        timestamp: Date = Date(),
        isOutgoing: Bool = false
    ) {
        self.id = id
        self.app = app
        self.conversationId = conversationId
        self.contact = contact
        self.text = text
        self.timestamp = timestamp
        self.isOutgoing = isOutgoing
    }
}

struct AppSettings: Codable, Equatable {
    var baseURL: String = "https://api.openai.com/v1"
    var apiKey: String = ""
    var model: String = "gpt-4o-mini"
    var temperature: Double = 0.7
    var maxTokens: Int = 512
    var persona: String = ""
    var instructions: String = ""
    var policy: ExecutionPolicy = .oneTap
}

struct SuggestionResult: Equatable {
    let variants: [String]
    let error: String?

    var isSuccess: Bool {
        error == nil && !variants.isEmpty
    }
}

@Model
final class StoredMessage {
    var id: UUID
    var app: String
    var conversationId: String
    var contact: String
    var text: String
    var timestamp: Date
    var isOutgoing: Bool

    init(
        id: UUID = UUID(),
        app: String,
        conversationId: String,
        contact: String,
        text: String,
        timestamp: Date = Date(),
        isOutgoing: Bool = false
    ) {
        self.id = id
        self.app = app
        self.conversationId = conversationId
        self.contact = contact
        self.text = text
        self.timestamp = timestamp
        self.isOutgoing = isOutgoing
    }
}

