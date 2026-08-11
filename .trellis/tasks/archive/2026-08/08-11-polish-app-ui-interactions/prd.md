# 打磨软件 UI 和操作逻辑

## Goal

继续打磨 DianMao 的整体软件 UI 和操作逻辑，让首页、智能视图、象限详情、任务详情、归档/备份等主要路径更符合“快速捕获、低噪声整理、时间语义清晰”的产品方向；在不改变数据模型、提醒、日历、备份等核心行为的前提下，提升日常使用的扫读效率、操作反馈和路径一致性。

## What I already know

* 用户要求“打磨软件 UI、操作逻辑”，范围比上一轮新建任务页面更广。
* 项目是单模块 Android Compose 应用，主界面主要在 `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`。
* 现有主要 UI 入口包括 `DianMaoApp`、`AppHeader`、`HomeSmartControls`、`TimeFocusLane`、`HomeSmartTaskList`、`QuadrantBoard`、`TaskRow`、`TaskDetailScreen`、`BackupPanel`、`ArchivedPanel` 和 `AddTaskPanel`。
* 设计系统已明确 DianMao 的方向是 warm neutral、terracotta 主强调、calendar blue 仅用于时间/日历语义、Material 3、低阴影、低噪声、快速捕获优先。
* README 当前描述产品重点：四象限任务、日历/响铃提醒、完成备注、备份恢复、移动端安全区适配。
* 最近几轮已经打磨过新增任务面板、任务详情、归档、备份、图标系统、首页智能视图和时间关注 lane。
* 现有未提交的任务外脏文件包括 `.idea/misc.xml`、`.trellis/.template-hashes.json`、`.impeccable/`、`.opencode/`，本任务不应误提交这些文件。

## Assumptions (temporary)

* 本轮优先做软件体验打磨，不做新功能扩展。
* 可以调整文案、布局、视觉层级、操作反馈和入口显隐规则。
* 不修改 Room 数据模型、提醒调度、日历写入、备份格式或 WebDAV 行为。

## Open Questions

* None.

## Requirements (evolving)

* 保持 DianMao 既有设计语言：warm neutral、terracotta 主强调、calendar blue 只用于时间/日历语义、低噪声 Material 3。
* 提升主要日常路径的扫读效率和操作一致性。
* 本轮 MVP 聚焦主日常路径：首页、智能视图、象限详情、任务行、任务详情的扫读和点击路径。
* 首页/列表浏览优先：重点覆盖首页头部、智能视图切换、时间关注 lane、象限卡片和任务行。
* 本轮主要解决视觉扫读层级：让首页头部、Today/Upcoming/Inbox、时间关注、象限卡片、任务行的主次更清楚，减少“都是卡片”的堆叠感。
* 操作逻辑只做轻量澄清：通过入口文案、状态提示、按钮层级和触控范围改善下一步动作识别，不改变既有点击/保存/返回行为。
* 保持新增/编辑、完成、删除、提醒、日历、重复、归档、备份等既有行为不变，除非后续明确纳入范围。
* 保持小屏和系统字体缩放可用，关键操作目标保持约 48dp。

## Requirements

* Polish the home/list browsing path, with the homepage as the primary surface.
* Refine `AppHeader`, `HomeSmartControls`, `TimeFocusLane`, `QuadrantBoard`, `QuadrantPane`, `CompactQuadrantTask`, and `TaskRow` only as needed for the home/list visual hierarchy.
* Make the surface rhythm clearer: app header should feel like the page anchor, smart view controls like a navigation/filter strip, time focus like a temporal attention lane, quadrant board like the four-quadrant map, and task rows like scannable list items.
* Reduce same-looking card stacking without abandoning the existing warm neutral Material 3 language.
* Keep operation logic lightweight and behavior-preserving: improve copy, state hints, action hierarchy, and touch affordances only where they clarify the next action.
* Preserve existing task creation, editing, completion, deletion, archiving, detail opening, reminder, calendar, repeat, backup, and back navigation behavior.
* Preserve the color semantics from `DESIGN.md`: terracotta for primary/current emphasis, calendar blue only for time/calendar semantics, quadrant colors as low-alpha semantic overlays.
* Keep mobile usability intact: wrap-safe metadata, scrollable content where already present, no clipped text on narrow screens, and key actions around 48dp.

