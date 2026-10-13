# 复盘窗口常量收敛 + 边界测试

## 背景

任务 `10-13-dismissible-time-focus-lane-upcoming-window-parity` 修复了首页 lane 与「近期」智能视图的窗口漂移,并在 `compose-patterns.md` 记下一条常见错误:
**窗口断言如果只是复述实现里的数字,它就是变更探测器而不是契约**。

顺着这条线索排查同一类问题,发现复盘页(`buildReviewStats`)有一份同构的隐患:

| 位置 | 代码 | 作用 |
|---|---|---|
| `ReviewModels.kt:75` | `today.minusDays(6)` | 「近 7 天」计数下界 |
| `ReviewModels.kt:76` | `today.minusDays(29)` | 「近 30 天」计数/四象限占比下界 |
| `ReviewModels.kt:81` | `(6 downTo 0)` | 每日分布柱状图根数 |
| `ReviewScreen.kt:78,90` | 文案「近 7 天」×2 | 用户可见的「7」 |

`6`、`29`、`6` 三处裸字面量彼此隐式耦合:把 `minusDays(6)` 改成 `minusDays(5)`,头部计数仍标称「近 7 天」、柱状图仍是 7 根,但两者覆盖的区间已错开一天 —— 与刚修复的 lane 缺陷完全同类。

**当前行为是正确的**(三处字面量一致),属**预防性加固**,不改变任何用户可见行为。

现有测试挡不住:`ReviewModelsTest.statsCountWithinLocalDayWindows` 用的数据(今天 / 昨天 / 10 天前)落在任何一天边界的哪一侧都不变,把 `minusDays(6)` 改成 `minusDays(5)` 后断言仍然全绿。

## 目标

1. 把窗口天数收敛为具名常量,让三处逻辑共用同一来源,消除再次漂移的可能。
2. 补边界断言,使「7」「30」这两个数字被测试锁住,而不是被测试复述。

## 已确认决策

| 决策点 | 选择 | 理由 |
|---|---|---|
| 收敛范围 | **只收敛逻辑层(常量 + 边界测试),UI 文案不动** | 用户 2026-10-13 选择。文案改动面更大且涉及像素对齐基准,本任务限在逻辑层 |
| 是否改变行为 | **否** | 当前三处字面量一致,纯重构 + 补测试 |
| 常量命名 | `REVIEW_WINDOW_DAYS` / `REVIEW_MONTH_DAYS` | 与 `HomeModels.kt` 的 `FOCUS_LANE_UPCOMING_DAYS` 风格一致 |

## 范围

**A. 逻辑层(`app/src/main/java/fun/han1997/dianmao/ui/logic/ReviewModels.kt`)**

1. 新增文件内 `private const val REVIEW_WINDOW_DAYS = 7L` 与 `REVIEW_MONTH_DAYS = 30L`。
2. `day7Start` → `today.minusDays(REVIEW_WINDOW_DAYS - 1)`;`day30Start` → `today.minusDays(REVIEW_MONTH_DAYS - 1)`。
3. `dailyCounts` 的 `(6 downTo 0)` → `((REVIEW_WINDOW_DAYS - 1) downTo 0)`(`downTo` 需要 `Long`/`Int` 匹配,用 `Long` 循环变量或显式转换)。
4. 补注释说明「近 N 天」= 含今天在内的 N 个本地日,与该常量的关系。

**B. 测试(`app/src/test/java/fun/han1997/dianmao/ReviewModelsTest.kt`)**

5. 新增 `statsWindowBoundariesAreInclusiveOfTheOldestDay`:
   - 第 7 天当天 0 点后的完成事件**计入** `last7DaysCount`;
   - 第 8 天(即 `minusDays(7)`)的事件**排除**;
   - 第 30 天当天**计入** `last30DaysCount`,第 31 天**排除**;
   - `dailyCounts.size == 7`,且首根柱子的 `date == today.minusDays(6)`。
6. 新增 `dailyDistributionCoversTheSameWindowAsTheHeadlineCount`:断言 `dailyCounts.sumOf { it.count } == events.count { 落在近 7 天窗口内 }`,把「两张嘴说同一个 7」锁成不变量。
7. 保留既有 `statsCountWithinLocalDayWindows`、`emptyEventsProduceZeroedStats`。

## 非目标

- **不修改** `ReviewScreen.kt` 的「近 7 天」/「近 30 天」UI 文案(用户选定范围外)。
- 不改动任何用户可见行为、布局、像素对齐基准。
- 不涉及原型(`design/mockup/`)—— 纯逻辑重构,无 UI 改动,`prototype-first.md` 不适用。
- 不扩展到其他窗口(如 `CalendarMonthModels` 的网格起始偏移)—— 那是日历月本身的结构,非「近 N 天」语义。

## 验收标准

- [ ] `ReviewModels.kt` 中不再出现裸字面量 `6` / `29` 表示窗口天数。
- [ ] `:app:testDebugUnitTest` 通过,`ReviewModelsTest` 新增两条断言用例,且**把它们要防的回归真正防住**:手动把 `REVIEW_WINDOW_DAYS` 改成 `6` 或 `8`,新用例必须失败。
- [ ] `:app:lintDebug`、`:app:assembleDebug`、`git diff --check` 通过。
- [ ] 行为不变:既有 `statsCountWithinLocalDayWindows` / `emptyEventsProduceZeroedStats` 无需修改即通过。

## 技术说明

- 权威常量风格参考:`HomeModels.kt` 的 `FOCUS_LANE_UPCOMING_DAYS` / `UPCOMING_SMART_VIEW_DAYS` / `FOCUS_LANE_LIMIT`。
- 相关常见错误条目:`.trellis/spec/backend/compose-patterns.md` 的「Sibling Views Silently Disagreeing on a Window」。
- `ReviewScreen.kt` 的两处文案保持字面量,属**已知且已接受**的重复(记入 spec),若日后要改窗口天数需同步这两处。