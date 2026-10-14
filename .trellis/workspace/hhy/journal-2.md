# Journal - hhy (Part 2)

> Continuation from `journal-1.md` (archived at ~2000 lines)
> Started: 2026-09-13

---



## Session 59: Home widget

**Date**: 2026-09-13
**Task**: Home widget
**Branch**: `master`

### Summary

Glance home-screen widget: today task list (max 4, today smart-view logic), quick-add deep link opening the add sheet, task-row deep links, refresh on activeTasks change + 30-min system period, day/night warm palette; version bumped to 1.0.7

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `690f4cc` | (see git log) |
| `0648ac8` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 60: Feature polish v1.0.8 - batch ops + calendar + widget + notifications

**Date**: 2026-09-15
**Task**: Feature polish v1.0.8 - batch ops + calendar + widget + notifications
**Branch**: `master`

### Summary

Completed feature polish v1.0.8: phase 1 notification quick-complete/snooze actions, phase 2 interactive widget (toggle completion + responsive 8-row layout), phase 3 calendar month view (per-day task list, 记一笔 with preset due date), phase 4 batch operations (long-press multi-select with complete/move-quadrant/delete + undo). Version bumped 1.0.7->1.0.8, CHANGELOG/README updated. All checks green (compile, unit tests, assembleDebug, lintDebug). Spec updated with TaskSelection back-target priority.

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `9109b4f` | (see git log) |
| `5dfad44` | (see git log) |
| `fac2099` | (see git log) |
| `578c705` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 61: Stability polish

**Date**: 2026-09-16
**Task**: Stability polish
**Branch**: `master`

### Summary

Hardened cross-entry silent task completion with latest-entity snapshot validation, made batch selection and calendar cleanup resilient to stale tasks, added reminder/calendar regression coverage, updated quality guidance and changelog, and verified unit tests, lint, and debug assembly.

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `201dd08` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 62: ui-polish: DESIGN.md 对齐审计与修复

**Date**: 2026-09-16
**Task**: ui-polish: DESIGN.md 对齐审计与修复
**Branch**: `master`

### Summary

全量审计 19 个 UI 文件对齐 DESIGN.md:修复 Home 面板 tint 8%→4.5%、时间关注行 0.07/0.12、TaskRow 边框 0.18、OutlinedButton padding 11dp、删死亡 import;新增 boardPane alpha token 并同步 spec;更新 DESIGN.md(暗色已实现、优先级 SegmentedButton、表单对话框统一 QuietDialogContainer)。lint+compile 通过。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `132118e` | (see git log) |
| `52fff89` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 63: Launcher icon redesign: Day Compass

**Date**: 2026-09-23
**Task**: Launcher icon redesign: Day Compass
**Branch**: `master`

### Summary

