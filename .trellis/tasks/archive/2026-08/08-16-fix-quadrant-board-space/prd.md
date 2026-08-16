# 修复首页四象限空间压缩

## Goal

首页四象限面板（QuadrantBoard）被上方元素挤压，可用垂直空间不足。通过压缩 AppHeader 摘要行、减少段落间距、减少顶部留白、以及将搜索框改为按钮触发的浮动搜索，释放更多垂直空间给四象限面板。

## What I already know

* 首页 Quadrants 视图垂直空间分配：AppHeader(~85dp) + HomeSmartControls(~130dp) + TimeFocusLane(~100-220dp) + 段落间距(48dp) + 顶部padding(8dp) ≈ 360-480dp 开销
* QuadrantBoard 使用 `Modifier.weight(1f)` 获取剩余空间
* AppHeader 包含：AppCompassMark(42dp) + 标题行 + SummaryPill FlowRow（独立一行）
* HomeSmartControls 包含：标题行 + FilterChip FlowRow + 全宽 OutlinedTextField 搜索框（始终可见）
* 首页 Column spacing = 16dp，top padding = 8dp
* 用户反馈：APP 标题与状态栏之间空太多；搜索可以做成按钮，点击出现浮动搜索框

## Requirements

### 1. 压缩 AppHeader

* 将 SummaryPill 从独立 FlowRow 行移到标题行右侧（与归档/备份按钮同行），或缩小为更紧凑的内联形式
* 如果摘要行无法放入标题行，则缩小 SummaryPill 尺寸（减小 padding 和字号）使其更紧凑
* 减少 AppHeader 内部 spacedBy(10dp) 到 7-8dp

### 2. 减少顶部留白

* 将首页 Column 的 top padding 从 8dp 减少到 4dp
* 减少 windowInsetsPadding 后的额外 top 空间

### 3. 减少段落间距

* 将首页 Column 的 verticalArrangement 从 spacedBy(16dp) 减少到 spacedBy(12dp)

### 4. 搜索改为按钮触发浮动搜索

* HomeSmartControls 中的 OutlinedTextField 搜索框改为搜索图标按钮
* 点击搜索按钮时，显示一个浮动搜索面板（Overlay 或顶部展开式）
* 搜索面板包含 OutlinedTextField + 关闭按钮
* 搜索面板使用 AnimatedVisibility 展开/收起
* 搜索面板展开时自动获取焦点
* 搜索面板不占用 HomeSmartControls 的固定高度——它是浮动的

### 5. 保持 HomeSmartControls 紧凑

* 移除搜索框后 HomeSmartControls 高度降低约 56dp
* 保持 "浏览" 标题 + 结果计数 + FilterChip 行
* 搜索按钮放在标题行右侧

## Acceptance Criteria

* [ ] AppHeader 摘要行更紧凑，不再独占一整行（或尺寸明显缩小）
* [ ] 首页顶部留白减少
* [ ] 首页段落间距从 16dp 减少到 12dp
* [ ] 搜索框改为按钮触发，默认不占用固定空间
* [ ] 点击搜索按钮展开浮动搜索面板，可输入关键词筛选
* [ ] 搜索面板可关闭，关闭后不占用空间
* [ ] QuadrantBoard 可用垂直空间明显增加
* [ ] 不改变任务数据模型、提醒、日历、重复规则、备份或导航底层契约
* [ ] 更新 CHANGELOG.md
* [ ] `:app:testDebugUnitTest`、`:app:lintDebug`、`:app:assembleDebug`、`git diff --check` 通过

## Definition of Done

* 改动聚焦首页空间优化
* 不新增外部依赖
* 不误提交任务外既有脏文件

## Technical Approach

* AppHeader：将 SummaryPill 移入标题行（与 compass、标题、操作按钮同行），使用 FlowRow 或 weight 布局；如果太挤则缩小 pill 尺寸
* 首页 Column：top padding 8dp→4dp，spacedBy 16dp→12dp
* HomeSmartControls：移除 OutlinedTextField，在标题行右侧加搜索 IconButton；新增 `searchVisible` 状态控制浮动搜索面板
* 浮动搜索面板：使用 AnimatedVisibility + Surface overlay，放在 HomeSmartControls 上方或内部顶部

## Out of Scope

* TimeFocusLane 的压缩（用户未选择）
* 数据模型、提醒、日历、备份变更
* 深色主题
* 大规模拆分 MainActivity.kt

## Technical Notes

* 目标文件：`app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
* 相关 composable：`DianMaoApp`（首页布局）、`AppHeader`、`HomeSmartControls`、`SummaryPill`
* 设计参考：`DESIGN.md`
