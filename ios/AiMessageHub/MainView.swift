import SwiftUI

struct MainView: View {
    var body: some View {
        TabView {
            AiSuggestView()
                .tabItem {
                    Label("建议", systemImage: "sparkles")
                }
            HistoryView()
                .tabItem {
                    Label("历史", systemImage: "message")
                }
            SettingsView()
                .tabItem {
                    Label("设置", systemImage: "gearshape")
                }
        }
    }
}
