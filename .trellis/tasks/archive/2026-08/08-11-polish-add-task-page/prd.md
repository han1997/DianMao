# 新建任务页面 UI 打磨

## Goal

继续打磨 DianMao 的新建任务页面，让 `AddTaskPanel` 更符合“先记录，再整理”的核心工作流：打开后第一眼聚焦任务内容输入，之后再逐步处理子事项、象限、时间、日历、响铃和重复规则；同时保持编辑事项路径可用，不改变保存、提醒、日历和快速连续记录行为。

## What I already know

* 用户要求继续 UI 打磨，特别是新建任务页面。
* 新建任务页面由 `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` 内的 `AddTaskPanel` 实现。
* `AddTaskPanel` 同时承载新增和编辑事项两种模式。
* 现有设计系统要求 Material 3、warm neutral、terracotta 主强调色、calendar blue 仅用于时间/日历语义、低噪声、低阴影、状态变化动效。
* 现有 code-spec 已明确：
  * 新建任务保存后不关闭面板，清空标题/时间/提醒/重复/子事项，保留当前象限并显示保存反馈。
  * 编辑事项保存后关闭面板。
  * 面板返回逻辑必须继续由统一 `BackHandler` 优先级处理。
  * 功能图标应使用 `IconTile` / `InlineButtonIcon` 等低噪声图标容器，不再散落默认裸图标。
* 当前 `AddTaskPanel` 已有基础表单分区，但头部、主输入、保存反馈、保存按钮和信息层级还未完全跟最近两轮 UI 语言对齐。

## Requirements

* 打磨 `AddTaskPanel` 的新增模式，让标题区、主输入和保存动作更突出。
* 保持编辑模式完整可用，文案可以按编辑语境调整，但保存行为不能改变。
* 强化“捕获优先”：任务内容输入应是最显眼、最先完成的区域；象限/时间/提醒属于次级整理。
* 将面板头部关闭按钮、保存反馈、保存按钮内图标等迁移到统一 icon treatment。
* 改善分区视觉层级：事项、子事项、象限、时间、提醒、重复规则应更容易扫读，避免像普通设置页堆叠。
* 保持可访问性和小屏可用：保留 48dp 操作目标、滚动表单、固定底部保存按钮、IME padding。
* 不改变 Task 数据模型、提醒调度、日历写入、备份或导航逻辑。

## Acceptance Criteria

* [ ] 新建任务面板打开后，任务内容输入是明确主焦点。
* [ ] 新增和编辑两种模式的标题、副标题、保存按钮文案清晰。
* [ ] 保存反馈使用统一图标容器和现有 capture-first 文案。
* [ ] 象限、时间、日历、响铃、重复规则保持现有功能，并在视觉上从属于“先记录”的主任务。
* [ ] `:app:testDebugUnitTest`、`:app:lintDebug`、`:app:assembleDebug` 通过。
* [ ] `git diff --check` 通过。

## Definition of Done

* 改动聚焦 `AddTaskPanel` 及其直接复用的小型 UI helper。
* 不新增外部依赖。
* 如发现新的可复用 UI 规则，再更新 `.trellis/spec/backend/compose-patterns.md`。
* 更新 `CHANGELOG.md`。

## Technical Approach

Use a focused refinement of the existing Compose implementation:

* Add small reusable helpers only if they remove duplicated icon/header/control treatment.
* Keep `AddTaskPanel` as the owner of add/edit state.
* Reuse `RowShape`, `PanelShape`, `IconTile`, `InlineButtonIcon`, `quietBorder`, and `quietTextFieldColors`.
* Avoid changing `onSave`, permission requests, date/time pickers, repeat logic, or back handling.

## Decisions

* Scope: polish the shared add/edit sheet, with the new-task capture experience as the primary design target.

## Out of Scope

* New fields, tags, lists, attachments, templates, natural language parsing, or AI task creation.
* Database/model changes.
* Calendar/reminder behavior changes.
* Dark theme redesign.

## Technical Notes

* Relevant source:
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
  * `DESIGN.md`
  * `.trellis/spec/backend/compose-patterns.md`
* Existing dirty paths before this task:
  * `.idea/misc.xml`
  * `.impeccable/`
