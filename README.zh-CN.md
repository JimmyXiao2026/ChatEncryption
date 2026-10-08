# Chat Encryption

一个 Fabric mod，复刻 [No Chat Reports](https://modrinth.com/mod/no-chat-reports)（NCR）的**聊天加密**功能（不含禁用聊天举报），用于 MC **1.21.11**。

> No Chat Reports 自 1.21.5 起不再提供聊天加密，本 mod 补位，且与 NCR 的加密格式完全兼容。

[English](README.md) · **中文（简体）**

## 功能

- **4 种加密算法**：AES-CFB8（默认）、AES-GCM、AES-ECB、凯撒。
- **多密钥支持**：可保存多个密钥，每个密钥可附带**备注**；选择用哪个密钥发送，删除不再需要的密钥。
  - 配置屏的"密钥"按钮打开**密钥管理子页面**：添加密钥（手动输入 / 随机生成 / 由口令派生，均可在密钥输入框上方填备注），并在列表中**选择**、**删除**密钥。
- **解密模式**：
  - 默认：接收消息时**尝试所有已存密钥**，任一能解出即解密。
  - 可勾选 **"仅解密当前密钥"**（默认关闭）：只尝试当前选中的活动密钥。
  - 可勾选 **"关闭时保持解密"**（默认开启）：关闭加密时仍解密服务器聊天。
- 聊天输入框右侧有加密开关按钮；**按住 Ctrl 发送的消息不加密**。
- 接收端自动解密，密文消息带 `Encrypted` 图标标识（可在配置中关闭指示器）。
- 配置文件位于 `config/NoChatReports/NCR-Encryption.json`，旧版 NCR 密钥无缝沿用。

## 安装

1. 把 `ChatEncryption-1.0.0.jar` 放进 `.minecraft/mods/`。
2. 需要 Fabric Loader `>=0.19.5`、Fabric API、Java `>=21`。
3. 进入游戏，聊天输入框右侧会出现加密按钮。

## 使用

- **切换加密**：点击聊天输入框旁的按钮。
- **配置**：右键点击该按钮（或无有效密钥时点击）进入配置屏。
- **管理密钥**：在配置屏点击"密钥"按钮打开子页面：
  - 在**备注**输入框填备注，在密钥输入框填密钥（或点骰子随机生成，或输入口令派生），再点**添加**。
  - 在列表中点**选择**设为发送用密钥，或点**删除**移除。
- 按住 **Ctrl** 发送消息即以明文发送。

## 与 No Chat Reports 的兼容性

- 沿用同一配置文件（`config/NoChatReports/NCR-Encryption.json`）。
- 使用相同的加密输出格式（`#%` 前缀、Base64R 打乱、PBKDF2 口令派生）。
- 只要密钥与算法相同，本 mod 与 NCR 1.21.4 的加密消息可互相解密。

## 构建

```bash
# 需 JDK 25（fabric-loom 1.18.2 要求）
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
gradlew remapJar
# 产物在 build/libs/chatencryption-1.0.0.jar
```

注意：`gradlew build` 会在 `remapSourcesJar` 阶段因 JDK25 + JDT/Mercury 问题失败，请用 `remapJar`。

## 多语言

内置 **51 个语言文件**（含文言 `lzh`），以 `en_us` 为基准生成；未手译的键自动回退英文。

## 许可证

MIT
