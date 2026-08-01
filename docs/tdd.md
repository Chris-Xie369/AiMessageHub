# TDD 开发模式

后续所有修复都按以下顺序执行：

1. **审查**：先读相关代码和最近改动，确认问题根因，不直接改。
2. **红**：为缺陷写一个会失败的测试，证明问题存在。
3. **绿**：写最小修复，让测试通过。
4. **重构**：清理重复或边界逻辑，保持行为不变。
5. **自测**：运行全部相关测试和语法检查，再交付。

## 测试入口

Web/PWA：

```bash
cd web
npm test
node --check app.js
node --check core.js
```

Netlify Function：

```bash
node --check netlify/functions/chat.js
cd web && npm test
```

Android：

```bash
cd android
./gradlew.bat testDebugUnitTest
./gradlew.bat assembleDebugAndroidTest
```

iOS（macOS 环境）：

```bash
cd ios
xcodegen generate
xcodebuild -project AiMessageHub.xcodeproj -scheme AiMessageHub -destination 'platform=iOS Simulator,name=iPhone 16' test
```

## 验收标准

- 修复必须带有失败测试，或明确说明无法自动化测试的原因。
- 提交前重新审查 diff，确认没有无关改动、没有把密钥写进仓库、没有破坏已有功能。
- 涉及 Web 缓存时，需要升级 `web/sw.js` 的 `CACHE` 版本号。

