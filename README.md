# Chat Encryption

A Fabric mod that replicates the **chat encryption** feature of [No Chat Reports](https://modrinth.com/mod/no-chat-reports) (NCR) for Minecraft **1.21.11** — without the chat-report disabling part.

> No Chat Reports removed chat encryption starting in 1.21.5. This mod fills that gap while staying fully compatible with NCR's encryption format.

[中文 (简体)](README.zh-CN.md) · **English**

## Features

- **4 encryption algorithms**: AES-CFB8 (default), AES-GCM, AES-ECB, and Caesar.
- **Multi-key support**: store several keys, each with an optional **note**, select which key to send with, and delete keys you no longer need.
  - Encryption config screen now opens a **key-management sub-screen** (via the "Key" button): add keys (manual input, random, or derived from a passphrase) with a note, and pick/delete saved keys.
- **Decryption modes**:
  - By default, incoming messages are tried against **every saved key** (any of them can decrypt).
  - Optional **"Only decrypt active key"** toggle to decrypt with just the selected key.
  - Optional **"Keep decrypting when disabled"** (default ON): keeps decrypting server chat even when encryption is off.
- Encryption toggle button next to the chat input; **hold Ctrl** while sending to send the message **unencrypted**.
- Incoming encrypted messages are automatically decrypted and tagged with an `Encrypted` indicator (can be hidden in config).
- Config lives in `config/NoChatReports/NCR-Encryption.json`, so existing NCR encryption keys carry over seamlessly.

## Install

1. Put `ChatEncryption-1.0.0.jar` into `.minecraft/mods/`.
2. Requires Fabric Loader `>=0.19.5`, Fabric API, and Java `>=21`.
3. Launch the game — an encryption button appears next to the chat input.

## Usage

- **Toggle encryption**: click the button next to the chat input.
- **Configure**: right-click the button (or click it when no valid key is set) to open the config screen.
- **Manage keys**: in the config screen, click the **Key** button to open the key-management sub-screen:
  - Type a **note** and a **key** (or press the dice to randomize, or enter a passphrase to derive a key), then **Add**.
  - In the list, **Select** the key to send with, or **Delete** a key.
- Hold **Ctrl** while sending a message to send it in plaintext.

## Compatibility with No Chat Reports

- Uses the same config file (`config/NoChatReports/NCR-Encryption.json`).
- Uses the same encryption output format (`#%` prefix, Base64R scrambling, PBKDF2 passphrase derivation).
- Messages encrypted by NCR 1.21.4 can be decrypted by this mod, and vice versa, as long as the same key and algorithm are used.

## Build

```bash
# Requires JDK 25 (fabric-loom 1.18.2 requirement)
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
gradlew remapJar
# Output: build/libs/chatencryption-1.0.0.jar
```

Note: `gradlew build` fails in the `remapSourcesJar` step due to a JDK25 + JDT/Mercury issue — use `remapJar` instead.

## Localization

Ship with **51 language files** (including 文言 / Classical Chinese, `lzh`), generated from an `en_us` base. Keys not hand-translated fall back to English.

## License

MIT
