# brainstorm: 完善软件功能

## Goal

在现有 DianMao 任务管理、提醒、日历、复盘、小组件和备份能力之上，选择一条明确的产品主线补齐功能闭环，避免无边界地堆叠功能，并以可验证的 MVP 交付。

## What I already know

* DianMao 是 Android 任务规划软件，核心模型是四象限任务、时间安排、提醒和完成心得。
* 当前已实现：四象限、今天/近期/收集箱智能视图、搜索、任务编辑/删除/归档、子事项、循环规则、多个提醒提前量、系统日历同步、AlarmManager 提醒、通知操作、小组件、月历、复盘、批量操作、主题切换、JSON/WebDAV 备份恢复和自动备份。
* 当前代码与测试主要覆盖 Room 数据模型、循环任务、提醒推进、备份规则、复盘统计、日历模型和首页投影；最近版本已处理多提醒循环推进、恢复前快照、恢复确认和静默完成历史等边界。
* 设计契约要求 Compose UI 先有确认后的 `design/mockup/` 原型，并同步 `CHANGELOG.md`、`README.md`（行为变化时）。不使用模拟器；验收以 Gradle 检查、单元测试、原型冒烟测试和 `git diff --check` 为准。
* 当前工作区已有用户/环境产生的未提交变化：`.idea/`、`.impeccable/`、`.kotlin/`、`.opencode/`。本任务不应覆盖或清理这些变化。

## Assumptions (temporary)

* 本轮只选择一个主线，优先交付端到端闭环，而不是同时修改多个独立子系统。
* 如果涉及 Compose 页面，需先更新或新增原型状态，再修改实现；不以“编译通过”替代 UI 验收。

## Decision (ADR-lite)

**Context**: 用户将功能方向选为「提醒与通知」，并明确排除可配置稍后提醒；随后明确表示刚提出的两项都要做：发送测试通知、展示通知/精确闹钟权限状态与设置入口。
**Decision**: 本轮同时交付两项窄范围能力：(1)「小组件与通知」页显示通知权限与精确闹钟权限状态，并分别提供系统设置入口；(2)提供真实的「发送测试通知」按钮验证通知渠道与用户可见投递。测试通知独立于任务数据，不携带完成/稍后操作，不写 Room、不创建闹钟。
**Consequences**: 复用现有提醒 channel、`POST_NOTIFICATIONS` 权限 launcher、`canScheduleExactAlarms()` 和系统设置跳转惯例；Android 13+ 未授权时先请求通知权限，其他被系统关闭的情况给出清楚的系统设置引导。精确闹钟许可只呈现状态和设置入口，不改变目前 AlarmManager 的精确/非精确降级策略。无 Room 或备份格式变更。原型交互获确认后再实现 Compose。

## Requirements

* 保留现有通知提醒、任务快捷操作、重复周期推进和稍后提醒行为；不修改稍后提醒的固定 10 分钟时长。
* 「设置 → 小组件与通知」提供独立的「通知权限」「精确闹钟」状态行，反映真实平台状态；两项状态各自可打开对应的系统应用通知设置或精确闹钟授权页。
* 页面提供「发送测试通知」操作：通过既有提醒渠道发送一条明确标记为测试的非任务通知；不得要求任务 id，不得完成任务、创建提醒或刷新小组件。
* Android 13+ 未授予 `POST_NOTIFICATIONS` 时，测试操作先发起运行时权限请求；拒绝后显示可恢复的说明/设置入口。权限已授予但应用通知/提醒 channel 被系统关闭时，不得谎报测试成功，应引导至应用通知设置。
* 精确闹钟状态在 Android 12+ 使用 AlarmManager 的系统状态；低版本显示为不需要额外授权。入口只在授权缺失时突出显示，可通过系统设置返回后刷新状态。
* 原型须覆盖权限状态、测试成功、权限被拒/通知关闭的反馈文案；原型中的系统状态和发送结果明确为演示，不伪造 OS 行为。原型修改经用户确认后才能开始 Compose UI 实现。
* 使用现有设计令牌与 `QuietDialogContainer`/snackbar 样式，不增加 Room 字段或备份字段；保持独立通知测试 ID/动作与任务提醒 ID 不冲突。

