# UI Audit Report — 09-16-ui-polish

> 全量比对代码与 `DESIGN.md`、`.trellis/spec/backend/compose-patterns.md`。默认原则:以 DESIGN.md 为准,改代码;例外(代码优于文档)单独列出待用户决策。

审计日期:2026-09-16。覆盖全部 19 个 UI 文件。

---

## 结论摘要

整体对齐度很高(项目经多次 ui-polish 迭代)。本轮发现的**真实代码侧偏差**集中在象限透明度、时间关注行、任务行边框、Outlined 按钮内边距四处,均为小幅视觉偏差。另有 4 处**文档落后于代码**(代码为有意演进,需改 DESIGN.md)。

---

## A. 代码侧偏差(修复代码对齐 DESIGN.md)

### A1. Home 象限面板 tint 透明度 8% → 4.5% 【真实偏差,建议修复】

- **代码**: `ui/screens/HomeScreen.kt:503` — `QuadrantPane` 的 `paneColor` 使用 `quadrant.color.copy(alpha = DianMaoAlpha.subtle)` (`subtle = 0.08f`)。
- **规范**: DESIGN.md:196、275 — **Home board 象限 pane 用 0.045 alpha (4.5%)**;quadrant detail header / empty state 才用 0.08。
- **依据**: "The Quadrant Overlay Rule. Quadrant colors appear as low-alpha surface tints... Home-board quadrant panes use 0.045 alpha."
- **处理**: Home 面板背景改 0.045。因其是文档明示的设计值,新增 token `DianMaoAlpha.boardPane = 0.045f`(DianMaoTokens 契约允许文档需要的值)。detail header 的 0.08 不受影响。

### A2. TimeFocusRow 行背景/边框 0.08 → 0.07 / 0.12 【真实偏差,建议修复】

- **代码**: `ui/screens/HomeScreen.kt:342-343` — `TimeFocusRow` 背景 `color.copy(alpha = DianMaoAlpha.subtle)`(0.08),边框同样 0.08。
- **规范**: DESIGN.md:279 "Time Focus Lane... Per-row background uses the lane color... at 0.07 alpha with a 0.12-alpha border in the same color."
- **处理**: 行背景 0.07、同色边框 0.12。沿用字面值(`0.07f`/`0.12f`),这是文档定义、token 之外的语义填色,不新增 token。

### A3. TaskRow 活动态边框 0.16 → 0.18 【真实偏差,建议修复】

- **代码**: `ui/components/TaskRow.kt:150` — 活动任务行 `quietBorder(0.16f)`;完成行 `0.12f`。
- **规范**: DESIGN.md:272 "Task Row Card: ... quiet border at 0.18 alpha. ... completed tasks, surfaceVariant at 0.38 alpha" (完成行 border 0.12 见 §5 Collapsed Completed Group / Quiet Border Pattern)。
- **处理**: 活动行边框改 0.18。完成行 0.12 保持。

### A4. OutlinedButton 垂直内边距 13dp → 11dp 【真实偏差,建议修复】

- **代码**: `TaskDetailScreen.kt:291`(打开系统日历提醒)、`BackupPanel.kt:216,246,349` 的 `OutlinedButton` 用 `contentPadding = PaddingValues(vertical = 13.dp)`。
- **规范**: DESIGN.md:97 "button-outlined padding: 0dp 11dp";§5 Buttons "Outlined: ... 11dp vertical padding."
- **处理**: 四处 OutlinedButton 垂直内边距改 11.dp。

### A5. HomeScreen 死亡 import `FilterChip` 【代码卫生,建议移除】

- **代码**: `HomeScreen.kt:72` import `androidx.compose.material3.FilterChip`,全文件无使用(DESIGN 曾规划 FilterChip 优先级选择器,现已被 SegmentedButton 取代)。
- **处理**: 删除该 import。

---

## B. 文档落后于代码(代码是有意演进,待用户决策是否更新 DESIGN.md)

### B1. 暗色模式已经从"未来任务"变为已实现

- **代码**: `ui/theme/DianMaoTheme.kt:157-176` — 已存在完整手工调优的 `darkColorScheme`(暖棕灰世界、明亮化 terracotta/calendar-blue、低透明度象限 tint)。
- **文档**: DESIGN.md:153 "The Compose theme defines a single `lightColorScheme`; dark mode currently falls back to Material 3 defaults... A proper `darkColorScheme` is a future task."
- **方向**: 文档明显过时。建议更新 DESIGN.md:153 + Key Characteristics 段,删除"future task"表述,补一段暖暗色描述。

### B2. 优先级选择器已从 FilterChips 演进为 SegmentedButton

- **代码**: `ui/panels/AddTaskPanel.kt:313-341` 用 `SingleChoiceSegmentedButtonRow` + `SegmentedButton`(带 8dp ColorDot);`HomeScreen.kt:657` 的视图切换器复用同一 family。
- **文档**: DESIGN.md:293-302 "优先级 holds the four quadrant FilterChips (12dp ColorDot)。… values rows。"
- **方向**: DESIGN.md Chips 段落里的 "优先级 ... FilterChips(12dp ColorDot)" 已过时;但 DESIGN.md:647-650 Home 视图切换器注释明确 "uses the same segmented-row family as the form's priority selector",说明 segmented 是既有意图。建议改为 "优先级 holds a single segmented row of four quadrant options (8dp ColorDot), same family as the home smart-view switcher."

