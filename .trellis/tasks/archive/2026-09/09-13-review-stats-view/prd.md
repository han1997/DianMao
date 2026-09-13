# 复盘统计视图

## Goal

补全产品愿景中 "Completion creates memory" 的最后一块：一个专门的复盘页面，聚合完成数据（近 7/30 天完成次数、每日分布、四象限占比）与完成心得，让已完成的工作保持可回顾、可复盘。

## Requirements

* 入口：点按主页"已完成 N"统计 pill 打开复盘页（不加头部按钮）
* 新全屏页面 `ReviewScreen`（复用 FullScreenScaffold 体系：顶栏、安全区、600dp 宽度帽）
* Room JOIN 查询：`task_completions` + `tasks`（标题、象限）按时间倒序（新 DAO 查询 + `CompletionWithTask`）
* 完成事件流（纯函数，可测）：`task_completions` 行（重复任务每周期完成）∪ 非重复已完成任务（1 次事件，时间 = lastCompletedAtMillis，心得 = completionNote），按时间倒序
* 统计（纯函数，可测）：近 7 天 / 近 30 天完成次数、近 7 天每日分布（quiet 行内条形）、近 30 天四象限占比（InlineStatDot）
* 最近完成列表：时间倒序，展示象限点 + 标题 + 时间 + 心得（如有），约 20 条上限
* 空状态："暂无完成记录"
* 返回行为：ReviewPanel 作为新 back-consuming 层插入 InAppBackTarget 优先级链（enum + resolver + dispatch + InAppBackTargetTest 同步更新，位于 ArchivedPanel 之后）

## Acceptance Criteria

* [ ] 点按"已完成" pill 打开复盘页；返回先关复盘页再回主页
* [ ] 完成事件与 task_completions ∪ 非重复完成一致，按时间倒序
* [ ] 近 7/30 天计数、每日分布、四象限占比与事件数据一致
* [ ] 空状态清晰
* [ ] InAppBackTargetTest 更新并通过
* [ ] lint / 单测 / assembleDebug 全绿

## Definition of Done

* 单测新增/更新并通过
* lint / typecheck / assembleDebug 绿
* CHANGELOG.md 追加、README 必要处同步
* 版本号 bump（项目硬规则）

## Technical Approach

* DAO：`observeAllCompletionsWithTask(): Flow<List<CompletionWithTask>>`（@Embedded completion + taskTitle/taskQuadrant 别名列，象限经现有 TypeConverter）
* Repository 透传 Flow
* 纯函数（ui/logic）：`buildReviewCompletionEvents` / `buildReviewStats`（每日桶按 LocalDate，四象限计数）
* UI：`ReviewScreen`（ui/screens）+ MainActivity overlay 状态 `showReviewPanel`（Crossfade/motionTween 体系与其它全屏 sibling 一致）
* SummaryPill 加 onClick 参数（仅"已完成" pill 传入入口回调，其余为 null）
* InAppBackTarget 加 ReviewPanel 优先级位

## Decision (ADR-lite)

**Context**: 完成数据分散在 task_completions（重复任务每周期）与 TaskEntity（非重复 lastCompletedAtMillis/completionNote）；统计口径决定复盘页的覆盖面。
**Decision**: 完成事件 = task_completions ∪ 非重复已完成任务；重复任务的静默滑动完成（无 history 行，现有行为）暂不入复盘——维持现状不在本任务重构 history 记录。
**Consequences**: 复盘覆盖"写了心得的重复完成 + 全部非重复完成"；重复静默完成的缺口留待后续 history 化任务；统计为纯函数可测。

## Out of Scope

* 桌面小组件（任务 5）
* 非重复任务完成 history 化 / 重复静默完成入 history
* 备份/导出复盘数据
* 月度趋势图

## Technical Notes

* 已核实：TaskDao.kt（observeCompletions/findAll）、HomeScreen.kt AppHeader（:112-165，SummaryPill 行 :166-172）、MainActivity 全屏 overlay 体系（Crossfade + InAppBackTarget）
* Room JOIN：`SELECT tc.*, t.title AS taskTitle, t.quadrant AS taskQuadrant ... INNER JOIN tasks t ON tc.taskId = t.id` + @Embedded
* compose-patterns spec：新增 back-capable 层需 enum + resolver when + dispatch when + InAppBackTargetTest 同步更新
* 项目硬规则：每次代码变更会话必须 bump 应用版本