## Acceptance Criteria

* [ ] 设置页显示通知与精确闹钟当前状态，系统设置入口可分别访问对应设置。
* [ ] 测试通知发送后显示可识别的测试文案，不携带任务完成/稍后按钮，也不改变 Room 数据/闹钟。
* [ ] Android 13+ 权限被拒、应用通知关闭、channel 被关闭时不显示虚假发送成功，并提供恢复路径。
* [ ] Android 11 及更低系统不要求精确闹钟特殊权限；Android 12+ 状态按 `canScheduleExactAlarms()` 显示。
* [ ] 通知渠道可用性、权限状态投影/文案以及测试通知独立标识有确定性验证；平台专属逻辑由 lint/build 验证，不在 JVM 测试里构造 Android framework intent。
* [ ] 原型冒烟测试覆盖设置入口、系统设置行、测试发送和反馈状态；原型确认后 Compose 与原型一致。
* [ ] `:app:testDebugUnitTest`、`:app:lintDebug`、`:app:assembleDebug`、`git diff --check` 通过；`CHANGELOG.md` 和 README 能力清单同步更新。

## Definition of Done

* 权限状态读取、设置跳转、测试通知与反馈从设置入口端到端可用。
* 所有权限拒绝/通知关闭分支均提供诚实、可恢复的状态反馈。
* 不更改 Room schema、备份格式或既有任务通知动作；不修改稍后提醒时长。
* 不改动任务范围外的工作区文件。

## Out of Scope

* 不新增稍后提醒时长设置、提醒历史、标签/项目、通知分组管理或多设备同步。
* 不在测试通知中挂任务操作，不修改任务完成、闹钟调度或循环推进。
* 不重做已确认的整体视觉设计，不启动模拟器、安装 APK 或生成设备截图。

## Technical Notes

* 现有设置页面与通知预览：`app/src/main/java/fun/han1997/dianmao/ui/screens/WidgetNotificationsScreen.kt`。
* 权限请求、设置导航与测试通知触发：`app/src/main/java/fun/han1997/dianmao/MainActivity.kt`。
* 系统通知设置现有 helper：`app/src/main/java/fun/han1997/dianmao/ui/logic/PlatformHelpers.kt`。
* 既有提醒 channel：`app/src/main/java/fun/han1997/dianmao/alerts/TaskNotificationChannels.kt`；既有真实任务提醒与快捷操作分别由 `TaskReminderReceiver.kt` / `TaskActionReceiver.kt` 处理，不复用任务操作 PendingIntent。
* 精确闹钟检查：`ReminderScheduler.canScheduleExactAlarms()`；注意通知是否允许、应用通知总开关、channel importance 与精确闹钟授权是不同状态。
* 原型页面：`design/mockup/app.js` 的「小组件与通知」，现有静态预览不能当作真实 OS 状态或通知发送成功。
* UI 规约：`.trellis/spec/frontend/prototype-first.md`；后端权限与取消规约：`.trellis/spec/backend/error-handling.md`、`.trellis/spec/backend/quality-guidelines.md`。


## Verification Record

* `./gradlew.bat :app:testDebugUnitTest --no-daemon` — passed.
* `./gradlew.bat :app:lintDebug --no-daemon` — passed.
* `./gradlew.bat :app:assembleDebug --no-daemon` — passed; only the existing SDK XML version warning was emitted.
* `node --check design/mockup/app.js` and `node --check design/mockup/smoke-test.cjs` — passed.
* `git diff --check` — passed.
* `node design/mockup/smoke-test.cjs` — blocked because the environment has no `playwright` module; no dependency was installed. Device/emulator verification remains intentionally out of scope.
* `./gradlew.bat :app:bumpVersion` — passed; version advanced to `1.2.9` / versionCode `23` and synchronized the prototype version constant.
* `./gradlew.bat :app:assembleRelease --no-daemon` — passed; `app/build/outputs/apk/release/dianmao-1.2.9.apk` exists.
