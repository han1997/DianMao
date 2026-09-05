# WebDAV folder backup and readable format (WebDAV 目录备份 + 可读备份格式)

## Goal

修复 WebDAV 备份的两个问题：
1. **URL 是文件夹**：用户配置的 WebDAV URL 是目录（如 `https://dav.jianguoyun.com/dianmao/`），当前代码直接 PUT/GET 该 URL 会失败。备份应写入目录内的固定文件，目录不存在时自动创建。
2. **备份格式不利于解析**：当前 JSON 面向 Room 恢复（枚举 name、裸毫秒、无元数据），人工/外部工具难以阅读。升级格式使其自描述、可读、可校验。

## What I already know

- `WebDavBackupClient`（backup/WebDavBackupClient.kt）：仅 PUT/GET `settings.url`，无目录处理、无 MKCOL、无 URL 规范化
- `WebDavSettings(url, username, password)`：url 语义当前是"备份文件 URL"（UI placeholder 也这么写）
- `TaskBackupCodec`：format=dianmao.tasks, backupVersion=3；字段：tasks[]（枚举 name、毫秒时间戳、subTasks 内嵌）、completions[]；decode 校验 format + backupVersion<=3
- `TaskRepository.exportBackupJson()/importBackupJson(json)`：编解码入口
- `BackupPanel`：URL 字段 label "备份文件 URL"，placeholder 提示 backup.json；状态行 string-match（"失败"/"上传"/"下载"…）— 必须保留该契约
- `MainActivity` onUploadWebDav/onRestoreWebDav：调用 client.upload/download + repository.export/import
- spec 契约（error-handling.md WebDAV Backup Transport）：PUT/GET 手动连接、Basic auth、IO dispatcher、失败不破坏本地数据、下载先解码后删行 — 全部保持
- manifest 已开 usesCleartextTraffic（LAN http 可用）
- 无 WebDavBackupClient/TaskBackupCodec 的现有单测；org.json 是 Android SDK 内置（JVM 测试不可用，spec 明确禁止）

## Requirements

### 1. WebDAV 目录语义（folder-aware backup）

- **URL 规范化**：`WebDavSettings.url` 语义升级为"备份目录 URL"（兼容旧的完整文件 URL）
  - 新增纯函数 `resolveBackupFileUrl(url: String, fileName: String): String`：
    - URL 以 `.json` 结尾（忽略 query）→ 视为完整文件 URL，直接使用（向后兼容）
    - 否则视为目录：确保以 `/` 结尾后拼接 fileName
- **固定备份文件名**：`dianmao-backup-latest.json`（目录模式下上传/下载都用它）
- **目录自动创建**：上传前对目录 URL 发 `MKCOL`（WebDAV 标准建目录方法）：
  - MKCOL 返回 201/405（已存在）→ 继续
  - MKCOL 失败（403/409 等）→ 不阻塞：很多服务器目录已存在或不允许 MKCOL，直接尝试 PUT，由 PUT 的错误状态决定成败
  - 支持多级路径：逐级 MKCOL（如 `/a/`、`/a/b/`），405 视为已存在跳过
- **下载**：目录模式 GET `目录 + dianmao-backup-latest.json`；文件模式 GET 原样
- **UI 文案更新**：URL label 改为"备份目录 URL"，placeholder 改为目录示例（`https://dav.example.com/dianmao/`），说明文字提示"以 .json 结尾视为完整文件路径，否则视为目录"
- **状态头卡**：`webDavHost()` 显示逻辑保持，目录模式下显示 host + 目录路径

### 2. 备份格式 v4（readable format）

`TaskBackupCodec` 升级 backupVersion 3 → 4：

- **顶层元数据**（新增）：
  ```json
  {
    "format": "dianmao.tasks",
    "backupVersion": 4,
    "databaseVersion": 9,
    "createdAtMillis": 1757...,
    "createdAt": "2026-09-05T12:30:00+08:00",   // 新增 ISO-8601 可读时间
    "app": { "versionName": "1.0", "platform": "android" },  // 新增
    "summary": { "taskCount": N, "completionCount": M, "archivedCount": K },  // 新增
    "tasks": [...],
    "completions": [...]
  }
  ```
