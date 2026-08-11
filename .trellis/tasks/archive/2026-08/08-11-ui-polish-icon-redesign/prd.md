# 软件 UI 统一与图标重设计

## Goal

将 DianMao 的现有 Android/Compose 界面做一次成体系的视觉打磨：统一界面语言，降低图标老气和同质感，强化个人任务管理工具应有的清爽、快速、可信赖体验，同时不破坏现有任务、提醒、备份、归档、详情等功能。

## What I already know

* 用户明确要求：软件打磨、UI 统一、图标过于老气、重新设计。
* 项目是 Android Compose 应用，主 UI 主要集中在 `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`。
* 现有图标全部来自 Material Rounded / AutoMirrored Rounded，例如 Add、Archive、Settings、Inventory2、DoneAll、CalendarMonth、Alarm、Delete、Edit 等。
* 项目已经有 `PRODUCT.md` 和 `DESIGN.md`，产品定位是个人 Android 任务管理工具，关键词是 simple、fluid、polished。
* 现有设计系统要求保留 Material 3、暖中性色背景、terracotta 主强调色、calendar blue 仅用于时间/日历语义、低噪声、低阴影、低动画。
* `gradle/libs.versions.toml` 已包含 Compose Material Icons Extended；不应为了图标打磨轻易引入新的图标库，除非确认依赖和维护成本可接受。

## Assumptions

* 本次优先做应用内核心界面打磨，不先做功能重构。
* 图标“重新设计”可以理解为建立更现代、更统一的图标使用规则和视觉容器，而不是必须新增外部矢量图标包。
* 用户已确认本次包含启动图标/桌面图标资源，范围是应用内 UI + launcher icon 一起改。

## Requirements

* 统一首页、四象限、任务列表、任务详情、新增/编辑、备份、归档等主要界面的视觉语言。
* 建立统一的图标尺寸、颜色、容器、语义映射，减少老式 Material Rounded 图标直接裸露的感觉。
* 重新设计启动图标/桌面图标，使它和应用内的“四象限 + 时间指南针 + 低噪声个人工具”视觉语言一致。
* 保持现有产品设计原则：快速捕获、低噪声、Android 原生、语义色清晰、任务行静态无重阴影。
* 不改变任务数据模型、提醒/日历/备份行为和导航逻辑。
* 避免大面积高饱和颜色、花哨动效、重仪表盘化、非原生 Android 控件。

## Acceptance Criteria

* [ ] 主要操作图标在尺寸、描边感、颜色和触控区域上保持一致。
* [ ] 首页、列表、详情、面板之间的卡片/Surface/按钮/chip 风格统一。
* [ ] 旧感较重或语义弱的图标用更合适的图标、组合或容器替代。
* [ ] 启动图标/桌面图标完成重设计，并更新 adaptive icon 相关资源。
* [ ] 变更后编译通过。
* [ ] 现有测试通过或明确说明无法运行的原因。

## Definition of Done

* 代码改动聚焦在 UI/视觉层。
* 运行项目可用的 lint、type-check 或 test 命令。
* 对照 `DESIGN.md` 做一致性检查。
* 如果发现值得沉淀的新 UI 规则，更新 Trellis spec 或设计文档。

## Decisions

* Scope: include both in-app UI/icon polish and launcher icon redesign.

## Technical Notes

* Relevant files inspected:
  * `PRODUCT.md`
  * `DESIGN.md`
  * `app/build.gradle.kts`
  * `gradle/libs.versions.toml`
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
* Existing dirty files before this task:
  * `.idea/misc.xml`
  * `.impeccable/`
* Impeccable context reported `PRODUCT.md` is on an older schema. Treat `## Register` as absent for design decisions during this task unless the user asks for `init`.

## Out of Scope

* Backend/data-model changes.
* New task/reminder/calendar features.
* Dark theme redesign unless explicitly added.
* Large architecture split of the single Compose file unless required for the UI changes.
