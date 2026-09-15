# PRD: Feature polish v1.0.8 — 通知快捷完成 / 小组件交互 / 日历月视图 / 批量操作

## Background

DianMao（Android Compose 待办，v1.0.7）核心功能已完整。本次继续完善四个方向（用户在计划阶段确认，含两轮决策）：

1. 通知直达与快捷完成
2. 小组件交互增强
3. 日历月视图
4. 批量操作

一个任务、四个阶段顺序实施，每阶段独立验证。

## 已确认的决策

- 组织方式：4 个方向一个任务，按阶段顺序实施，每阶段独立验证。
- 通知「稍后提醒」间隔：固定 10 分钟。

## 阶段 1：通知直达与快捷完成

现状（已研究）：
- 通知构建于 `alerts/TaskReminderReceiver.kt:34-46`，contentIntent 带 `EXTRA_TASK_ID` 已能直达任务详情（冷启动 `initialTaskId` / 热启动 `onNewIntent` 静态回调桥）。
- 全库无 `NotificationCompat.Action`；`ReminderScheduler` 每个 (taskId, leadMinutes) 独立闹钟，requestCode = `taskId.hashCode() * 31 + leadMinutes`，通知 id 复用同一函数。
- 完成调用链（app 内滑动完成，`MainActivity.kt:405-414`）：`scheduler.cancel` → `repository.completeSilently`（循环任务推进周期/普通任务完成并自动归档）→ 循环任务重排。
- `TaskRepository.completeSilently(task: TaskUiModel)` 在 `TaskRepository.kt:186-188`；`dao.findById(id)` 存在。

需求：
- 新增 `alerts/TaskActionReceiver.kt`（BroadcastReceiver），处理两个通知 action：
  - **完成**：`findById` 查任务（查不到直接返回）→ `scheduler.cancel(taskId, leads)` → `repository.completeSilently` → 循环任务且开本地提醒时 `scheduler.schedule(next)` → `refreshHomeWidget(context)` → `NotificationManagerCompat.cancel(notificationId)`。无 `POST_NOTIFICATIONS` 权限时照旧检查。
  - **稍后提醒**：以共享的 reminder PendingIntent 规则 `setExactAndAllowWhileIdle(now + 10 分钟)`，并取消当前通知；到点由现有 `TaskReminderReceiver` 自然再次弹出。精确闹钟无权限时降级 `setAndAllowWhileIdle`（与 Scheduler 现行为一致）。
- `TaskReminderReceiver` 通知加这两个 action（`NotificationCompat.Action`，requestCode 用独立偏移避免与闹钟 PendingIntent 冲突）。循环任务 action 文案「记一次完成」，非循环「完成」。
- 从 `ReminderScheduler` 抽出共享的 `reminderPendingIntent(context, taskId, leadMinutes)` 构造函数，schedule / cancel / snooze 共用。
- 完成语义 = 滑动完成（静默、不弹心得，可在详情后补笔记）。
- 提取一个共享的"完成任务全局副作用"助手（取消闹钟 + completeSilently + 循环重排），供通知 action 与阶段 2 小组件复用；MainActivity 滑动完成如可无痛改用则一并收敛，否则保持不动。

验收：
- 通知出现「完成」「稍后提醒」按钮；完成后的行为与 app 内滑动完成一致（含小组件刷新）；稍后提醒 10 分钟后再次弹出。
- 单元测试：snooze 触发时间计算纯函数。

## 阶段 2：小组件交互增强

现状（已研究）：
- `widget/DianMaoWidget.kt`（187 行）：任务行仅标题+时间标签、点击 deep link 进详情；`actionRunCallback` 全库未用；`WIDGET_TASK_LIMIT = 4` 固定；尺寸 `dianmao_widget_info.xml`（3×2 cell，resizeMode h|v）；颜色硬编码 day/night 色对；刷新 = app 内 `LaunchedEffect(activeTasks)` + 30 分钟系统周期。

需求：
- `WidgetTaskRow` 行前加圆形勾选位：`actionRunCallback<ToggleWidgetTaskCallback>`（actionParameters 带 taskId），复用阶段 1 共享完成助手（completeSilently + 闹钟取消/循环重排 + `refreshHomeWidget`）。标题/时间点击仍 deep link 进详情。
- 用 `SizeMode.Responsive`（Glance 1.1.1 API 为准）提供两档布局：窄组件 ≤4 条（现状），宽组件最多 8 条。

