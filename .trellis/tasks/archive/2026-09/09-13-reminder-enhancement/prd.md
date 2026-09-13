# 提醒增强

## Goal

让到点提醒支持多重提前量（如同时提前 1 天 + 提前 5 分钟），一个任务可在多个时间点收到提醒；并评估通知贪睡动作。补全提醒能力的最后一层，为后续自动备份/复盘/小组件让路。

## What I already know

* 现状：`useLocalReminder: Boolean` + `reminderLeadMinutes: Int`（单一提前量，四档：准时/5分/30分/1天）
* 通知点击直达任务详情**已实现**：TaskReminderReceiver 的 contentIntent 携带 EXTRA_TASK_ID，MainActivity 处理 cold start（initialTaskId）+ onNewIntent（currentComposeTaskId 转发）
* `ReminderScheduler.schedule(task)`：按 `dueAt - leadMinutes` 设定单个精确闹钟，requestCode = taskId.hashCode()；cancel 按 taskId
* `TaskReminderReceiver` 收到广播后发通知 + `advanceRepeatAfterReminder` 推进重复任务并重排
* `ReminderRestoreReceiver` 重启后 `restoreScheduledReminders` 批量重排（恢复时 dueAt 已推进到下一周期）
* Room v12、备份格式 v6（任务 1 刚完成）；迁移链显式、schema 导出、无 destructive fallback
* TaskFormState.normalizedTimeSettings() 归一化提醒字段（useLocalReminder && hasDate）
* 备份面板/任务详情展示 `leadLabel(reminderLeadMinutes)`

## Assumptions (temporary)

* 数据模型：`reminderLeadMinutes: Int` → `reminderLeadMinutes: Set<Int>`（空 set = 未开启）；Room v12→v13 迁移，旧单值 → 单元素 set
* 备份格式 v6→v7，向后兼容导入
* ReminderScheduler 每个提前量一个闹钟（requestCode = taskId + leadTime 组合），cancel 全部

## Open Questions

（已全部解决）

## Requirements (evolving)

* 一个任务可同时启用多个提醒提前量（Set<Int>），每个提前量独立闹钟
* 提前量可选集合：现有四档（准时/提前 5 分钟/提前 30 分钟/提前 1 天）多选，不加新档位
* Room v12→v13 迁移：reminderLeadMinutes Int → Set<Int>（JSON/逗号串），旧数据单值转单元素 set
* TaskBackupCodec v7：携带 set，v3-v6 仍可导入
* ReminderScheduler：多闹钟调度/取消；TaskReminderReceiver 携带触发提前量
* 到点提醒提前量 UI：单选菜单升级为多选
* 任务详情/表单标签展示多个提前量

## Acceptance Criteria (evolving)

* [ ] 旧数据升级后行为不变（单提前量 → 单元素 set）
* [ ] v3-v6 备份仍可导入，v7 导出携带新字段
* [ ] 多提前量任务到点时各触发一次提醒
* [ ] 重复任务在提醒触发后推进到下一周期并重排所有提前量
* [ ] 取消任务提醒时全部闹钟被取消
* [ ] lint / 单测 / assembleDebug 全绿

## Definition of Done (team quality bar)

* 单测新增/更新并通过
* lint / typecheck / assembleDebug 绿
* CHANGELOG.md 追加、README 必要处同步
* 版本号 bump（项目硬规则）

## Out of Scope (explicit)

* 通知贪睡动作（评估后明确不做，留待以后）
* 通知点击直达任务详情（已实现，无需改动）
* 桌面小组件（任务 5）
* 自动备份（任务 3）

## Technical Approach

* 数据模型：`reminderLeadMinutes: Int` → `reminderLeadMinutes: Set<Int>`；空 set = 未开启，与 useLocalReminder 联动归一化
* Room v12→v13 迁移：`ALTER TABLE tasks ADD COLUMN reminderLeadMinutesNew TEXT`（逗号串）→ 数据搬移（单值 → "value"）→ DROP 旧列 → RENAME。或简化为：新列 + 默认搬移
* ReminderScheduler：每个提前量独立闹钟（requestCode = taskId 混合 leadTime 的稳定 hash），cancel 按 taskId 取消全部
* TaskReminderReceiver：extras 携带触发提前量（EXTRA_LEAD_MINUTES），通知文案区分准时/提前
* advanceRepeatAfterReminder / restoreScheduledReminders：重排时覆盖任务全部提前量
* UI：提醒提前量行升级为多选（复用品牌化对话框或 FlowRow chip 多选）

## Decision (ADR-lite)

**Context**: 到点提醒目前只支持单一提前量；通知直达已实现；贪睡评估为非必需。
**Decision**: 范围收窄为多重提前量（现有四档多选）；数据模型 Set<Int>；每提前量一个闹钟；不含贪睡。
**Consequences**: 旧数据单值迁移为单元素 set 行为不变；闹钟数量 = 任务数 × 提前量数（个人使用量级可控）；后续贪睡可复用调度器。

## Technical Notes

* 已核实：ReminderScheduler.kt、TaskReminderReceiver.kt、ReminderRestoreReceiver.kt、MainActivity.kt（deep link at :137/:142-156）
* 待实现时读：TaskNotificationChannels.kt、AddTaskPanel 提前量区块、TaskRepository.reminder 相关方法
* AlarmManager 每任务多闹钟：requestCode 需区分（taskId 与 leadTime 组合 hash）
* 迁移方案：ALTER TABLE tasks ADD COLUMN reminderLeadMinutesSet TEXT NOT NULL DEFAULT '' → UPDATE 搬移 → DROP 旧列 → RENAME（Room 需要最终 schema 与实体一致）
* 项目硬规则：每次代码变更会话必须 bump 应用版本