- **task 对象增强**（保留全部现有字段，新增可读字段）：
  - `quadrantLabel`（如"重要且紧急"）、`repeatRuleLabel`（如"每周"）— 枚举 name 保留（机器解析稳定），label 并列（人类可读）
  - `createdAt` / `updatedAt` ISO-8601 字符串并列毫秒值
  - `dueAt` ISO-8601 并列 `dueAtMillis`
- **completion 对象增强**：`completedAt` ISO-8601 并列
- **解码兼容**：
  - `backupVersion <= 4` 均可导入（v3 备份文件继续可用）
  - 新增字段全部 optional：解码用 `optString`/`has` 判断，缺失时回退现有逻辑
  - 枚举解析失败 → 报错拒绝（现有行为不变）
- **格式自描述**：顶层加 `"fieldNotes"` 说明对象（可选，简短描述主要字段含义）— 若实现中发现冗余可省略，以 summary + ISO 时间为核心

### 3. 约束

- 不改 `WebDavSettings` 数据类字段结构（url/username/password 不变，仅 url 语义升级）
- `status: String?` string-match 契约保留（新增状态文案需包含既有关键词："上传"/"下载"/"失败"/"导出"/"设置已保存"）
- 网络全部 Dispatchers.IO；失败不破坏本地数据；下载先解码后删行
- 本地导出/导入（SAF 文件）不受影响，同样产出 v4 格式

## Acceptance Criteria

- [ ] 目录 URL（无 .json 后缀）上传：自动逐级 MKCOL + PUT 到 `目录/dianmao-backup-latest.json`
- [ ] 目录 URL 下载：GET `目录/dianmao-backup-latest.json`
- [ ] 完整文件 URL（.json 结尾）行为与现在一致（向后兼容）
- [ ] MKCOL 405（已存在）不报错；MKCOL 失败不阻塞 PUT 尝试
- [ ] v4 备份含 createdAt ISO 时间、app 元数据、summary 统计、task/completion 可读字段
- [ ] v3 备份文件仍可导入（向后兼容）
- [ ] v4 备份可导入（自导自洽）
- [ ] UI 文案更新（目录 URL 提示）
- [ ] `.\gradlew.bat :app:testDebugUnitTest --no-daemon` 通过
- [ ] `.\gradlew.bat :app:lintDebug --no-daemon` 通过
- [ ] `.\gradlew.bat :app:assembleDebug --no-daemon` 通过

## Definition of Done

- 三项 Gradle 检查全绿
- spec 同步：error-handling.md WebDAV 契约（MKCOL、目录语义、固定文件名）、database-guidelines.md 备份格式 v4
- CHANGELOG 更新

## Out of Scope

- 备份历史多版本文件（latest 单文件）
- 自动定时备份
- 加密备份
- 恢复冲突合并策略（保持现有全量替换）

## Technical Notes

- `resolveBackupFileUrl` 放 `ui/logic/PlatformHelpers.kt`？不 — 它是 backup 域纯函数，放 `backup/WebDavBackupClient.kt` 同文件或 `backup/WebDavUrl.kt` 新文件（可 JVM 测试，无 Android 依赖）
- MKCOL 逐级实现：解析 URL path，按 `/` 分段累积，对每级发 MKCOL；405 = 已存在
- ISO 时间用 `java.time.OffsetDateTime.now().toString()`（minSdk 31，java.time 可直接用）
- codec 新字段解码全部 `opt*` 系列，避免破坏 v3
- UI 状态行新增文案示例："WebDAV 备份已上传"（含"上传"✓）、"已从 WebDAV 恢复 N 个事项"（含"下载"语义需检查 — 现文案无"下载"二字但 string-match 有"从 WebDAV 恢复"分支 ✓）