验收：
- 小组件上点勾选直接完成任务且组件立即刷新；宽组件显示更多条目；day/night 配色不变。

## 阶段 3：日历月视图

现状（已研究）：
- 无导航库；全屏次级页面模式 = `showXxx` state + `AnimatedVisibility`/visible 参数 + `FullScreenScaffold`（参考 `ReviewScreen`）。
- `InAppBackTarget` 枚举与 `resolveInAppBackTarget` 在 `ui/logic/HomeModels.kt:15-22 / 242-259`；BackHandler 在 `MainActivity.kt:447-466`；FAB 守卫 `MainActivity.kt:477-479`。
- 数据：`repository.activeTasks` Flow 已含 `dueAtMillis`/`repeatRule`/`completed`；循环任务当前周期到期用现成 `effectiveDueAtMillis`（`HomeModels.kt:201-215`）。
- 入口：`HomeScreen.kt:151-165` AppHeader 搜索按钮旁。

需求：
- 新建 `ui/screens/CalendarMonthScreen.kt`，复刻 ReviewScreen 模式（visible 参数 + FullScreenScaffold，标题「日历」）：
  - 月份切换（‹ ›，显示「2026年9月」）；周一开头的 7 列网格；今天高亮、选中日填充、非本月日期置灰。
  - 每天格子显示最多 3 个象限色点 + 溢出 "+n"（纯内存过滤 activeTasks，无需 DB 迁移）。
  - 点选某天 → 网格下方展开当天任务列表（未完成 + 已完成置灰态），点击行进详情。
  - 当天列表带「记一笔」入口 → 打开新增面板并预置该天到期日：`AddTaskPanel` 增加可选 `presetDueAtMillis` 参数（仅新增模式生效）。
- MainActivity 接线：`showCalendarView`（rememberSaveable）、`InAppBackTarget.CalendarView` 入枚举 + `resolveInAppBackTarget` 参数与分支、BackHandler 分支、FAB 守卫加 `!showCalendarView`、Box 内覆盖层渲染。
- 入口：AppHeader 搜索旁加日历图标按钮。
- 补 `InAppBackTargetTest` 用例（CalendarView 优先级）。

验收：
- 月视图可浏览每月任务分布（循环任务按当前周期归日）、点天看列表、从当天快速新增（到期日已预置）；返回链不破坏。

## 阶段 4：批量操作

现状（已研究）：
- 行组件 `TaskRow`（`ui/components/TaskRow.kt:76-92`）无长按参数；主页智能列表 `HomeSmartTaskList`（HomeScreen.kt:684-789）行包在 `SwipeableTaskRow`；无 ViewModel，MainActivity 直接持有 repository + state。
- 删除链（单条，`MainActivity.kt:1110` 附近）：confirm → `scheduler.cancel` → `repository.deleteTask` → 撤销窗口关闭后清日历事件 → 撤销恢复原实体。
- 批量移动象限需定向更新：`TaskDao` 现无 `updateQuadrant`。

需求：
- `TaskRow` 加可选 `onLongPress` 参数；长按进入多选（主页智能视图列表与四象限板同时生效，共用组件）。
- 多选态：行首勾选框、点击切换选中、滑动禁用；底部操作栏（「N 已选」+ 全选/取消）。
- 批量完成：逐条复用共享完成助手（不弹心得）+ 刷新小组件。
- 批量移动象限：四象限选择对话框 → DAO 加 `updateQuadrant(ids, quadrant)` 定向更新（quadrant + updatedAt，无迁移）。
- 批量删除：确认对话框 → 逐条走现有删除链（提醒取消 + 日历事件清理）；撤销横幅一次性恢复全部已删实体。

验收：
- 长按进入多选后可批量完成/移动象限/删除并全部可撤销恢复；单选行为不受影响。

## 收尾

- 每阶段跑 `:app:assembleDebug` + `testDebugUnitTest`；新增纯函数单测（snooze、月网格/按天分组、选中集合逻辑）。
- 更新 `CHANGELOG.md`、`README.md`；版本 1.0.7 → 1.0.8。
- 分阶段提交（每阶段 1 commit，收尾 1 commit），推送由 owner 手动执行。

## 约束

- 遵循 `.trellis/spec/backend/` 现有约定（UTF-8、无注释除非必要、复用现有组件与视觉语言）。
- 不引入新依赖（Glance/Room/Compose 既有）。
