# 稳定性优化：通知、小组件、日历与批量操作

## Goal

在 v1.0.8 四项功能的基础上，消除跨入口完成任务时的行为差异，收口批量操作的失败与撤销边界，并补齐日历与提醒的关键回归测试，降低重复任务、跨时区和快速连续操作导致的数据不一致风险。本任务只做稳定性修复和测试，不新增业务功能。

## What I Already Know

* 通知和小组件都复用 `completeTaskSilently`，但主页普通完成、批量完成、通知取消通知等副作用仍分散在调用方。
* `completeTaskSilently` 当前先更新任务，再按原始任务状态取消提醒并查询后续周期；需验证完成过程中异常时是否留下旧提醒。
* 批量完成逐条执行，批量删除逐条取消提醒、删除实体，并在 Snackbar 撤销窗口内逐条恢复；目前没有统一的部分成功处理或事务边界。
* 日历使用 `YearMonth`、`ZoneId` 和 `effectiveDueAtMillis` 进行纯内存分组，已有月网格、循环任务和完成任务日期测试，但没有覆盖跨午夜、DST/时区边界和同日稳定排序的完整组合。
* 已有提醒 request code 与稍后提醒时间计算测试；尚未覆盖重复触发、过期任务和通知 action 的幂等行为。
* 小组件完成 callback 只按 id 查询后完成并刷新，缺少对任务已不存在、已完成、重复任务推进和刷新失败的可观察测试。
* 现有工作区包含用户或工具产生的未提交文件：`.idea/*`、`.impeccable/`、`.kotlin/`、`.opencode/`，本任务不处理这些文件。

## Requirements

* 统一并明确“静默完成”的副作用顺序：任务状态更新、当前提醒取消、重复任务下一周期提醒重排、小组件刷新由调用方可安全复用；重复执行同一入口不得产生重复或错误提醒。
* 修复通知 action、小组件 callback、主页滑动和批量完成之间的行为差异，尤其是任务已被其他入口完成或删除时的幂等处理。
* 强化批量完成、批量移动和批量删除的空选择、任务消失、单条失败和撤销恢复行为；不得因为一条异常导致 UI 永久停留在多选态或静默吞掉已完成部分。
* 保持批量删除现有撤销语义：撤销窗口内恢复原 id 和实体字段，窗口结束后清理托管日历事件；恢复时提醒状态与实体状态一致。
* 校验日历月视图在系统时区下的日期归属，覆盖任务到期时间靠近午夜、重复任务推进和完成任务保留原日期的边界。
* 补充纯函数单元测试，优先覆盖提醒 action 幂等/时间计算、日历日期分组、批量选中集合和完成副作用决策；不引入新依赖。
* 更新 `CHANGELOG.md`，如稳定性行为对用户可见则同步更新 `README.md`；版本号是否变更沿用现有发布约定，避免在未形成可发布版本时擅自 bump。

## Acceptance Criteria

* [ ] 同一任务从通知、小组件、主页滑动和批量完成，最终状态、提醒和重复周期结果一致。
* [ ] 对已完成、已归档或已删除任务重复触发通知/小组件 action 不会抛异常、重复推进周期或重新创建错误提醒。
* [ ] 批量操作在空选择和部分任务已经消失时安全结束，并正确退出或保持多选态；用户能获得明确结果提示。
* [ ] 批量删除撤销能够恢复全部成功删除的实体及其提醒，未成功删除的任务不会被误恢复或误清理日历事件。
* [ ] 日历分组测试覆盖系统时区、午夜边界、循环任务当前周期和已完成任务原始日期。
* [ ] `:app:testDebugUnitTest`、`:app:assembleDebug` 和 `:app:lintDebug` 通过。

## Definition Of Done

* 实现代码遵循 `.trellis/spec/backend/` 规范，优先复用现有 repository、scheduler 和 UI 状态模型。
* 新增或更新关键单元测试，并通过完整质量检查。
* `CHANGELOG.md` 和必要的 `README.md` 内容已更新。
* 对本任务产生的新约定完成 spec review；不把无关工作区变更纳入提交。

## Technical Approach

1. 先追踪完成、提醒、日历和批量删除的完整数据流，确定最小共享边界。
2. 把可验证的决策提取为纯函数或 repository 层操作，避免在 BroadcastReceiver、Glance callback 和 Compose lambda 中复制业务规则。
3. 对跨入口操作采用幂等保护和基于数据库最新实体的判断；对批量操作保留现有 UI 语义，优先修复异常路径而不是重写页面状态管理。
4. 先补回归测试，再运行单测、编译和 lint；如发现需求需要扩大到数据库事务或日历写入，则回到 PRD 重新确认范围。

## Decision (ADR-lite)

**Context**: v1.0.8 增加了多个任务变更入口，但当前实现的副作用由不同调用方分别负责，测试主要覆盖纯模型而不是跨入口一致性。

**Decision**: 本轮采用“最小共享逻辑 + 最新实体幂等判断 + 纯函数回归测试”的方案，不引入 ViewModel、新依赖或新的数据模型。

**Consequences**: 可降低重复完成和批量异常风险，改动集中在现有 repository/alerts/UI 边界；真正的端到端通知和 Glance 设备测试仍受 Android runtime 限制，需要通过可测试决策函数和构建验证兜底。

## Out Of Scope

* 新增任务、提醒、日历或批量操作能力。
* 重做主页、日历或小组件视觉设计。
* 引入新的状态管理框架、数据库迁移或第三方依赖。
* 修改用户未授权的 `.idea/`、`.impeccable/`、`.kotlin/`、`.opencode/` 等既有工作区变更。

## Technical Notes

* 相关实现：`app/src/main/java/fun/han1997/dianmao/alerts/TaskCompletionActions.kt`、`TaskActionReceiver.kt`、`ReminderScheduler.kt`、`widget/DianMaoWidget.kt`、`MainActivity.kt`、`TaskRepository.kt`、`CalendarMonthModels.kt`。
* 相关测试：`ReminderActionRequestCodeTest.kt`、`CalendarMonthModelsTest.kt`、`InAppBackTargetTest.kt`、`GroupHomeSmartItemsTest.kt`。
* 项目约定要求每次修改追加 `CHANGELOG.md`，提交推送由 owner 手动执行。
