# 移除首页浏览模块，保留搜索

## Goal

移除 HomeSmartControls 浏览模块（"浏览"标题 + 结果计数 + FilterChip 视图切换 + 搜索面板），将搜索按钮移到 AppHeader 标题行，让四象限面板获得最大垂直空间。同时减少 DianMao 标题与状态栏之间的留白。

## What I already know

* HomeSmartControls 包含：浏览标题行 + FilterChip FlowRow + 浮动搜索面板，约占 80-130dp
* 搜索按钮当前在 HomeSmartControls 标题行右侧
* AppHeader 标题行已有归档和备份两个 HeaderActionButton
* 首页 Column top padding = 4dp，但 windowInsetsPadding(safeDrawing) 在 Box 层级，导致状态栏下方仍有额外间距
* homeSmartView 状态控制 Quadrants/Today/Upcoming/Inbox 视图切换
* 移除浏览模块后，Today/Upcoming/Inbox 视图将不再可从首页访问——需要决定是否保留这些视图

## Requirements

### 1. 移除 HomeSmartControls

* 从首页布局中移除整个 HomeSmartControls composable 调用
* 移除后首页布局为：AppHeader → TimeFocusLane → QuadrantBoard(weight 1f)
* homeSmartView 状态保留默认值 Quadrants，不再从 UI 切换
* HomeSmartTaskList 路径不再从首页触发（homeSmartView 始终为 Quadrants）

### 2. 搜索按钮移到 AppHeader

* 将搜索按钮（HeaderActionButton）添加到 AppHeader 标题行，与归档、备份按钮并列
* 搜索浮动面板（AnimatedVisibility + OutlinedTextField）放在 AppHeader 内部或首页 Box overlay
* 搜索面板展开时覆盖在内容上方，不占用固定空间
* 保持搜索筛选功能：输入关键词仍筛选当前视图的任务

### 3. 减少 DianMao 标题与状态栏间距

* 当前 Box 使用 windowInsetsPadding(WindowInsets.safeDrawing)，然后 Column 再加 top=4dp
* 将 safeDrawing 的 top inset 处理方式改为仅应用 status bar 部分，或减少 Column top padding 到 0-2dp
* 目标：DianMao 标题紧贴状态栏下方，仅有最小间距

### 4. 清理

* HomeSmartControls composable 可以保留在代码中（不删除函数定义），但从首页布局中移除调用
* 或者如果确认不再需要，可以删除函数定义和相关状态

## Acceptance Criteria

* [ ] HomeSmartControls 不再出现在首页布局中
* [ ] 搜索按钮移到 AppHeader 标题行
* [ ] 搜索浮动面板仍可展开/关闭，筛选功能正常
* [ ] DianMao 标题与状态栏间距明显减少
* [ ] QuadrantBoard 获得更多垂直空间
* [ ] 不改变任务数据模型、提醒、日历、重复规则、备份或导航底层契约
* [ ] 更新 CHANGELOG.md
* [ ] `:app:testDebugUnitTest`、`:app:lintDebug`、`:app:assembleDebug`、`git diff --check` 通过

## Definition of Done

* 改动聚焦首页布局简化
* 不新增外部依赖
* 不误提交任务外既有脏文件

## Technical Approach

* AppHeader：新增 `onSearchToggle` + `searchVisible` + `query` + `onQueryChanged` 参数；在标题行归档按钮前加搜索按钮；在 AppHeader Column 末尾加 AnimatedVisibility 搜索面板
* 首页 Column：移除 HomeSmartControls 调用；top padding 从 4dp 减到 0dp；safeDrawing top inset 通过 windowInsetsPadding 处理
* 搜索面板：使用 Box overlay 或放在 AppHeader 内部底部，用 AnimatedVisibility 控制

## Out of Scope

* 删除 HomeSmartControls 函数定义（保留代码，仅移除调用）
* 删除 HomeSmartView 枚举（保留，默认 Quadrants）
* 数据模型、提醒、日历、备份变更
* 深色主题

## Technical Notes

* 目标文件：`app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
* 相关 composable：`DianMaoApp`、`AppHeader`、`HomeSmartControls`
* 设计参考：`DESIGN.md`
