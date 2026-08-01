import Foundation

enum AppConfig {
    static let appGroup = "group.com.aimessagehub.app"

    static var settings: AppSettings {
        get {
            guard
                let data = UserDefaults(suiteName: appGroup)?.data(forKey: "settings"),
                let decoded = try? JSONDecoder().decode(AppSettings.self, from: data)
            else {
                return AppSettings()
            }
            return decoded
        }
        set {
            guard let data = try? JSONEncoder().encode(newValue) else { return }
            UserDefaults(suiteName: appGroup)?.set(data, forKey: "settings")
        }
    }

    static var apiKey: String {
        if let stored = KeychainStore.read(key: "apiKey"), !stored.isEmpty {
            return stored
        }
        return settings.apiKey
    }

    static func saveAPIKey(_ key: String) {
        if KeychainStore.save(key, key: "apiKey") {
            return
        }
        var updated = settings
        updated.apiKey = key
        settings = updated
    }
}
