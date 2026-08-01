import SwiftUI

struct SettingsView: View {
    @State private var baseURL = AppConfig.settings.baseURL
    @State private var apiKey = AppConfig.apiKey
    @State private var model = AppConfig.settings.model
    @State private var temperature = AppConfig.settings.temperature
    @State private var maxTokens = AppConfig.settings.maxTokens
    @State private var persona = AppConfig.settings.persona
    @State private var instructions = AppConfig.settings.instructions
    @State private var policy = AppConfig.settings.policy

    var body: some View {
        NavigationStack {
            Form {
                Section("AI 服务") {
                    TextField("Base URL", text: $baseURL)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                    SecureField("API Key", text: $apiKey)
                    TextField("模型", text: $model)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                    HStack {
                        Text("Temperature")
                        Spacer()
                        TextField("0.7", value: $temperature, format: .number)
                            .keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 80)
                    }
                    Stepper("Max Tokens: \(maxTokens)", value: $maxTokens, in: 64...4096, step: 64)
                }

                Section("回复风格") {
                    TextField("风格", text: $persona)
                    TextField("额外要求", text: $instructions, axis: .vertical)
                }

                Section("执行策略") {
                    Picker("策略", selection: $policy) {
                        Text("仅建议").tag(ExecutionPolicy.suggest)
                        Text("一键发送").tag(ExecutionPolicy.oneTap)
                        Text("白名单自动").tag(ExecutionPolicy.autoWhitelist)
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("设置")
            .toolbar {
                Button("保存") {
                    save()
                }
            }
        }
    }

    private func save() {
        var updated = AppConfig.settings
        updated.baseURL = baseURL
        updated.model = model
        updated.temperature = temperature
        updated.maxTokens = maxTokens
        updated.persona = persona
        updated.instructions = instructions
        updated.policy = policy
        AppConfig.settings = updated
        AppConfig.saveAPIKey(apiKey)
    }
}

