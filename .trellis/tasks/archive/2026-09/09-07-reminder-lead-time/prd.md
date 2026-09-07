# Reminder lead time and local notifications

## Goal

After removing the local notification pipeline, tasks without calendar sync
have no due-time alert at all. Restore a lightweight local reminder AND add a
remind-lead-time option (准点/提前5分/30分/1天), matching mainstream apps,
while keeping「日历响铃」as the calendar-side switch.

## What I already know

- The previous pipeline (alerts/*, AlarmManager, boot restore, channels,
  POST_NOTIFICATIONS) was fully removed in commit 89e386c; git history holds a
  proven implementation to reintroduce in slimmer form.
- Form now has: 到期日 / 提醒时间 / 同步到系统日历 / 开启日历响铃 / 重复.
- DESIGN.md: calendar blue only for scheduling semantics; alarms are
  scheduling semantics → blue voice.
- OPPO force_reminder linkage exists for calendar ring.

## Requirements

- 新增「到点提醒」开关(本地通知),独立于「同步到系统日历」与「日历响铃」。
- 提醒提前量选项:准点 / 提前5分钟 / 提前30分钟 / 提前1天(存于任务)。
- 需要新增持久化字段(如 reminderLeadMinutes / useLocalReminder)→ 迁移 + 备份
  codec + schema 导出同步更新。
- 通知点击进入对应任务详情;boot 后恢复未来提醒;重复任务按周期推进。
- UI:开关与提前量作为「时间与提醒」区行,依赖到期日(和提醒时间,若提前量>准点)。

## Acceptance Criteria

- [ ] 开启到点提醒且不开日历同步,到点(或提前量)收到通知。
- [ ] 提前量可选且持久化;编辑/重启/重复推进后调度正确。
- [ ] Room 迁移 + 备份导出/导入包含新字段;lint/test/assemble 绿。

## Out of Scope

- 多重提醒、按位置提醒、通知样式定制。

## Technical Notes

- 恢复 alerts 包(ReminderScheduler/Receiver/BootRestore)但精简;
- 提前量=触发时间=dueAt-lead;重复任务 nextDue 同样应用提前量。
- DB 版本 +1,迁移加可空列(默认准点)。
