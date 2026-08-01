# Android 构建说明

## 前置条件

- JDK 17
- Android SDK Platform 35
- Gradle 8.11.1（也可以使用仓库内 wrapper）

## 构建

```bash
cd android
gradle testDebugUnitTest
gradle assembleDebug
```

产物位于 `android/app/build/outputs/apk/debug/app-debug.apk`。

## 首次使用

1. 安装 APK 后进入“总览”页，逐项开启通知监听、无障碍服务和悬浮窗权限。
2. 在“设置”页填写 OpenAI 兼容 `Base URL`、`API Key` 和模型名。
3. 在“总览”或“设置”页勾选需要接入的 App（微信、QQ、Soul、小红书）。
4. 收到新消息后，AI 建议会出现在悬浮气泡和“会话”页。

## 已知边界

- 一键发送依赖无障碍控件定位，微信/QQ 更新后选择器可能需要调整。
- 自动回复只对当前位于前台的会话生效，避免误操作其他聊天窗口。
- 群聊默认关闭，需要用户在“设置”中显式开启。

