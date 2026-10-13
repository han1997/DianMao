# PRD: 小缺口修补 — 关于弹层接通 + 空状态引导 + 通知设置入口

来源:功能盘点(2026-10-13)发现三个收尾缺口,用户确认本次范围为此三项。
完整实现计划见 `C:\Users\hanhu\.claude\plans\robust-twirling-fog.md`(已获用户批准)。

## 背景

1. 设置页「关于 DianMao」是死按钮(`SettingsScreen.kt:137` `onClick = {}`),而原型已有完整 About 弹层(`design/mockup/app.js:897-901`,含 `__APP_VERSION__` 令牌)。
2. 新安装自动灌 13 条演示任务(`seedIfEmpty` 链路:`MainActivity.kt:416` → `TaskRepository.kt:29` → `PlatformHelpers.kt:79`),无空状态引导。
3. 无应用内通知设置入口(仅系统设置预览页)。

## 目标(三项)

### A. 关于弹层接通(无需改原型)

- `ui/panels/Dialogs.kt` 新增 `AboutDialog(onDismiss)`,用 `QuietDialogContainer`,正文逐字对齐原型:
  粗体 `DianMao v${BuildConfig.VERSION_NAME}` + 「个人任务指挥台。四象限决定方向,时间决定什么时候看它。」
  + 「设计语言:The Day Compass。数据与偏好仅保存在本机;日历、提醒与备份均由你显式授权后才发生。」
- `SettingsScreen.kt` 加 `onOpenAbout` 参数接通 line 137;`MainActivity.kt` 加 `showAboutDialog` 状态并渲染。

### B. 移除演示数据 + 空状态引导(先改原型)

- 原型 `quadrantsView()`:零任务且无搜索词时渲染首启空态(复用搜索空态结构):compass 图标 + 「创建第一个任务」+ 引导文案 + tonal「新增事项」按钮。
- 原型 `smartList()`:全库无活跃任务时显示同样引导。
- Compose:删除 seed 链路三处(`MainActivity.kt:416` 调用、`TaskRepository.kt` `seedIfEmpty`、`PlatformHelpers.kt` `seedTaskEntities`);`QuadrantBoard`/`HomeSmartTaskList` 加空态分支与 `onAddTask`。
- **决策:彻底移除 seed,不留 debug 开关**(用户已确认改为空状态引导)。

### C. 通知设置入口(先改原型)

- 原型 `widgetContent()` 两个通知预览后加 `.settings-row`:`row('open-notification-settings', 'bell', 'var(--primary)', '通知设置', '打开系统的应用通知设置', '')`,action 用 toast 演示。
- Compose:`WidgetNotificationsScreen` 加该行;`PlatformHelpers.kt` 新增 `openAppNotificationSettings(context)`(`ACTION_APP_NOTIFICATION_SETTINGS` + `EXTRA_APP_PACKAGE`,`runCatching` 包裹)。

## 流程(prototype-first 契约)

1. Phase 1:改原型(app.js + smoke-test.cjs 断言 + BASELINE.md v1.7 + SHA)→ 跑 smoke test
2. Phase 2:**STOP — 用户确认关卡**:展示零任务首页明暗、widget 页新行、About 弹层截图;明确告知 seed 移除改变新装首启体验
3. Phase 3:Compose 实现(项 A 可先行,项 B/C 待确认后)
4. Phase 4:验证 — `:app:assembleDebug` + `:app:lintDebug` + `:app:testDebugUnitTest` + 原型 smoke test + 明暗并排截图 + 手动验证

## 非目标

- 不做标签/分类、排序选项、归档/回顾搜索、备份合并模式(后续任务)。
- 不重构 `MainActivity` 状态枢纽;不动 `resolveInAppBackTarget`/BackHandler。
- 不伪造系统通知设置页面(原型仅 toast 演示)。

## 验收标准

- [ ] 设置页「关于 DianMao」点击弹出与原型逐字一致的 About 弹层,版本号来自 `BuildConfig.VERSION_NAME`。
- [ ] 新安装(或清数据后)首页四象限/智能视图显示空状态引导,「新增事项」打开 AddTaskPanel;无演示任务。
- [ ] 「小组件与通知」页有「通知设置」行,点击跳系统应用通知设置。
- [ ] 原型 smoke test 通过,含新增空态/widget 行/About 弹层断言。
- [ ] BASELINE.md 记录 v1.7 变更与新 SHA-256。
- [ ] `:app:assembleDebug` + `:app:lintDebug` + `:app:testDebugUnitTest` 全绿。
- [ ] 明暗两主题并排图更新(零任务首页、widget 页、About 弹层)。

## 已确认决策(2026-10-13)

- 范围:仅三项小缺口修补(用户多选确认)。
- 演示数据:改为空状态引导,彻底移除 seed,不留 debug 开关。
- 通知设置入口放在「小组件与通知」页,跳系统设置页。
