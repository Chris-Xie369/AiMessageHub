import SwiftUI
import SwiftData

@main
struct AiMessageHubApp: App {
    var body: some Scene {
        WindowGroup {
            MainView()
        }
        .modelContainer(for: StoredMessage.self)
    }
}

