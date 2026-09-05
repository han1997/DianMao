# WebDAV upload 400: diagnose root cause + robust folder auto-create

## Goal

用户反馈 WebDAV 上传失败（400 Bad Request），怀疑远端目录不存在。目标：修复上传 400 错误，并保证远端目录不存在时可靠自动创建。

## What I already know

* 远端目录自动创建**已存在**：`WebDavBackupClient.ensureDirectoryExists()`（backup/WebDavBackupClient.kt:53）上传前对逐级 collection URL 发 MKCOL，201/405 视为成功，其余**静默忽略**（runCatching + 非 fatal）。
* `webDavCollectionUrls`（backup/WebDavUrl.kt:40）按 `/` 拆分路径生成逐级 URL，**无任何百分号编码**。
* `resolveBackupFileUrl` 对 `.json` 结尾的 URL 走 legacy 文件语义，此时 collection 列表为空 → **完全不建目录**。
* PUT 使用 `HttpURLConnection` + `doOutput=true` 直接写流，**未设置 Content-Length** → payload 超过内置缓冲区（4KB）时自动变为 `Transfer-Encoding: chunked`，部分 WebDAV 服务器（老旧 nginx/某些 NAS）对 chunked 返回 400/411。
* URL 含中文/空格时，原样字节进入 HTTP 请求行 → 服务器解析失败 → 400 Bad Request（最常见根因）。
* MKCOL 失败被静默吞掉 → 用户永远看不到"建目录失败"，只会看到 PUT 的错误。

## Root cause hypotheses (ranked)

1. **URL 未百分号编码**（中文/空格目录名）→ 请求行非法 → 400
2. **chunked transfer-encoding 不被服务器支持** → 400/411
3. MKCOL 全部失败且静默 → PUT 得到 409/4xx，错误信息无建目录上下文

## Open Questions

* （已答）用户使用群晖/威联通 NAS；URL 特殊字符情况未确认，但按"必须兼容"处理

## Root cause analysis (Synology/QNAP specific)

群晖 WebDAV Server（Apache 内核）已知行为：

1. **URL 路径未百分号编码** → 请求行含原始中文/空格字节 → Apache 直接返回 **400 Bad Request**。当前 `webDavCollectionUrls`/`resolveBackupFileUrl` 逐段拼接 URL，无编码；`java.net.URL` 不编码非 ASCII。
2. **PUT 无 Content-Length** → payload > 4KB（备份 JSON 必然超过）时 HttpURLConnection 自动 chunked，部分 NAS 固件返回 400/411。
3. MKCOL 静默失败 → 若目录创建真失败，PUT 报 409，用户看不到建目录失败上下文。

## Requirements

* R1: URL 路径逐段百分号编码（保留 `/`、已编码序列不二次编码），应用于 MKCOL 与 PUT/GET 的所有 URL
* R2: PUT 显式设置 Content-Length（写临时字节数组后 `setFixedLengthStreamingMode`），避免 chunked
* R3: 目录自动创建保持逐级 MKCOL；MKCOL 失败仍非 fatal（spec 契约），但 PUT 失败时错误信息保留服务器响应体（已有）
* R4: 纯函数编码逻辑放 `WebDavUrl.kt`（JVM 可测），补 JVM 单测

## Acceptance Criteria

* [ ] 含中文/空格的目录 URL 可正常上传（编码后请求行合法）
* [ ] 远端目录不存在时上传成功（逐级 MKCOL 自动创建）
* [ ] 大 payload（>4KB）上传不再依赖 chunked 传输
* [ ] `WebDavUrl` 编码纯函数有 JVM 单测覆盖（中文、空格、已编码、多级路径）
* [ ] `:app:assembleDebug` 与 `:app:testDebugUnitTest` 通过

## Out of Scope

* WebDAV UI 改版
* 备份格式变更
* Basic auth 之外的认证方式（digest/OAuth）

## Technical Approach

1. `WebDavUrl.kt` 新增 `encodeWebDavPathSegment` / 或对 collection/file URL 逐段编码的纯函数：
   - 按 `/` 拆分路径，逐段 `URLEncoder`-style 编码（空格→`%20`，非 ASCII→UTF-8 百分号）
   - 已含 `%XX` 的段不二次编码（兼容用户直接粘贴编码过的 URL）
   - scheme/authority 不动
2. `webDavCollectionUrls` 与 `resolveBackupFileUrl` 输出前应用编码
3. `WebDavBackupClient.upload`：payload 先 `toByteArray` → `setFixedLengthStreamingMode(bytes.size)` → 写流
4. 单测：`WebDavUrlTest.kt`（新增）

## Decision (ADR-lite)

**Context**: 400 来自群晖 Apache 对未编码请求行/chunked body 的拒绝，而非缺目录（缺目录是 409）。
**Decision**: 修编码 + fixed-length 传输，MKCOL 保持非 fatal；不引入 OkHttp 等新依赖，继续用 HttpURLConnection。
**Consequences**: 兼容 legacy `.json` 文件 URL 与目录 URL 两种形态；编码函数需处理"已编码不重复编码"边界；无网络层依赖变化，风险低。

## Technical Notes

* 相关文件：`backup/WebDavBackupClient.kt`、`backup/WebDavUrl.kt`
* Spec：`.trellis/spec/backend/error-handling.md`（WebDAV Backup Transport 契约）、`logging-guidelines.md`（不得记录凭据/URL 隐私）
* JVM 纯函数测试可用（WebDavUrl.kt 无 Android 依赖）；org.json 不可用于 JVM 测试
