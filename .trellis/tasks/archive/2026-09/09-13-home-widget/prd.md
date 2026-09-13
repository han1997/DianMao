# 桌面小组件

## Goal

为 DianMao 增加桌面小组件：今日事项速览 + 快速新增入口，强化 "capture before classification" 与 daily capture——不用打开应用即可看到今天要做什么并快速记录。

## What I already know

* 数据可复用：TaskRepository（Room 直接查询）、今日任务逻辑已有纯函数基础（HomeSmartView.Today 分类、buildTimeFocusItems、effectiveDueAtMillis + RepeatReminderCalculator.currentCycleDueAt）
* 深链已成熟：MainActivity 处理 EXTRA_TASK_ID（cold start initialTaskId + onNewIntent currentComposeTaskId 转发）；widget 快速新增可复用同样模式（EXTRA_OPEN_ADD）
* 依赖清单：无 WorkManager/Glance——小组件框架需新增依赖或用 framework RemoteViews
* 全 App 单一写入方 = 应用自身；widget 数据更新需主动触发（provideGlance 每次系统更新时查询）
* minSdk 31；manifest 已有 receivers（reminder/boot restore）
* Compose 代码库（Kotlin 2.0.21 + Compose BOM 2024.09.03）

## Assumptions (temporary)

* Widget 内容：今日未完成事项（≤4 条，按到期时间排序）+ 快速新增按钮 + 点击 widget 打开应用
* 更新时机：应用启动时 + 数据变更后（helper 调用）+ 系统周期（30min 下限）
* 快速新增 = PendingIntent 打开 MainActivity 并自动展开新增面板

## Open Questions

（已全部解决）

## Requirements (evolving)

* 桌面小组件（Glance）：今日未完成事项速览（≤4 条，按到期时间排序）+ 快速新增按钮 + 点击 widget 打开应用
* 数据从 Room 查询（复用 TaskRepository，今日分类复用现有纯函数逻辑）
* 快速新增：PendingIntent 打开 MainActivity 并自动展开新增面板（EXTRA_OPEN_ADD，cold start + onNewIntent）
* 更新时机：应用启动时 + 数据变更后（updateAll helper）+ 系统周期（30min 下限）

## Acceptance Criteria (evolving)

* [ ] Widget 可添加到桌面，展示今日未完成事项
* [ ] 快速新增按钮打开应用并展开新增面板
* [ ] 数据变更后 widget 更新
* [ ] lint / 单测 / assembleDebug 全绿

## Definition of Done (team quality bar)

* 单测新增/更新并通过
* lint / typecheck / assembleDebug 绿
* CHANGELOG.md 追加、README 必要处同步
* 版本号 bump（项目硬规则）

## Out of Scope (explicit)

* Widget 内直接完成任务（点击条目仅打开应用）
* 多尺寸/可配置 widget
* 备份/复盘在 widget 中展示

## Technical Notes

* Glance：androidx.glance:glance-appwidget（Compose 风格，suspend provideGlance 可查 Room；需新依赖）
* RemoteViews：framework API（无依赖；XML 布局 + RemoteViews API，动态列表受限）
* 更新触发：`DianMaoWidget().updateAll(context)`（Glance）或 AppWidgetManager.update（RemoteViews）
* MainActivity 深链：新增 EXTRA_OPEN_ADD 处理（cold start + onNewIntent）
* 项目硬规则：每次代码变更会话必须 bump 应用版本
