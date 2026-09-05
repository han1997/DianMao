# UI comfort: spacing, transitions, refactor (界面舒适度、切换流畅度、代码结构优化)

## Goal

在上一轮结构性重排（09-04-ui-polish-secondary-pages）基础上继续优化：
1. **视觉舒适度** — 界面更舒服、清爽、不拥挤（间距/密度/呼吸感）
2. **切换流畅度** — 页面/面板切换动画更顺滑
3. **代码结构优化** — MainActivity.kt 单文件（约 5300 行）拆分重组

## What I already know

- 全部 UI 在 `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`（约 5312 行单文件）
- 上一轮已完成：TaskDetail verb rows、QuadrantDetail sticky 头 + 分节、AddTaskPanel sticky 捕获头 + chips 行、BackupPanel 设置页式、ArchivedPanel 元数据行、对话框 TextButton 化
- DESIGN.md 有完整设计系统（间距 token：screen-h 18dp / row-v 10dp / gap-md 8dp 等；motion：120-220ms state-change only）
- spec: `.trellis/spec/backend/compose-patterns.md`（BackHandler 快照、Overlay Panel 一致性、Capture-First 等契约）
- 用户反馈方向：不要太挤 → 现有间距 token 可能偏紧（row-v 10dp、section-v 11dp、gap-md 8dp）

## Assumptions (temporary)

- 代码结构优化 = 文件拆分（按 screen/组件分层），不改行为

## Open Questions

-（已全部解决，见 Decision）

## Requirements

### 1. 全局间距放松（visual comfort）

- 统一调大间距 token（DESIGN.md frontmatter + 代码引用同步）：
  - screen-h 18dp → 20dp；row-v 10dp → 12dp；section-v 11dp → 14dp
  - gap-md 8dp → 10dp；sheet-v 14dp → 16dp
- 列表行内边距与行间距增加呼吸感（TaskRow、面板内列表）
- 首页四象限面板、时间聚焦区、面板内部、详情页区块间距全面放松
- 保持安静风格：不加阴影、不加大圆角，只动间距与密度
- 检查小屏（360dp 宽）不破版

### 2. 切换动画微调（transitions）

- 保持现有 fade+slide 语言，时长从 150/220ms 放宽到约 200/280ms（仍在 FastOutSlowInEasing）
- 面板进出（AddTask/Backup/Archived）、折叠组展开、save pill、完成缩放同步微调
- DESIGN.md Motion Vocabulary 更新为新时长（State-Change Only 原则不变，只是数值放宽）
- 列表项 animateItemPlacement 保留默认时长

### 3. 代码结构拆分（refactor，不改行为）

- MainActivity.kt（约 5300 行）拆分为：
  - `ui/theme/` — DianMaoTheme、形状、quietBorder、quietTextFieldColors
  - `ui/components/` — IconTile、HeaderActionButton、TaskFormSection、FormSettingBlock、CompactMeta、ColorDot、CompletionMark、ToggleRow 等复用组件
  - `ui/screens/` — DianMaoApp 主屏（含 AppHeader/QuadrantBoard/TimeFocusLane）、QuadrantDetail、TaskDetail
  - `ui/panels/` — AddTaskPanel、BackupPanel、ArchivedPanel、RecurringTaskHistoryPanel、对话框
- MainActivity.kt 只保留 Activity 入口 + 数据装配（repository/scheduler/回调 wiring）
- 纯移动代码：不改任何行为、签名、逻辑；data 层不动
- 纯函数（buildTimeFocusItems、buildHomeSmartTaskItems、resolveInAppBackTarget 等）与既有单测的 import 路径保持兼容（测试文件同步更新或保持 internal 可见性）

## Acceptance Criteria

- [ ] 间距 token 更新且全 UI 引用同步（DESIGN.md frontmatter 数字与代码一致）
- [ ] 首页/面板/详情页肉眼可见更宽松，无拥挤感
- [ ] 动画时长统一更新，进出更顺滑；DESIGN.md Motion Vocabulary 数值更新
- [ ] MainActivity.kt 缩减为入口+装配（目标 <500 行）
- [ ] 拆分后无行为变化（现有单测全过，无逻辑改动）
- [ ] `.\gradlew.bat :app:testDebugUnitTest --no-daemon` 通过
- [ ] `.\gradlew.bat :app:lintDebug --no-daemon` 通过
- [ ] `.\gradlew.bat :app:assembleDebug --no-daemon` 通过
- [ ] DESIGN.md 与 .trellis/spec/backend/compose-patterns.md 同步（文件结构、Reference implementation 路径）

## Definition of Done

- Gradle 三项检查全绿
- DESIGN.md / spec / CHANGELOG 同步更新
- 目录结构：`ui/theme`、`ui/components`、`ui/screens`、`ui/panels`

## Decision (ADR-lite)

**Context**: 用户反馈界面挤、切换不够顺滑、代码单文件过大；要求继续优化。

**Decision**（2026-09-05 用户确认）:
1. 间距全面放松（统一调大 token）
2. 动画微调现有 fade+slide 语言（150/220 → 约 200/280ms），State-Change Only 原则不变
3. 按页面拆分文件：theme/components/screens/panels 四层

**Consequences**: DESIGN.md 间距与 motion 数值更新；spec 的 Reference implementation 路径更新；拆分是纯移动不改行为；动画时长放宽需在 DESIGN.md 记录新数值。

## Out of Scope

- 新功能
- 深色模式
- M3 Shared Axis 等新转场语言（用户选了微调现有）
- data 层改动

## Technical Notes

- 入口：MainActivity.kt（5312 行）
- 拆分注意：internal 可见性（buildTimeFocusItems 等被测试引用）；包名 `fun.han1997.dianmao.ui.*`
- 上一轮任务归档：.trellis/tasks/archive/2026-09/09-04-ui-polish-secondary-pages/
