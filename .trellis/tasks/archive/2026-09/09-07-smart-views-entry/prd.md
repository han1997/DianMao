# Restore smart views entry on home

## Goal

The `HomeSmartView` model (Today/Upcoming/Inbox) and `buildHomeSmartTaskItems`
already exist with unit tests, but the home UI entry was removed. Restore a
lightweight browsing entry so users can see 今天/近期/收集箱 like every
mainstream app, without weakening the four-quadrant model as the primary home.

## What I already know

- `ui/logic/HomeModels.kt`: `HomeSmartView { Quadrants, Today, Upcoming, Inbox }`,
  `buildHomeSmartTaskItems(tasks, view, query, nowMillis, zoneId)` — pure and
  tested (TimeFocusItemsTest / HomeSmartTaskItems tests).
- Home currently renders AppHeader + TimeFocusLane + QuadrantBoard only.
- DESIGN.md: quadrant board stays primary; smart views must not become a
  second planner model (they are projections).
- TaskRow already renders smart items with `dueLabelOverride`.

## Requirements

- 首页提供轻量入口切换 四象限/今天/近期/收集箱(默认四象限)。
- 智能视图复用 TaskRow 列表渲染,支持既有行操作(完成/编辑/详情)。
- 不新增持久化字段;搜索在当前视图内生效。
- 视觉沿用 quiet 语言(与 AppHeader 同族,不引入 dashboard 感)。

## Acceptance Criteria

- [ ] 首页可在四象限与三个智能视图间切换,默认四象限。
- [ ] 今天=过期+今日定时;近期=未来7天;收集箱=无日期;均排除已完成/归档。
- [ ] 行操作与详情/编辑流程与象限详情一致;返回/状态不回归。

## Out of Scope

- 新的持久化模型、标签/清单、统计页。

## Technical Notes

- 入口形态:AppHeader 下的一行分段切换(SegmentedButtonRow,与优先级选择器同族)。
- 复用 quadrant detail 的 LazyColumn 组装方式。
