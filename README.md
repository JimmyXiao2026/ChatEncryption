# Chat Encryption

一个 Fabric mod，复刻 No Chat Reports 的**聊天加密**功能（不含禁用聊天举报），用于 MC **1.21.11**。

> No Chat Reports 自 1.21.5 起不再提供聊天加密，本 mod 补位。

## 安装

1. 把 `ChatEncryption-1.0.0.jar` 放进 `.minecraft/mods/`。
2. 需要 Fabric Loader ≥0.19.5、Fabric API、Java ≥21。
3. 进入游戏，聊天输入框右侧会出现加密按钮。

## 功能（与 No Chat Reports 完全兼容）

- 4 种算法：AES-CFB8（默认）、AES-GCM、AES-ECB、凯撒。
- 配置界面（右键点击聊天框的加密按钮进入，或在加密按钮无效时点击进入）：
  - 选择算法、设置密钥（或从口令派生）、随机生成密钥、是否加密公屏消息。
- 加密开关按钮：正常点击切换开关；按住 Ctrl 发送的消息**不加密**。
- 接收端自动解密，密文消息带 `Encrypted` 图标标识（可在配置中关闭指示器）。
- 配置文件：`config/NoChatReports/NCR-Encryption.json`（沿用旧版 key，可无缝兼容旧 No Chat Reports 的加密消息）。

## 界面（复刻自 1.21.4 No Chat Reports）

- 聊天屏加密按钮（active / inactive / error 三种贴图）
- 加密配置屏（算法选择、密钥/口令输入、公屏加密勾选、校验图标、随机按钮）
- 首次使用警告屏（含"不再显示"勾选与"了解更多"链接）

## 构建

```bash
# 需 JDK 25（fabric-loom 1.18.2 要求）
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
gradlew remapJar
# 产物在 build/libs/chatencryption-1.0.0.jar
```

注意：`gradlew build` 会在 remapSourcesJar 阶段因 JDK25 + JDT/Mercury 问题失败，请用 `remapJar`。

## 验证情况

- 主源码集 + 客户端源码集均编译通过（`compileJava` / `compileClientJava`）。
- `remapJar` 打包成功，jar 内含全部类、4 个客户端 mixin、语言文件、17 张加密贴图、图标与 accesswidener。
- 尚未在游戏内实机加载测试（mixin 注入点已按 1.21.11 API 逐类核对，但首次进游戏请留意是否正常加载）。
