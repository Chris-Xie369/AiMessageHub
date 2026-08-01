# 只有 iPhone、没有 Mac 的三种方案

## 方案一：Web/PWA（推荐，今天就能用）

不需要 Xcode、不需要 Mac、不需要 Apple Developer 账号。

1. 把 `web/` 目录部署到 GitHub Pages 或其他 HTTPS 静态托管；仓库已带 [web.yml](../.github/workflows/web.yml)。
2. 用 iPhone Safari 打开部署后的地址，点“分享 → 添加到主屏幕”。
3. 打开 PWA，在“设置”里保存 OpenAI 兼容 `Base URL`、`API Key` 和模型。
4. 在微信复制对方消息，打开 PWA，点“读取剪贴板”或粘贴，点“生成建议”，再点“复制”回到微信发送。

也可以建一个快捷指令：获取剪贴板后打开

```text
https://你的域名/index.html?text=<剪贴板>&auto=1
```

页面会自动生成建议。

### 用 Netlify 部署

临时最快：打开 [Netlify Drop](https://app.netlify.com/drop)，把本仓库的 `web/` 文件夹直接拖进去，Netlify 会立即生成 HTTPS 地址，不需要 GitHub。

1. 登录 [Netlify](https://www.netlify.com)，选择“Add new site → Import an existing project”。
2. 选择 GitHub，并授权 Netlify 访问 `AiMessageHub` 仓库。
3. Build command 留空，Publish directory 填 `web`。
4. 点击 Deploy，Netlify 会给出 HTTPS 地址；之后每次推送 GitHub 都会自动重新部署。

仓库根目录的 `netlify.toml` 已把发布目录设置为 `web`，也配置了 service worker 和首页的缓存策略。

### Failed to fetch 怎么处理

这是浏览器 CORS 问题：OpenAI/DeepSeek 等 API 不允许网页直接跨域请求。仓库已带 `netlify/functions/chat.js`，用 Netlify Function 在服务端转发请求，并支持任意 OpenAI 兼容 Base URL。

修复后请确认：

- Netlify 使用 Git 导入，确保 `netlify/functions/chat.js` 被部署。
- 设置页“通过 Netlify Function 转发请求”保持勾选。
- 使用 DeepSeek 时，在设置页点“DeepSeek”预设，或手动把 Base URL 填为 `https://api.deepseek.com`、模型填为 `deepseek-chat`。
- 保存设置后重新点“生成建议”。

如果出现 `HTTP 405: Method Not Allowed`，通常是 Base URL 被填成了完整 `/chat/completions` 地址导致路径重复。现在程序会自动修正，请把 Base URL 填为 `https://api.deepseek.com` 或 `https://api.deepseek.com/v1` 后再试。

## 方案二：纯快捷指令（不依赖本项目部署）

即使完全不构建 App，也可以在“快捷指令”里手动搭一个 AI 回复器：

1. 新建快捷指令，加入“获取剪贴板”。
2. 加入“获取 URL 内容”：
   - URL：`https://api.openai.com/v1/chat/completions`
   - 方法：`POST`
   - Headers：`Content-Type: application/json`、`Authorization: Bearer <你的 API Key>`
   - 请求体 JSON：
     ```json
     {
       "model": "gpt-4o-mini",
       "messages": [
         {"role": "system", "content": "你是用户的聊天助手，给出 3 个简短回复候选，用 --- 分隔"},
         {"role": "user", "content": "对方说：<剪贴板内容>"}
       ]
     }
     ```
3. 用“获取字典值”逐层取出 `choices` → 第一项 → `message` → `content`。
4. 最后“复制到剪贴板”或“显示结果”。

这个方案适合个人使用；API Key 只保存在你的快捷指令和手机本地。

## 方案三：云端构建原生 iOS App

如果一定要安装原生 App：

1. 需要付费 Apple Developer 账号（每年 99 美元）。
2. 把仓库推到 GitHub，使用 macOS runner 云端构建；仓库已带 [ios.yml](../.github/workflows/ios.yml) 做构建和测试。
3. 在 Apple Developer 后台创建 App ID、开启 App Groups，并准备 Distribution 证书和 Provisioning Profile。
4. 使用 TestFlight 或 Ad Hoc 分发安装到 iPhone。

真机安装无法绕过 Apple 签名，但不需要你本地有一台 Mac。

## iPhone 的边界

- iPhone 不允许第三方 App 读取微信内部聊天内容，也不能替用户自动点击“发送”。
- 能稳定做到的是：分享文本、读取剪贴板、生成建议、复制/插入。
- 不建议使用需要 Root、越狱、注入微信或绕过系统沙盒的方案。