### B3. "表单对话框仍是底部弹层"的说法已过时

- **代码**: 完成备注对话框(`Dialogs.kt` CompletionNoteDialog)与日期/时间选择器(`DateTimePickers.kt`)全部走 `QuietDialogContainer` / `BrandedPickerDialog`(居中 PanelShape Card,见 DESIGN.md:313-337)。
- **文档**: DESIGN.md:274 "A 6dp sheet Card still wraps the completion-note form and the time picker dialog."
- **方向**: DESIGN.md:274 与它自己的 §Dialogs / §Date & Time Pickers 自相矛盾。建议删除:274 的 sheet 包装表述,改为指向 QuietDialogContainer 居中对话框 family。

### B4. 保存后行为:设计稿"capture-first 保持打开",代码"单次保存即关闭" 【行为偏差,建议单独决策】

- **代码**: `ui/panels/AddTaskPanel.kt:230-236` — 保存成功后 `onDismiss()` 直接关闭;`saveFeedback` 成功路径不再展示(KDoc/注释 "Both add and edit close after a successful save — capture is single-shot, no continuous-recording mode.")。
- **规范**: DESIGN.md:348 "Capture-First Save Pattern ... When the user saves a newly-created task ... screen does NOT dismiss. Instead it shows a save-feedback pill ... stays open so the user can capture the next item.";compose-patterns.md "Capture-First Task Entry" 同样要求 add 模式保存后保持打开。
- **性质**: 这是**交互/功能语义**偏差,不是纯视觉。本文 task 明确「不改交互逻辑」。
- **建议**: 本次**不改动**(避免功能范围漂移);在报告中记录,作为独立小任务(可后续把保存改成:add 成功 → 保持打开 + 显示 pill + 清空字段;edit 成功 → 关闭)。需用户确认是否要单独拆单。

---

## C. 经比对确认合规的项(抽样,非穷举)

- Home 板和面板均无 rest 阴影(`shadowElevation=0` / `cardElevation(0.dp)`),符合 "Surface Before Shadow"。
- TaskRow 完成行 `surfaceVariant.copy(alpha=0.38f)` ✓ (DESIGN:272)。
- Tile/IconTile 3 档尺寸、HeaderActionButton 48dp IconButton + IconTile ✓ (compose-patterns Accessible Header Actions)。
- QuietDialogContainer / BrandedPickerDialog 居中 PanelShape + 6dp elevation + quiet hairline + warm scrim `#211815@0.16` + TextButton actions ✓ (DESIGN:313-337)。
- FullScreenScaffold:`background` 全屏 Surface + safeDrawing insets + 600dp 内容宽帽 + 顶部 quiet bar ✓ (DESIGN:273、compose-patterns Full-Screen Safe-Area Ownership)。
- 时间关注 lane 容器 RowShape/raised/border 0.14 ✓ (DESIGN:279)。
- QuadrantDetailHeader:tint 8%、InlineShape、quiet border 0.16、12dp dot、open count ✓ (DESIGN:276)。
- QuadrantDetailCompletedGroup / CompletionHistoryGroup:surfaceVariant 0.20、RowShape、quiet border 0.12 ✓ (DESIGN:278)。
- 全部动画走 `motionTween` + `withReducedMotion`,时长落在 140–280ms,统一 FastOutSlowIn ✓ (DESIGN:232-258、Global Reduced-Motion Gate)。
- 搜索是按钮触发的浮动面板+自动聚焦+关闭清空 ✓ (Button-Triggered Floating Search)。
- Switch 全部走 `QuietSwitch`;DropdownMenu 走 `QuietDropdownMenu`(无阴影、quiet border、RowShape)✓ (Quiet Controls round 2)。
- 次级文本/边框 alpha 全部落在 `DianMaoAlpha` 四档内,无散落新值(除上述 A2 文档语义填色)✓。
- `Color.White` / `Color.Black` / 负 letterSpacing 搜索:0 命中 ✓ (Quiet Daily Task UI)。
- 无障碍:回退按钮为 48dp IconButton、图标带 contentDescription、`FlowRow` 处理换行 ✓ (compose-patterns Accessible Header Actions)。

---

## D. 待办清单(本次执行范围)

| # | 项 | 文件 |
|---|---|---|
| 1 | Home 面板 tint 4.5% | `HomeScreen.kt` + 新增 token |
| 2 | TimeFocusRow 0.07/0.12 | `HomeScreen.kt` |
| 3 | TaskRow 活动边框 0.18 | `TaskRow.kt` |
| 4 | OutlinedButton padding 11dp | `TaskDetailScreen.kt`、`BackupPanel.kt` |
| 5 | 删死亡 import FilterChip | `HomeScreen.kt` |

**待用户决策**:B1 / B2 / B3(更新 DESIGN.md)、B4(是否单独拆单处理 capture-first 保存行为)。