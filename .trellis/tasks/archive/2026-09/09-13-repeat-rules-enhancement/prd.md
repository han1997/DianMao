# 重复规则增强

## Goal

让重复任务覆盖更多真实场景：每年（生日/纪念日）、指定周几（如工作日）、自定义间隔（每 N 天/周/月/年），并把重复规则从单一枚举扩展为可表达频率 + 间隔 + 周几的结构，同时保持现有数据、备份与日历同步的向后兼容。

## Requirements

* 重复门禁解耦：只需设置到期日（+时间）即可选重复；日历响铃只控制 RRULE 同步、到点提醒只控制本机按周期重排，均不再作为重复前置条件
* 数据模型：`RepeatRule` 新增 `Yearly`；`TaskEntity`/`TaskFormState`/`TaskUiModel` 新增 `repeatInterval: Int = 1`（每 N 周期）与 `repeatWeekdays: Set<Int>`（ISO 星期 1-7，仅 Weekly 用；空 = 到期日所在周几）
* Room v10→v11 迁移：新列带默认值（repeatInterval DEFAULT 1），旧数据行为不变
* `RepeatReminderCalculator` 支持 interval 步进、按周几取下一次发生（带间隔时按起始周锚定，每 N 周内所选周几各触发一次）、Yearly（2/29 在非闰年由 java.time 钳位到 2/28）
* `CalendarWriter` RRULE 扩展：`INTERVAL=N`、`BYDAY=MO,TU…`、`FREQ=YEARLY`
* `TaskBackupCodec` 升级 v5：导出携带新字段，v3/v4 仍可导入
* 重复选择器 UI（`AddTaskPanel`）：7 预设（不重复/每天/工作日(周一~五)/每周/每月/每年/自定义）+ 自定义（每 N 天|周|月|年 + 任意周几组合，周几 chip）
* 动态重复标签解析器（纯函数，可测）："每天"/"每 2 天"/"每周一、三"/"每 2 周"/"每月"/"每 3 个月"/"每年"/"每 2 年"，TaskRow/TaskDetail 复用
* 工作日 = Weekly + weekdays {1..5} 的预设，不单设周末预设（自定义周几覆盖）

## Acceptance Criteria

* [ ] 旧数据升级后行为不变（interval=1、空周几 = 现有语义）
* [ ] v3/v4 备份文件仍可导入，v5 导出携带新字段
* [ ] 指定周几的重复任务：下一次发生落在所选周几
* [ ] 每年重复的 2/29 任务在非闰年推进到 2/28
* [ ] 自定义间隔（如每 2 周）推进正确，日历 RRULE 与本机提醒一致
* [ ] 未开任何提醒、仅设到期日的任务可选重复并正常推进周期
* [ ] lint / 单测 / assembleDebug 全绿

## Definition of Done

* 单测新增/更新并通过
* lint / typecheck / assembleDebug 绿
* CHANGELOG.md 追加、README 必要处同步
* 版本号 bump（项目硬规则）

## Technical Approach

* 枚举加 `Yearly`（name 字符串存储，`valueOf` 对旧值向后兼容）+ 两个新字段装间隔与周几，避免"枚举带载荷"的复杂度
* Room 迁移链 v10→v11 显式 MIGRATION，schema JSON 导出
* RRULE 生成与解析均集中在 `CalendarWriter`，保持"重复规则 → 系统日历"单一出口
* 标签解析与周期推进为纯函数，优先可测
* UI 选择器沿用品牌化对话框容器（branded dialog container），替换现有 `QuietDropdownMenu`

## Decision (ADR-lite)

**Context**: 重复规则原先耦合在"日历响铃"开关下（repeatReady = 有日期 && 日历 && 响铃），但数据层周期推进不依赖提醒管线；本任务需扩展频率/间隔/周几表达力。
**Decision**: 门禁解耦为只需到期日；数据模型 = 枚举频率 + repeatInterval + repeatWeekdays；UI 用 7 预设 + 自定义。
**Consequences**: 未开提醒的重复任务也能推进周期（依赖 app 启动 reset）；旧数据 interval=1 语义不变；后续多重提醒（任务 2）复用同一门禁逻辑。

## Out of Scope

* 多重提醒（任务 2 处理）
* 重复任务结束条件（至某日/共 N 次）——Count 枚举维持现状
* 农历/自定义日历

## Technical Notes

* 已核实：TaskModels.kt、TaskConverters.kt、RepeatReminderCalculator.kt、CalendarWriter.kt（RRULE at :223-225）、AddTaskPanel.kt（repeatReady gate at :511）
* 待实现时读：TaskBackupCodec.kt、TaskDatabase.kt（迁移链）、schemas/ v10 JSON
* RRULE 参考：RFC 5545（FREQ/INTERVAL/BYDAY）
* java.time `plusYears` 对 2/29 自动钳位
* 项目硬规则：每次代码变更会话必须 bump 应用版本
