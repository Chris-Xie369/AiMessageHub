import SwiftUI
import UIKit

struct AiSuggestView: View {
    @State private var inputText = ""
    @State private var contact = ""
    @State private var result: SuggestionResult?
    @State private var isLoading = false

    var body: some View {
        NavigationStack {
            Form {
                Section("消息文本") {
                    TextEditor(text: $inputText)
                        .frame(minHeight: 160)
                }
                Section("联系人") {
                    TextField("联系人", text: $contact)
                }
                if let result {
                    Section("回复建议") {
                        if let error = result.error {
                            Label(error, systemImage: "exclamationmark.triangle")
                                .foregroundStyle(.red)
                        } else {
                            ForEach(Array(result.variants.enumerated()), id: \.offset) { index, variant in
                                HStack(alignment: .top) {
                                    Text(variant)
                                        .textSelection(.enabled)
                                    Spacer()
                                    Button("复制") {
                                        UIPasteboard.general.string = variant
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("AI 建议")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(isLoading ? "生成中" : "生成") {
                        generate()
                    }
                    .disabled(
                        isLoading ||
                            inputText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                    )
                }
                ToolbarItem(placement: .cancellationAction) {
                    Button {
                        inputText = UIPasteboard.general.string ?? ""
                    } label: {
                        Image(systemName: "doc.on.clipboard")
                    }
                }
            }
            .onAppear {
                if inputText.isEmpty {
                    inputText = UIPasteboard.general.string ?? ""
                }
            }
        }
    }

    private func generate() {
        let text = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty else { return }
        let name = contact.trimmingCharacters(in: .whitespacesAndNewlines)
        let resolvedContact = name.isEmpty ? "联系人" : name
        let message = ChatMessage(
            app: "manual",
            conversationId: "manual",
            contact: resolvedContact,
            text: text
        )
        isLoading = true
        Task { @MainActor in
            result = await SuggestionProvider().suggest(
                contact: resolvedContact,
                history: [message],
                settings: AppConfig.settings
            )
            isLoading = false
        }
    }
}

