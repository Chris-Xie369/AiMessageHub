# AiMessageHub（AI 消息中枢）

一款本地优先的 AI 消息中转站 MVP：把微信、QQ、Soul、小红书等社交 App 的消息收进一台设备，由用户自带的 OpenAI 兼容 API 生成回复建议，并按用户授权执行“仅建议 / 一键发送 / 白名单自动回复”。

## 目录

- `android/`：Android 原生实现（Kotlin + Jetpack Compose + Room）
- `ios/`：iOS 原生实现（SwiftUI + Share/Keyboard 扩展 + Shortcuts）
- `docs/`：架构、权限与适配器说明

## Android 快速开始

```bash
cd android
./gradlew.bat assembleDebug   # Windows
./gradlew.bat testDebugUnitTest
```

首次使用需要在系统设置中开启：

1. 通知使用权
2. 无障碍服务
3. 悬浮窗

## 只有 iPhone 怎么用

iOS 受系统沙盒限制，无法像 Android 一样读取微信内部聊天并自动点击发送，但可以这样做：

1. 微信/QQ 里长按消息，选择“分享”到 AI 消息中枢，生成建议后复制。
2. 复制聊天内容，打开 AI 消息中枢“建议”页自动读取剪贴板并生成回复。
3. 启用 AI 键盘后，在输入框切到该键盘，点“分析剪贴板”并一键插入。
4. 用“快捷指令”创建自动化：打开微信时读取剪贴板、生成回复、复制结果。

如果你连 Mac 也没有，优先使用 [web/](web/) 里的 PWA：部署到 GitHub Pages 后直接添加到主屏幕，不需要 Xcode，也不需要 Apple Developer 账号。详细步骤见 [docs/iphone-no-mac.md](docs/iphone-no-mac.md)。

也支持 Netlify：仓库根目录已带 `netlify.toml`，在 Netlify 导入仓库后设置 Publish directory 为 `web`，Build command 留空，即可自动部署到 HTTPS。

## iOS 快速开始

使用 [XcodeGen](https://github.com/yonaskolb/XcodeGen) 生成工程：

```bash
cd ios
xcodegen generate
open AiMessageHub.xcodeproj
```

## 当前状态

- Android：主流程已实现，单元测试 10 个全部通过，`assembleDebug` 和 `assembleDebugAndroidTest` 构建成功。
- iOS：主 App、AI 建议页、Share Extension、Keyboard Extension、Shortcuts Intent 与测试源码已落地；仓库包含 macOS CI，可在 GitHub Actions 中自动生成工程并构建验证，真机安装仍需在 Xcode 中配置签名。
- 本仓库内置 Gradle Wrapper，wrapper 使用腾讯镜像下载 Gradle 8.11.1。

## 设计原则

- 消息默认只保存在本机，不上传云端。
- 只有当前会话文本会发送到用户配置的 AI 服务。
- 默认私聊采集，群聊默认关闭。
- 不 Root、不逆向、不调用私有协议，不批量群发。
