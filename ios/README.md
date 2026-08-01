# iOS 工程

使用 [XcodeGen](https://github.com/yonaskolb/XcodeGen) 生成 Xcode 工程：

```bash
cd ios
xcodegen generate
open AiMessageHub.xcodeproj
```

## Target

- `AiMessageHub`：主 App，包含“AI 建议”页、历史记录和设置。
- `AiMessageHubShareExtension`：在其他 App 分享文本后生成回复建议并复制。
- `AiMessageHubKeyboardExtension`：切换键盘后分析剪贴板并一键插入建议。
- `AiMessageHubTests`：Prompt 与建议引擎单元测试。

## 只有 iPhone 的四种用法

1. 微信/QQ 里长按消息或文本，选择“分享”，再选“AI 消息中枢”，生成建议后点“复制”。
2. 在微信里复制对方消息，回到 AI 消息中枢的“建议”页，App 会自动读取剪贴板，点“生成”后复制回复。
3. 在系统设置中启用 AI 消息中枢键盘并打开“完全访问”，聊天输入时切到该键盘，点“分析剪贴板”，再点建议直接插入。
4. 在“快捷指令”App 中创建自动化：打开微信时“获取剪贴板”，调用“分析剪贴板生成回复”，然后复制结果；回到微信直接粘贴发送。

## 首次运行

1. 在 Signing & Capabilities 中选择自己的 Team。
2. 确认主 App 与两个扩展都启用 App Groups：`group.com.aimessagehub.app`。
3. 键盘扩展需要在系统设置中开启“完全访问”才能联网和读取剪贴板。
4. 在主 App“设置”中填写 OpenAI 兼容 `Base URL`、`API Key` 和模型名。

iOS 不做跨 App 自动发送；建议内容由用户手动复制或插入。

## 没有 Mac 也能构建

仓库已包含 [iOS CI](../.github/workflows/ios.yml)：推送到 GitHub 后会在 macOS runner 上自动安装 XcodeGen、生成 Xcode 工程、构建模拟器版本并运行单元测试。真机安装仍需使用 Xcode 配置自己的 Team 和签名。

如果不打算构建原生 App，可直接使用 [web/](../web/) 的 iPhone PWA，零构建、零签名成本。
