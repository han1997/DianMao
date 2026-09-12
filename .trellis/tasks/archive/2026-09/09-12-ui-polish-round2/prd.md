# UI/交互打磨 第二轮

## 范围（5 项，用户已确认全做）

1. **页面过渡**：Home ↔ QuadrantDetail、TaskDetail 覆盖、RecurringHistory 覆盖、Smart View 切换统一用 `Crossfade(motionTween(150))`（reduced-motion 下瞬时）。
2. **对比度**：`DianMaoAlpha.muted(0.62)` 拆分为 `mutedIcon`(0.62，图标/装饰) 与 `mutedText`(0.75，正文/空态/次级信息)；正文用途全部指向 `mutedText`。
3. **控件统一**：新 `ui/components/QuietControls.kt` 提供 `QuietSwitch` + `QuietDropdownMenu`；替换 `AddTaskPanel.kt` 3 处 Switch、`AddTaskPanel.kt:493,539` 及 `TaskRow.kt:221` 的 DropdownMenu。
4. **小确感动画**：TimeFocus 计数、QuadrantPane openCount、完成历史计数用 `AnimatedContent(motionTween(150))`；TimeFocusLane 进出包 `AnimatedVisibility(...).withReducedMotion()`。
5. **完成历史收敛**：抽 `CompletionHistoryShared`（空态 + 列表），inline 组与 RecurringTaskHistoryPanel 共用，消除文案漂移。

## 验收
- [ ] assembleDebug / testDebugUnitTest / lintDebug 全绿
- [ ] 深色模式 muted 文本≥4.5:1（0.75 目标）
- [ ] 无默认 M3 Switch/DropdownMenu 残留观感
- [ ] 系统动画关闭时 Crossfade/AnimatedVisibility 均瞬时
