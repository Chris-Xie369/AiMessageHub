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

仓库已包含 `netlify/functions/chat.js`，这是一个 Netlify Function，会在服务端转发 OpenAI 请求，从而绕开浏览器 CORS。发布时请确认：

- `web/_redirects` 已把 `/api/chat/completions` 代理到 OpenAI，拖 `web` 文件夹部署也有效。
- 使用 Git 导入时，`netlify/functions/chat.js` 会作为后备转发；`netlify.toml` 保留 `functions = "netlify/functions"`。
- 设置页勾选“通过 Netlify Function 转发请求”。

如果部署在非 Netlify 平台且没有该 Function，需要改用支持 CORS 的 OpenAI 兼容服务，或取消勾选并自行配置可用的代理。

## Shortcuts 自动化

可以创建一个“快捷指令”：

1. “获取剪贴板”。
2. “打开 URL”：`https://你的域名/index.html?text=<剪贴板>&auto=1`

页面会自动生成建议；点击“复制”后回到微信粘贴发送。

注意：`text` 参数会被 URL 编码，超长内容建议改为手动粘贴。
