import SwiftUI
import SwiftData

struct HistoryView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \StoredMessage.timestamp, order: .reverse)
    private var storedMessages: [StoredMessage]

    @State private var showImport = false
    @State private var importText = ""

    var body: some View {
        NavigationStack {
            List {
                if storedMessages.isEmpty {
                    Text("暂无历史消息")
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(storedMessages) { message in
                        VStack(alignment: .leading, spacing: 4) {
                            HStack {
                                Text(message.contact)
                                    .font(.subheadline.weight(.semibold))
                                Spacer()
                                Text(message.timestamp.formatted(date: .abbreviated, time: .shortened))
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                            Text(message.text)
                                .font(.body)
                        }
                        .padding(.vertical, 4)
                    }
                    .onDelete { offsets in
                        for index in offsets {
                            modelContext.delete(storedMessages[index])
                        }
                    }
                }
            }
            .navigationTitle("历史消息")
            .toolbar {
                Button {
                    showImport = true
                } label: {
                    Image(systemName: "doc.on.clipboard")
                }
            }
            .sheet(isPresented: $showImport) {
                NavigationStack {
                    TextEditor(text: $importText)
                        .padding()
                        .navigationTitle("导入文本")
                        .toolbar {
                            ToolbarItem(placement: .confirmationAction) {
                                Button("导入") {
                                    importText()
                                    showImport = false
                                }
                            }
                            ToolbarItem(placement: .cancellationAction) {
                                Button("取消") {
                                    showImport = false
                                }
                            }
                        }
                }
            }
        }
    }

    private func importText() {
        let trimmed = importText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        let message = StoredMessage(
            app: "manual",
            conversationId: "manual",
            contact: "手动导入",
            text: trimmed
        )
        modelContext.insert(message)
        importText = ""
    }
}