Redesigned the launcher icon from the paw-print quadrant mark to the Day Compass: a thin terracotta dial ring (#F5DED9) with a slender needle (#A84534) pointing 45° up-right and an ink hub (#1E211F) at center, on a pure warm paper background (#F8F8F5). Monochrome layer mirrors the same ring + needle + hub silhouette for themed icons. All artwork sits inside the 66dp adaptive safe circle. Updated DESIGN.md and CHANGELOG.md. Build verified with assembleDebug.

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `4a64d44` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 64: UI/代码深度审查：修复周期判定与备份面板 bug，统一错误守卫与死导入清理

**Date**: 2026-10-11
**Task**: UI/代码深度审查：修复周期判定与备份面板 bug，统一错误守卫与死导入清理
**Branch**: `master`

### Summary

Deep code review across UI + data layers. Fixed the ISO-weekly cycle bug (calendar year vs weekBasedYear split the New Year's Eve week into two periods), merged the duplicated isInSamePeriod/isInSameCycle implementations, and reworked resetExpiredCycleTasks to a DAO-level filter + single transactional bulk update. Fixed BackupPanel where the status header Surface accidentally wrapped all four FormSettingBlocks (card-in-card double tint, no spacing). Extracted runOrNotifyFailure to replace 13 identical try/catch+snackbar blocks and the completion-record callbacks shared by task detail and history panel; removed 34 dead imports across 13 files; repaired mis-indented FAB/Crossfade/TimeFocusLane/TaskDetailScreen blocks. Threaded a midnight-refreshed nowMillis (produceState) through review stats, smart views, time-focus lane and list grouping so day buckets no longer go stale across midnight. compileDebugKotlin + all 15 unit test classes pass.

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `8fe93a1` | (see git log) |
| `3d7e0af` | (see git log) |
| `10e94c2` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 65: 像素级 UI 对齐收尾:补齐并排证据并修复备份页滚动列回归

**Date**: 2026-10-13
**Task**: 像素级 UI 对齐收尾:补齐并排证据并修复备份页滚动列回归
**Branch**: `master`

### Summary

完成 10-12 像素级对齐的验收阶段:补齐 settings/appearance/widget/backup/archive/review/quadrant 共 14 张明暗并排图;用 443dpi 几何 diff 复核新增屏 x 偏差 ≤3.8dp、行内间距与原型一致(纵向 ~-20dp 为既有状态栏基线),并将 dialogs 展示页与 WebDAV supportingText 登记为允许偏差。检查阶段发现并修复 3d7e0af 引入的备份页回归:滚动列被提前闭合,weight(1f) 列塌缩为 0 高,云同步状态头卡不可见且下载恢复/自动备份无法滚动到达(字体 1.3 破版);已把 FormSettingBlock 放回滚动列、chip 归位文本列,并在 compose-patterns 规范记录该花括号作用域陷阱。assembleDebug/lintDebug/testDebugUnitTest 全绿,版本升至 1.2.3。归档 10-11、10-12 两个任务。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `d7062c1` | (see git log) |
| `f0b3b91` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 66: 关闭像素级对齐遗留项:版本号单一来源 + 备份 URL 辅助行补齐

**Date**: 2026-10-13
**Task**: 关闭像素级对齐遗留项:版本号单一来源 + 备份 URL 辅助行补齐
**Branch**: `master`

### Summary

按原型优先契约先改原型再改实现,关闭 10-12 遗留的两条偏差。版本号:设置页 hero 与「关于」弹层的写死 v1.1.2 改为原型侧 __APP_VERSION__ 占位符 + APP_VERSION 常量(render()/sheet() 经 withVersion 替换),实现侧改读 BuildConfig.VERSION_NAME(开启 buildConfig);bumpVersion Gradle 任务同步改写 app.js 常量,杜绝漂移。备份 URL 辅助行:按严格对齐在原型 WebDAV 配置补上支持说明行(新增 .field-group,复用 .hint 12/16 @78%,对齐 QuietField.supportingText 的 4/6 间距),消除此前登记的允许偏差。更新 BASELINE.md v1.6 变更记录与新 SHA-256,并在 design-prototypes 规范固化版本号单一来源约定。验证:原型 smoke test 通过、assembleDebug/lintDebug/testDebugUnitTest 全绿、BuildConfig.VERSION_NAME 与原型 APP_VERSION 均为 1.2.3。归档任务 10-13。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `7b1de29` | (see git log) |
| `576328c` | (see git log) |
| `2b49bae` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 67: 小缺口修补:About 弹层 + 首启空态 + 通知设置入口

**Date**: 2026-10-13
**Task**: 小缺口修补:About 弹层 + 首启空态 + 通知设置入口
**Branch**: `master`

### Summary

接通设置页「关于 DianMao」弹层(AboutDialog);彻底移除 seedIfEmpty/seedTaskEntities/countTasks,首启改走真实空状态引导(四象限板按 activeTasks.isEmpty()、智能视图按无进行中任务,共享 AddTaskTonalButton);「小组件与通知」页新增系统分组「通知设置」行,经 openAppNotificationSettings 打开系统应用通知设置。原型 v1.7 已确认,新增三组 smoke 断言;screenshots 与抓图脚本转本地不追踪。bumpVersion -> 1.2.4 (versionCode 18),release APK 已构建并验签。验证:assembleDebug/lintDebug/testDebugUnitTest/git diff --check 全通过。按用户指令不再使用模拟器,设备侧视觉并排比对未执行。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `8fed4c1` | (see git log) |
| `dd1b2f6` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 68: 时间关注可移除 + 近期窗口语义统一

**Date**: 2026-10-13
**Task**: 时间关注可移除 + 近期窗口语义统一
**Branch**: `master`

### Summary

时间关注 lane 增加会话级关闭按钮(rememberSaveable,不持久化,冷启动必现);buildTimeFocusItems 增加 7 天上限(FOCUS_LANE_UPCOMING_DAYS)且超限直接剔除、默认 limit 3→4(FOCUS_LANE_LIMIT);buildHomeSmartTaskItems 的近期窗口 7→14 天(UPCOMING_SMART_VIEW_DAYS),消除「lane 有近期、近期页没有」的语义漂移。HeaderActionButton 增加 tileSize 参数以复刻原型 .lane-dismiss(34dp 热区/30dp 块)。原型 v1.8:同类会话级关闭,timeFocus() 改按时刻判定过期/今天。spec 补充 lane 契约与「兄弟视图窗口漂移」常见错误;测试新增 lane 上限、4 行默认、14 天窗口边界与「lane 近期 ⊆ 近期视图」子集断言(TimeFocusItemsTest 13 项)。验证:assembleDebug/lintDebug/testDebugUnitTest(127 项)/git diff --check 与原型冒烟测试全通过;bumpVersion -> 1.2.5 (versionCode 19)。按用户指令未使用模拟器,设备侧比对登记为残余风险。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `0d856f5` | (see git log) |
| `8f5b679` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 69: 复盘窗口常量收敛 + 边界测试

**Date**: 2026-10-13
**Task**: 复盘窗口常量收敛 + 边界测试
**Branch**: `master`

### Summary

承 10-13-dismissible-time-focus-lane 修复的同类问题排查:buildReviewStats 的「近 7 天」计数(minusDays(6))、30 天计数(minusDays(29))、每日分布图((6 downTo 0))三处裸字面量隐式耦合,当前一致但无测试锁定。提为 REVIEW_WINDOW_DAYS / REVIEW_MONTH_DAYS 常量;ReviewModelsTest 新增边界断言(第 6 天计入/第 7 天排除、第 29 天计入/第 30 天排除)与「图表总数 == 头部计数」不变量。变异验证:常量改 6 或 8 均失败 4 项测试,仅改图表跨度失败 3 项 —— 证明确实锁住了不变量而非复述实现。UI 文案(ReviewScreen 的「近 7 天」/「近 30 天」)按用户确认范围保持字面量,已在 spec 登记为已接受的重复。行为不变。spec 的「Sibling Views Silently Disagreeing on a Window」补记本实例。验证:testDebugUnitTest(130)/lintDebug/assembleDebug/git diff --check + 原型冒烟测试全通过;bumpVersion -> 1.2.6 (versionCode 20)。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `2e1436e` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete


## Session 70: 查缺补漏:D1 多提前量重复推进 / D2 本地恢复快照 / D3 恢复确认 / D4 备份测试

**Date**: 2026-10-15
**Task**: 查缺补漏:D1 多提前量重复推进 / D2 本地恢复快照 / D3 恢复确认 / D4 备份测试
**Branch**: `master`

### Summary

全仓盘点后修复四项缺口:v1.2.7。D1 提醒广播按到期时刻幂等推进(A)避免多提前量重复任务一次到期推进多周期;D2 本地恢复补写公共下载目录的恢复前快照并失败即中止;D3 本地/WebDAV 恢复加「恢复备份?」确认弹层(原型新增第 9 对话框,BASELINE v1.8);D4 抽出纯 BackupCodecRules + 容错枚举解码并补 17 项单测。新增 RepeatReminderAdvanceGuardTest/BackupCodecRulesTest。验证 assembleDebug/lintDebug/testDebugUnitTest/diff --check/原型冒烟全通过。

### Main Changes

(Add details)

### Git Commits

| Hash | Message |
|------|---------|
| `25daae0` | (see git log) |

### Testing

- [OK] (Add test results)

### Status

[OK] **Completed**

### Next Steps

- None - task complete
