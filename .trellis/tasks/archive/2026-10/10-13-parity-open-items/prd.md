# PRD: 关闭像素级对齐遗留项 — 版本号动态来源 + 备份 URL 辅助行

来源:任务 `10-12-pixel-parity-ui`(已归档)验收阶段登记的两条遗留项,用户 2026-10-13 决定全部处理。

## 背景

像素级对齐任务收尾时,`SettingsScreen` hero 的版本号写死为 `v1.1.2 · The Day Compass`(与原型一致),
WebDAV URL 字段有一行原型没有的 `supportingText`。两者都需要「先改原型 → 用户确认 → 再改 Compose」
(见 `.trellis/spec/frontend/prototype-first.md` 第 0 节)。本任务按该流程关闭它们。

## 目标(两项)

### A. 版本号改为动态来源

- 原型:`design/mockup/app.js` 设置页 hero 与「关于 DianMao」弹层中的 `v1.1.2` 是写死文案;
  改为**版本令牌占位符**(如 `__APP_VERSION__`),由 `app.js` 在渲染时替换为演示版本号,
  或明确标注「运行时由 `BuildConfig.VERSION_NAME` 注入,原型固定显示示例值」。
- 实现:`SettingsScreen.kt:184` 的 `"v1.1.2 · The Day Compass"` 改为读取构建版本号,
  格式维持 `v{versionName} · The Day Compass`。
- 需开启 `buildConfig = true`(AGP 8.13.1 下 `BuildConfig.VERSION_NAME` 需显式开启)
  或在构建脚本注入 `resValue`/`BuildConfigField`;实现时确认与 `gradle.properties` 的
  `app.versionName` 同源,避免再次漂移。

### B. 备份 URL 辅助行严格对齐原型

- 决策:**严格对齐**——先在原型补上该行,再保留实现的 `supportingText`。
- 原型:`design/mockup/app.js` `backupContent()` 的「备份目录 URL」`.field` 下补一行说明文案,
  与实现一致:`备份写入目录内 dianmao-backup-latest.json;目录不存在会自动创建`。
- 原型需同时定义该说明行的样式(沿用既有 `.field` 下方 hint 语义,字号 12px / 行高 16px,
  颜色 `--on-surface-variant` 加强档),保证 Compose `QuietField.supportingText` 与之一致。
- 实现侧已存在 `QuietField(supportingText=...)`(`BackupPanel.kt:251`),预计无需改动;
  仅需按原型核对文案、间距(6dp)与颜色。

## 范围

- 原型:`design/mockup/app.js`(+ 必要时 `style.css`)。
- 实现:`SettingsScreen.kt`、可能涉及 `app/build.gradle.kts`(buildConfig)。
- 不改业务逻辑、数据模型。

## 非目标

- 不重构设置页结构;不新增其他设置项。
- 不处理原型中其余 `v1.1.2` 之外的历史文案。

## 验收标准

- [ ] 原型 hero 与「关于」弹层的版本号不再是会被误当作真值的写死字符串(占位符或明确标注)。
- [ ] 原型「备份与恢复」页 URL 字段下方存在说明行,且样式与实现一致。
- [ ] `SettingsScreen` 显示 `v{BuildConfig.VERSION_NAME} · The Day Compass`,与 `gradle.properties` 同源。
- [ ] 备份 URL 辅助行与原型逐字、逐样式一致;更新 `BASELINE.md` 记录原型变更与新 SHA。
- [ ] `:app:assembleDebug` + `:app:lintDebug` + `:app:testDebugUnitTest` 通过。
- [ ] 明暗两主题并排图更新(设置页、备份页)。

## 已确认决策(2026-10-13)

- A 项版本号占位符:**方案 1** —— `app.js` 新增单一常量 `APP_VERSION`;标记中写字面量
  `__APP_VERSION__` 占位符,渲染时由 `withVersion()` 替换(hero 与「关于」弹层均用)。
  实现侧读 `BuildConfig.VERSION_NAME`。
  额外:`bumpVersion` Gradle 任务同步改写 `app.js` 的 `APP_VERSION`,两者不会漂移。
- B 项:**严格对齐** —— 在原型补上该说明行(不改实现侧已有的 `supportingText`)。

## 实施记录(2026-10-13)

原型(`design/mockup/`):
- `app.js`:`const APP_VERSION = '1.2.3';` + `withVersion()`;hero 与「关于 DianMao」弹层
  改用 `__APP_VERSION__` 占位符,`render()` 与 `sheet()` 两处入口做替换。
- `app.js`:`backupContent()` 的 URL 字段包进 `.field-group`,下方补 `.hint` 说明行
  (padding `6px 0 0 4px`,文案与实现逐字一致)。
- `style.css`:新增 `.field-group{display:flex;flex-direction:column}`(内部 6px、
  组间沿用 `.fields` 的 10px,与实现 `QuietField` + `FormSettingBlock` 间距一致)。
- `BASELINE.md`:登记 v1.6 变更记录与新文件 SHA-256。

实现(`app/`):
- `app/build.gradle.kts`:`buildFeatures { buildConfig = true }`;`bumpVersion` 同步 `APP_VERSION`。
- `SettingsScreen.kt:184`:`"v${BuildConfig.VERSION_NAME} · The Day Compass"`。
- `BackupPanel.kt:251`:`supportingText` 保持不变(与原型逐字一致)。

验证:
- 原型 `measure-proto` 实测:hero 为 `v1.2.3 · The Day Compass`;URL 说明行 y=474、h=38(2 行)。
- `:app:assembleDebug` + `:app:lintDebug` + `:app:testDebugUnitTest` 通过;
  生成 `BuildConfig.VERSION_NAME = "1.2.3"`,与原型一致。