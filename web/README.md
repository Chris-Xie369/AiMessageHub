# AI 消息助手（iPhone 零构建版）

这是给“只有 iPhone、没有 Mac”准备的 Web/PWA 版本：

1. 把 `web/` 部署到 GitHub Pages 或其他 HTTPS 静态托管。
2. 用 iPhone Safari 打开，选择“分享 → 添加到主屏幕”。
3. 打开后进入“设置”，填写 OpenAI 兼容 `Base URL`、`API Key` 和模型名。
4. 在微信里复制对方消息，打开 AI 消息助手，点“读取剪贴板”或直接粘贴，生成后复制回复。

仓库已包含 [netlify.toml](../netlify.toml)。在 Netlify 导入该仓库后，把 Publish directory 设为 `web`，Build command 留空即可。

想立刻上线而不等待 GitHub：打开 [Netlify Drop](https://app.netlify.com/drop)，把本文件夹直接拖进去即可。

## 为什么点击“生成建议”会提示 Failed to fetch

OpenAI 官方 API 不允许浏览器跨域直连，页面直接请求会触发 CORS，浏览器统一显示为 `Failed to fetch`。

仓库已包含 `netlify/functions/chat.js`，这是一个 Netlify Function，会在服务端转发 OpenAI 兼容请求，从而绕开浏览器 CORS。发布时请确认：

- 使用 Git 导入 Netlify，让 `netlify/functions/chat.js` 随仓库部署；`netlify.toml` 保留 `functions = "netlify/functions"`。
- 设置页勾选“通过 Netlify Function 转发请求”。

DeepSeek 用户：在设置页点“DeepSeek”预设，Base URL 会自动设为 `https://api.deepseek.com`，模型自动设为 `deepseek-chat`，再填入 DeepSeek API Key 即可。

Base URL 只需要填到域名或 `/v1`，例如 `https://api.deepseek.com` 或 `https://api.deepseek.com/v1`；即使误填了完整 `/chat/completions` 地址，程序也会自动修正，不再重复追加。

## 历史对话

- 每个联系人会单独保存最近 20 条对话，存储在 iPhone 浏览器的 localStorage。
- 生成建议时，AI 会自动带上该联系人的历史对话作为上下文。
- 复制某条建议后，会把“对方消息 + 你选择的回复”自动记入历史。
- 也可以在“导入记录”中粘贴格式如下的聊天记录：

```text
对方：在吗
我：在的
对方：晚上一起吃饭吗
```

- “清空”会删除当前联系人的历史；联系人输入框变化时会切换到对应历史。

## Shortcuts 自动化

可以创建一个“快捷指令”：

1. “获取剪贴板”。
2. “打开 URL”：`https://你的域名/index.html?text=<剪贴板>&auto=1`

页面会自动生成建议；点击“复制”后回到微信粘贴发送。

注意：`text` 参数会被 URL 编码，超长内容建议改为手动粘贴。
