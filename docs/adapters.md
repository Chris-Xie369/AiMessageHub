# ChatAdapter

每个社交 App 的界面和通知格式不同，`ChatAdapter` 是统一适配层：

```kotlin
interface ChatAdapter {
    val source: AppSource
    fun parseNotification(envelope: NotificationEnvelope): ChatMessage?
    fun extractConversation(root: AccessibilityNodeInfo): ChatContext?
    fun prepareReply(root: AccessibilityNodeInfo): ReplyTarget?
    fun sendReply(root: AccessibilityNodeInfo, target: ReplyTarget, text: String): Boolean
}
```

## v1 适配器

| 适配器 | 包名 | 说明 |
| --- | --- | --- |
| 微信 | `com.tencent.mm` | 通知标题为联系人，正文为消息 |
| QQ | `com.tencent.mobileqq` | 同上，群聊标题含群名 |
| Soul | `cn.soulapp.android` | 通知解析，控件选择器尽量通用 |
| 小红书 | `com.xingin.xhs` | 通知解析，控件选择器尽量通用 |
| 通用 | 未匹配 | 启发式查找输入框和发送按钮 |

如果某 App 更新后无法稳定定位输入框，适配器会降级为“复制建议”，不会盲目点击。

