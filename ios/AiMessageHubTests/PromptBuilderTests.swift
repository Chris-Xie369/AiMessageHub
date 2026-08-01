import XCTest
@testable import AiMessageHub

final class PromptBuilderTests: XCTestCase {
    func testUserPromptContainsContactAndHistory() {
        let history = [
            ChatMessage(
                app: "wechat",
                conversationId: "alice",
                contact: "Alice",
                text: "周末有空吗",
                timestamp: Date(timeIntervalSince1970: 100)
            )
        ]

        let prompt = PromptBuilder.user(contact: "Alice", history: history)

        XCTAssertTrue(prompt.contains("Alice"))
        XCTAssertTrue(prompt.contains("周末有空吗"))
    }

    func testSystemPromptUsesDefaultRulesWhenBlank() {
        let prompt = PromptBuilder.system(persona: "", instructions: "")

        XCTAssertTrue(prompt.contains("不要暴露你是 AI"))
    }
}

