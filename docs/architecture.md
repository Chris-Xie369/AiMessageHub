# 架构

## 总体数据流

```text
社交 App 通知/界面
        |
        v
NotificationListenerService / AccessibilityService
        |
        v
ChatAdapter（微信/QQ/Soul/小红书/通用）
        |
        v
ConversationRepository（Room 本地存储 + 去重）
        |
        v
SuggestionEngine（OpenAI 兼容 Chat Completions）
        |
        v
ExecutionPolicy（建议 / 一键发送 / 白名单自动回复）
```

## Android 模块

- `data`：Room 数据库、实体、DAO、Repository
- `domain`：核心模型与 `ChatAdapter` 接口
- `ai`：OpenAI 兼容客户端、Prompt 构建、建议引擎
- `service`：通知监听、无障碍服务、悬浮建议气泡
- `ui`：Jetpack Compose 页面与 ViewModel

## iOS 模块

- `AiMessageHub`：SwiftUI 主 App，管理 Key、历史记录、回复风格
- `AiMessageHubShareExtension`：从其他 App 分享文本并生成建议
- `AiMessageHubKeyboardExtension`：输入时生成建议并可一键插入
- 两个扩展通过 App Group 共享配置，API Key 存 Keychain

