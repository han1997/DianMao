# PRD: 静默完成记录重复历史(+死代码清理、反馈/一致性小修)

来源:2026-10-15 全仓代码债审计(第二次,第一次因 API 限流中断后重跑)。

## 已确认缺陷

### S1(高)静默完成对重复事项不记历史、不计次数

- `TaskRepository.completeSilently`(swipe 手势 + 小组件勾选的共同路径)→ `completedEntity`:
  - `isCycleBased()` 分支只推进到期日/置 `completed`,**从不 `completionDao.insert`,也从不 `completionCount+1`**;
  - `isCountBased` 分支递增 `completionCount` 但也不写历史;
  - 只有对话框路径 `saveCompletionNote`(TaskRepository.kt:295)为所有 `repeatRule != None` 写
    `TaskCompletionEntity`,并为所有周期型递增 `completionCount`。
- 后果:用最常见的滑动/勾选完成一个每日/每周任务后,
  - 复盘页(`buildReviewCompletionEvents` 只读 `task_completions`)不再统计该完成;
  - 详情页"已完成 N 次"/"总完成次数"停留在旧值(TaskDetailScreen);
  - 与对话框路径行为不一致。
- 归因:`completeSilently` 文档声称"非重复不写历史(非重复被归档,复盘只统计重复)",
  而"重复应写历史"这一半从未实现。

### S2(中)`toggleCompleted(true)` 分支死代码

- `toggleCompleted`(TaskRepository.kt:160-217)的 `completed=true` 状态机(165-205)全 app 无调用点
  (仅 `toggleCompleted(x,false)` 用于取消完成);真正完成都走 `saveCompletionNote`。
- 这些不可达分支与 `completedEntity` 重复且逻辑略异,是维护陷阱。

### S3(低)错误提示按子串"失败"着色,部分真实失败显示为成功色

- `TopNoticeHost`(MainActivity.kt:1629)`contains("失败")` 决定暖色;而移动任务的副作用失败串
  `"系统日历未同步"` / `"提醒未重排"`(MainActivity.kt:691,699)不含"失败",
  拼进消息后渲染为成功色。

### S4(低)(上一轮引入)恢复失败全角/半角冒号不一致

- MainActivity.kt:433 全角"恢复失败:",633 半角"恢复失败:"。统一为全角。

## 文档分歧(需用户决策)

### S5 更新设计文档以匹配已实现行为

- `DESIGN.md §5.5` 描述新增保存后继续记录，但 `AddTaskPanel` add/edit 成功后都会调用 `onDismiss()`，代码注释明确是单次捕获。
- 决策：采用低风险切面，只更新 `DESIGN.md` 描述为新增/编辑保存成功后关闭；不改变用户可见保存行为、不做原型重构。
## 非目标

- 不重构 MainActivity 状态枢纽 / BackHandler。
- 不加新测试依赖。
- S5 若选"改实现"则不在此任务内做(需原型 + 另开任务)。

## 验收标准

- [x] S1: 静默完成重复任务写入一条无心得完成历史并递增 `completionCount`；完成后补心得不重复计数。
- [x] S1: 增加 Android instrumentation 回归测试覆盖每日任务静默完成与后续心得。
- [x] S2: 移除不可达的 `toggleCompleted(true)` 路径；保留两个取消完成调用点。
- [x] S3 / S4: 失败通知着色涵盖日历/提醒同步副作用，恢复失败文案统一全角冒号。
- [x] S5: `DESIGN.md` 与单次保存即关闭的实现一致。
- [ ] 验证 `:app:assembleDebug`、`:app:lintDebug`、`:app:testDebugUnitTest`、instrumentation 回归测试和 `git diff --check`。
- [x] 更新 `CHANGELOG.md`。
