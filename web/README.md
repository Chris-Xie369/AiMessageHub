# AI 消息助手（iPhone 零构建版）

这是给“只有 iPhone、没有 Mac”准备的 Web/PWA 版本：

1. 把 `web/` 部署到 GitHub Pages 或其他 HTTPS 静态托管。
2. 用 iPhone Safari 打开，选择“分享 → 添加到主屏幕”。
3. 打开后进入“设置”，填写 OpenAI 兼容 `Base URL`、`API Key` 和模型名。
4. 在微信里复制对方消息，打开 AI 消息助手，点“读取剪贴板”或直接粘贴，生成后复制回复。

仓库已包含 [netlify.toml](../netlify.toml)。在 Netlify 导入该仓库后，把 Publish directory 设为 `web`，Build command 留空即可。

想立刻上线而不等待 GitHub：打开 [Netlify Drop](https://app.netlify.com/drop)，把本文件夹直接拖进去即可。

## Shortcuts 自动化

可以创建一个“快捷指令”：

1. “获取剪贴板”。
2. “打开 URL”：`https://你的域名/index.html?text=<剪贴板>&auto=1`

页面会自动生成建议；点击“复制”后回到微信粘贴发送。

注意：`text` 参数会被 URL 编码，超长内容建议改为手动粘贴。
