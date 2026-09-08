# PRD: Scroll & Gesture Performance — 滑动流畅度优化

## 背景

用户反馈 120Hz 手机上滚动、滑动、拖拽"卡卡的"，且长按拖拽失效。诊断确认了三类根因：
1. 拖拽无视觉反馈（DESIGN.md 写了 8dp lift + 1.02 scale，代码从未实现）+ DragDropListState 闭包捕获 bug + 拖拽中索引数学错误。
2. 多处重组热点在滚动路径上无 remember。
3. Compose BOM 2024.02.00（Compose 1.6 / M3 1.2）过旧，含 SwipeToDismissBox 已知 jank。
用户确认：Debug 包测试（本身比 release 慢 30-50%），优化范围全部 ①②③④，拖拽只修象限详情页。

## 需求列表（按 commit 批次）

### 批次 1 修拖拽排序（象限详情页）
- DragDropListState 闭包捕获修复：`remember { DragDropListState(...) }` 改为 onMove 用 `rememberUpdatedState` 持有，或 state 类不存 onMove 而是调用时从当前组合读取，保证 onMove 永远读最新值。
- 被拖拽行加视觉反馈：graphicsLayer 实现 DESIGN.md 的 Drag lift 规范——拖拽中 shadowElevation 8dp、scale 1.02，跟随手指 offset 实时位移（instant，无 tween），松手回 0。
- 修正拖拽索引数学：onDrag 中 draggedIdx（相对任务索引）与 visibleItemsInfo 的绝对 index 混用问题统一（统一用相对索引 + 固定前缀偏移）。
- 象限详情 itemsIndexed 行加 animateItemPlacement（modifier 放在 lazy item 直接子级）。
- 智能视图不加拖拽（用户确认，保持滑动操作不冲突）。

### 批次 2 重组热点修复
- HomeScreen：groupHomeSmartItems 移到 LazyColumn 外用 remember(items, homeSmartView) 计算。
- HomeScreen：buildTimeFocusItems 用 remember(tasks) 包裹。
- HomeScreen AppHeader：openTasks/timedTasks/completedTasks 三个统计 remember(tasks)。
- HomeScreen QuadrantBoard：四个 filter 提到 remember 层（或改用分组一次完成）。
- MainActivity：openDetailTaskCount 用 derivedStateOf(reorderableDetailTasks)。
- MainActivity quadrant detail LazyColumn 的 itemsIndexed 补 contentType。
- HomeScreen 智能视图 items 补 contentType。

### 批次 3 滑动手势重组修复
- SwipeableTaskRow backgroundContent：tint/label/icon/对齐改为 derivedStateOf(dismissState.targetValue) 驱动，滑动跟手位移不再引发背景每帧重组（位移本身由 SwipeToDismissBox 内部 offset 处理）。
- 删除无效的 animateItemPlacement（当前传在内层 TaskRow Card 上，是 no-op），如批次 4 未升级则先移除；升级后在 SwipeToDismissBox 层用 animateItem。

### 批次 4 升级 Compose + Kotlin
- libs.versions.toml：kotlin 1.9.22 → 2.0.x，ksp 对应 2.0.x-1.0.x，composeBom → 2024.09.03 或更高（Compose 1.7 / M3 1.3），kotlinCompilerExtensionVersion 移除（Kotlin 2.0 用 compose-compiler gradle plugin）。
- build.gradle.kts：加 org.jetbrains.kotlin.plugin.compose plugin。
- animateItemPlacement → animateItem（1.7 正式 API）。
- SwipeToDismissBox 相关 API 若有变更则适配。
- release buildType 开 isMinifyEnabled = true + proguard（验证 release 包性能；R8 keep 规则确认 Room/WebDAV 不被裁剪）。
- 全量验证：testDebugUnitTest、lintDebug、assembleDebug、assembleRelease。

## 非目标
- 智能视图列表不支持拖拽排序。
- 不引入 baseline profile 生成流程（可作为后续任务）。
- 不改任何视觉设计（动画时长、颜色等均遵守 DESIGN.md 现有规范）。

## 验证
- gradlew :app:testDebugUnitTest 全绿（现有 61 个测试不回归）。
- gradlew :app:assembleDebug / :app:assembleRelease 编译通过。
- gradlew :app:lintDebug 无新错误。
- 拖拽：象限详情页长按行 → 行抬起（阴影+缩放）→ 跟手移动 → 其他行让位动画 → 松手落位持久化。
- 滚动：智能视图快速滚动无可见掉帧（用户 release 包实测确认）。