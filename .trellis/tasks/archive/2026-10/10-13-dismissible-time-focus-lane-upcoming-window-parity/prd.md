# PRD: 时间关注可移除 + 近期窗口对齐

来源:用户反馈(2026-10-13)两点——「时间关注」无法移除;「时间关注」里有「近期」但「近期」页里没有对应事项。
完整实现计划见 `C:\Users\hanhu\.claude\plans\velvet-orbiting-harbor.md`(已获用户批准)。

## 背景

### 1. 「时间关注」lane 没有任何移除入口

首页四象限视图的「时间关注」卡片(`ui/screens/HomeScreen.kt:236-309` `TimeFocusLane`)只在有过期/今天/近期事项时整体显示(`AnimatedVisibility(visible = focusItems.isNotEmpty())`),没有关闭按钮,用户无法收起。用户要求:可以移除,但**第一次打开必须显示**。

### 2. 「时间关注」与「近期」页对「近期」的定义不一致(真实缺陷)

| | 原型(权威基准) | 当前实现 |
|---|---|---|
| lane 的「近期」 | 7 天内,取前 **4** 条(`design/mockup/app.js:105,110`) | **无上限**,取前 **3** 条(`ui/logic/HomeModels.kt:133-137,125`) |
| 「近期」智能视图 | **14** 天内(`design/mockup/app.js:277`) | **7** 天内(`ui/logic/HomeModels.kt:161,172-174`) |

后果:10 天后到期的任务在 lane 被打上「近期」标签,但「近期」页因 7 天窗口静默丢弃。测试 `TimeFocusItemsTest.kt:151` 还把 7 天窗口固化为期望值。三处均偏离已确认原型。

## 目标

1. 给「时间关注」lane 增加**本次启动隐藏、重启恢复**的移除能力(不持久化)。
2. 按原型对齐两处「近期」窗口:lane 收 7 天内取前 4 条;「近期」页收 14 天内。使「卡片里标近期的事项一定能在近期页找到」。

## 已确认决策(2026-10-13,用户问询)

| 决策点 | 选择 |
|---|---|
| 移除后行为 | 本次启动隐藏,重启恢复(session-only,`rememberSaveable`;不持久化、不加设置项) |
| 近期窗口 | 按原型对齐:lane 7 天内/前 4 条;近期页 14 天内 |
| 过期/今天边界 | 保持实现现状(按具体时刻判定);改原型 `timeFocus()` 的 `sameDay()` 对齐实现 |
| 完全空库首启 | 不显示卡片(与原型 `quadrantsView()` 的空态早返回一致);「首次必须显示」由「移除不持久化」保证 |

## 范围

### A. 原型侧(先改,prototype-first 契约)

- `design/mockup/app.js`:`state.focusLaneDismissed` session 状态;`quadrantsView()` 的 `.lane-head` 加关闭按钮(count `data-action="focus-dismiss"`),dismissed 时跳过 lane;`timeFocus()` 的过期/今天改按时刻判定;upcoming 上限 7 天。
- `design/mockup/style.css`:仅按需补最小间距规则。
- `design/mockup/smoke-test.cjs`:新增关闭/恢复、行数 ≤4、chip 文案断言。
- `design/mockup/BASELINE.md`:v1.8 变更记录 + 新 SHA-256。

### B. 实现侧

- `ui/logic/HomeModels.kt`:`buildTimeFocusItems` 加 7 天上限、默认 limit 3→4;`buildHomeSmartTaskItems` 的 Upcoming 7→14 天;窗口天数提为文件内常量。
- `ui/screens/HomeScreen.kt`:`TimeFocusLane` 加 `dismissed`/`onDismiss` 参数与关闭按钮(`DianMaoIcons.Close`)。
- `MainActivity.kt`:`timeFocusDismissed` rememberSaveable 状态并传入。
- `TimeFocusItemsTest.kt`:改写 7 天用例为 14 天并补边界;新增 lane 7 天上限与默认 limit=4 用例。
- `.trellis/spec/backend/compose-patterns.md`:更新 lane 契约(4 条、7 天上限、关闭态)。
- `CHANGELOG.md` + `bumpVersion`。

## 非目标

- 不做「恢复到设置页」的入口(决策为 session-only,重启即恢复)。
- 不改过期/今天的时刻判定语义(维持实现,改原型)。
- 不改 lane 的其他视觉(RowShape/quietBorder/CountLabel 均不动)。

## 验收标准

- [ ] 首页「时间关注」lane 头部有关闭按钮;点击后本会话内不再显示;进程重启后恢复。
- [ ] lane 只收 7 天内到期的 open 任务,最多 4 条;超出 7 天的不出现。
- [ ] 「近期」智能视图收 14 天内(明天起)任务;lane 中标「近期」的事项必能在该视图找到。
- [ ] 原型 smoke test 通过(含新增断言);BASELINE.md 记录 v1.8 与新 SHA-256。
- [ ] `:app:testDebugUnitTest` + `:app:lintDebug` + `:app:assembleDebug` + `git diff --check` 全绿。
- [ ] 按用户指令不使用模拟器;设备侧视觉并排登记为残余风险。