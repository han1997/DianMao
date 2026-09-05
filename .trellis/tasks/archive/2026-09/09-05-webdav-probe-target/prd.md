# WebDAV 400: target is legacy file not directory, probe before upload

## Goal

修复真机 QNAP NAS 上传 400 Bad Request：配置的 URL `/webdav/DianMao/` 指向的远端资源实际是**旧版备份文件**（v3 JSON，15KB），不是目录。目录语义代码往"文件内部"PUT/MKCOL 必然 400。

## Root cause（已用 curl 从 PC 直连 NAS 复现验证）

* `PROPFIND /webdav/DianMao/` → `resourcetype` 为空（文件）、`getcontentlength` 15024、内容是 `backupVersion: 3` 的旧备份
* `PUT /webdav/DianMao/dianmao-backup-latest.json` → 400 Forbidden（父节点是文件）
* `MKCOL /webdav/DianMao/test-sub/` → 400 Method Not Allowed
* `PUT /webdav/DianMao`（无斜杠，文件语义）→ **201 Created** ✓
* 编码不是问题（URL 全 ASCII）；上一任务（percent-encoding + fixed-length）的修复仍正确但未命中本次根因

## Requirements (v2 — 用户澄清：期望远端是目录 DianMao/xxx，而非文件覆盖)

* R1: 上传前 `PROPFIND Depth: 0` 探测配置 URL 的远端类型（合并为单次探测：NOT_FOUND / FILE / COLLECTION）
* R2 (v2): 远端是**文件**时执行一次性迁移：GET 下载并校验内容是 DianMao 备份（含 `dianmao.tasks` 格式标记）→ DELETE 该文件 → MKCOL 建目录链 → 按目录语义 PUT 备份到目录内。校验失败（非备份文件）→ 抛 IOException 拒绝迁移，绝不删除未知文件
* R3: 目录形态 / 404 不存在 → 现行为不变（逐级 MKCOL + PUT 到目录内固定文件名）
* R4: 下载保持文件回退（远端是文件时 GET 该 URL 本身），只读安全
* R5: legacy `.json` URL 跳过探测，直接文件语义（不变）
* R6: 探测/迁移失败（网络/5xx/403）→ 抛 IOException 中止
* R7: 纯函数（propfindBodyIsCollection、looksLikeDianmaoBackup）JVM 可测

## Acceptance Criteria (v2)

* [ ] NAS 上 `/webdav/DianMao`（文件）在上传后变为目录 `/webdav/DianMao/`，内含 `dianmao-backup-latest.json`
* [ ] 非备份文件占据 URL 时上传报错且不删除该文件
* [ ] 目录/不存在场景回归正常
* [ ] 真机上传验证成功

## Out of Scope

* NAS 上旧 v3 备份文件的自动迁移（上传 v4 备份后自然覆盖为最新）
* 认证方式变更

## Technical Notes

* `WebDavBackupClient.kt`：新增 `probeCollection(settings, url): Boolean`（PROPFIND Depth:0，200+multistatus 解析 resourcetype / 404 → not exists / 403 等其他 → 失败）
* `WebDavUrl.kt`：新增纯函数 `isCollectionResponse(multistatusXml): Boolean` 或更轻量的 `parsePropfindIsCollection`
* 探测目标 = 规范化后的"用户配置 URL"（目录形态带 trailing slash），不是 resolveBackupFileUrl 结果
* spec：error-handling.md WebDAV 契约需补充"探测语义"
* 凭据仅存于设备 SharedPreferences，排查过程已用 run-as 读取；不得写入代码/日志/提交
