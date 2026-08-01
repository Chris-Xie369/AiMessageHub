import XCTest
@testable import AiMessageHub

final class SuggestionProviderTests: XCTestCase {
    func testSuccessfulResponseReturnsVariants() async {
        let provider = SuggestionProvider(client: FakeClient(response: ["好的呀\n---\n当然可以"]))

        let result = await provider.suggest(
            contact: "Alice",
            history: [],
            settings: AppSettings(apiKey: "test")
        )

        XCTAssertTrue(result.isSuccess)
        XCTAssertEqual(result.variants.count, 2)
    }

    func testFailureReturnsError() async {
        let provider = SuggestionProvider(client: FakeClient(response: [], error: "HTTP 401"))

        let result = await provider.suggest(
            contact: "Alice",
            history: [],
            settings: AppSettings(apiKey: "bad")
        )

        XCTAssertFalse(result.isSuccess)
        XCTAssertEqual(result.error, "HTTP 401")
    }
}

private final class FakeClient: AIClientProtocol {
    private let response: [String]
    private let error: String?

    init(response: [String], error: String? = nil) {
        self.response = response
        self.error = error
    }

    func chatCompletion(
        system: String,
        user: String,
        settings: AppSettings
    ) async throws -> [String] {
        if let error {
            throw NSError(
                domain: "test",
                code: 1,
                userInfo: [NSLocalizedDescriptionKey: error]
            )
        }
        return response
    }
}

