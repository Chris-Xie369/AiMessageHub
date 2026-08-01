import Foundation

enum PromptBuilder {
    static func system(persona: String, instructions: String) -> String {
        let style = persona.trimmingCharacters(in: .whitespacesAndNewlines)
        let rules = instructions.trimmingCharacters(in: .whitespacesAndNewlines)
        return """
        你是用户个人聊天助手，帮助用户理解消息并草拟回复。
        回复风格：\(style.isEmpty ? "自然、简洁、符合中文日常聊天习惯" : style)
        额外要求：\(rules.isEmpty ? "不要暴露你是 AI；不要编造事实；回复控制在 80 字以内；不要使用 Markdown。" : rules)
        只返回回复草稿本身，每条候选之间用「---」分隔。
        """
    }

    static func user(contact: String, history: [ChatMessage]) -> String {
        let lines = history
            .sorted { $0.timestamp < $1.timestamp }
            .suffix(20)
            .map { message in
                let role = message.isOutgoing ? "我" : message.contact
                return "\(role)：\(message.text)"
            }
            .joined(separator: "\n")
        return """
        正在与「\(contact)」聊天。
        最近消息：
        \(lines.isEmpty ? "（暂无历史消息）" : lines)

        请给出 3 个不同角度的回复候选，使用「---」分隔。
        """
    }
}

