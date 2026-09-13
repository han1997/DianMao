# 自动备份

## Goal

让 WebDAV 备份从纯手动升级为可自动执行：应用启动时自动把最新数据上传到已配置的 WebDAV 目录，减少"忘记备份导致数据丢失"的风险。

## Requirements

* 自动备份开关（`autoBackupEnabled`，默认关闭，用户在备份面板开启）
* 应用启动触发：WebDAV 已配置 + 开关开启 + 距上次自动备份超过 24 小时 → 后台协程上传 `exportBackupJson()`（复用手动上传的传输层）
* `lastAutoBackupAtMillis` + `lastAutoBackupSucceeded` 持久化（SharedPreferences）；失败静默（下次启动重试，不更新成功时间戳）
* 备份面板：自动备份开关行 + "上次自动备份"状态展示（时间/结果），手动上传/恢复/设置流程不变
* 未配置 WebDAV 或开关关闭时不自动上传
* 零新依赖（不引入 WorkManager）

## Acceptance Criteria

* [ ] 自动备份默认关闭；开启后启动时满足条件自动上传
* [ ] 未配置 WebDAV 时不自动上传
* [ ] 失败静默，成功时间戳不更新则下次启动重试
* [ ] 24 小时内重复启动不重复上传
* [ ] 手动上传/恢复行为不变
* [ ] lint / 单测 / assembleDebug 全绿

## Definition of Done

* 单测新增/更新并通过
* lint / typecheck / assembleDebug 绿
* CHANGELOG.md 追加、README 必要处同步
* 版本号 bump（项目硬规则）

## Technical Approach

* `BackupSettingsStore` 扩展：`autoBackupEnabled` / `lastAutoBackupAtMillis` / `lastAutoBackupSucceeded`（SharedPreferences，与 WebDAV 设置同文件）
* 触发逻辑为纯函数（可测）：`shouldAutoBackupNow(enabled, configured, lastAt, now, intervalMillis)` → Boolean
* MainActivity（DianMaoApp 组合区）`LaunchedEffect` 启动时检查 → 后台协程：exportBackupJson → upload → 成功写时间戳；静默 catch
* BackupPanel 新增"自动备份"开关行（QuietSwitch）+ 上次自动备份状态行
* 上传路径复用 `WebDavBackupClient.upload(settings, payload)`（已含 MKCOL/PROPFIND/迁移健壮性）

## Decision (ADR-lite)

**Context**: WebDAV 备份仅手动；需决定调度机制与失败反馈。
**Decision**: 应用启动触发（24h 间隔）+ 静默失败 + 面板展示上次自动备份状态；不引入 WorkManager。
**Consequences**: 零新依赖；数据只在应用运行时变化，启动触发已覆盖数据安全；失败自愈（时间戳不更新则重试）；BackupPanel 需承载开关与状态展示。

## Out of Scope

* 复盘/统计视图（任务 4）
* 桌面小组件（任务 5）
* 备份文件轮转/多版本保留（始终覆盖 dianmao-backup-latest.json）
* 自动恢复（自动上传 only）

## Technical Notes

* 已核实：BackupSettingsStore.kt（SharedPreferences "backup_settings"）、MainActivity.kt 备份流程（:916-950）、BackupPanel.kt 上传入口（:320）、WebDavBackupClient（error-handling spec 契约）
* 上传静默失败需注意 error-handling spec：网络工作在 Dispatchers.IO、失败不改 Room 数据
* 上次自动备份成功时间戳在 upload 成功后写入（`apply()` 异步持久化即可）
* 项目硬规则：每次代码变更会话必须 bump 应用版本