## Acceptance Criteria (evolving)

* [ ] 被纳入范围的页面在视觉层级上更清晰，主操作更容易识别。
* [ ] 被纳入范围的操作路径文案、反馈和返回/关闭行为一致。
* [ ] 首页头部、智能视图切换、时间关注 lane、象限卡片、任务行不再呈现同质化卡片堆叠感。
* [ ] Calendar blue 仍只用于时间/日历语义，terracotta 仍只用于主强调和当前选择。
* [ ] 不改变任务数据模型、提醒、日历、重复规则、备份或导航底层契约。
* [ ] 更新 `CHANGELOG.md`。
* [ ] `:app:testDebugUnitTest`、`:app:lintDebug`、`:app:assembleDebug` 通过。
* [ ] `git diff --check` 通过。

## Acceptance Criteria

* [ ] Homepage/list browsing has a clearer hierarchy across header, smart controls, time focus lane, quadrant board, and task rows.
* [ ] Smart controls and search read as navigation/filter controls rather than another heavy content card.
* [ ] Time focus lane visually communicates time sensitivity while keeping calendar blue limited to time/calendar semantics.
* [ ] Quadrant board remains a low-alpha four-quadrant map, with clearer count/empty/overflow scanning.
* [ ] Task rows remain compact, accessible, and scannable, with metadata wrapping safely and actions preserving 48dp targets.
* [ ] Existing behaviors for task view, complete, note, edit, delete, archive, add, reminders, calendar, repeat, backup, and back handling are unchanged.
* [ ] `CHANGELOG.md` is updated.
* [ ] `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`, and `git diff --check` pass.

## Definition of Done

* 改动聚焦已确认的 UI/交互范围。
* 不新增外部依赖。
* 不误提交任务外既有脏文件。
* 如形成新的可复用 UI 或操作逻辑规则，更新 `.trellis/spec/backend/compose-patterns.md`。

## Technical Approach

Use focused Compose refinements in `MainActivity.kt`:

* Prefer small adjustments to existing composables over new abstractions.
* Reuse existing helpers such as `RowShape`, `PanelShape`, `ChipShape`, `IconTile`, `CompactMeta`, `quietBorder`, and `quietTextFieldColors`.
* Avoid changing callback signatures or persistence/platform integration paths unless required by existing call sites.
* Update `CHANGELOG.md` for the product-visible polish.

## Out of Scope (temporary)

* 新字段、标签、清单、附件、模板、自然语言解析或 AI 创建任务。
* 数据库 schema、备份格式、WebDAV 协议、Calendar Provider 写入语义、AlarmManager 调度语义。
* 深色主题重设计。
* 大规模拆分 `MainActivity.kt`。
* 归档、备份等管理路径的深入重设计，除非主路径改动需要轻微同步。

## Decisions

* Scope: 本轮 MVP 选择主日常路径优先，暂不把归档/备份等管理路径作为主要打磨对象。
* Focus: 主日常路径内选择首页/列表浏览优先，任务详情/完成路径只在需要保持入口一致时做轻量同步。
* Direction: 首页/列表浏览优先解决视觉扫读层级，操作逻辑只做不改行为的入口与反馈澄清。
* Confirmation: 用户确认按当前 MVP 进入实现准备。

## Technical Notes

* Relevant files inspected:
  * `DESIGN.md`
  * `README.md`
  * `CHANGELOG.md`
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` symbol map via search
* Likely source target:
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
* Relevant specs for implementation context:
  * `.trellis/spec/backend/index.md`
  * `.trellis/spec/backend/compose-patterns.md`
  * `.trellis/spec/backend/quality-guidelines.md`
