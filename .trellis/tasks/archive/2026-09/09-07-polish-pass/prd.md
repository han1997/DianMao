# PRD: Polish Pass — 缺失功能补全 + UI 打磨

## 背景

DianMao 完成度已高（四象限 + 智能视图 + 提醒/日历/重复 + 撤销 + 备份 + 暗色主题）。本次是针对分析中发现的缺口做一次集中补全和打磨。用户确认范围：A1–A4 + B 全部，外加修复一个孤儿功能入口。

## 需求列表（按 commit 批次）

### 批次 1 通知深链（A1 + B5）
- TaskReminderReceiver 的提醒通知加 contentIntent：点击通知打开 MainActivity 并定位到对应任务的详情页（intent extra extra_task_id）。
- MainActivity 在 onCreate（冷启动）和 onNewIntent（热启动）读取 taskId，找到任务后打开 taskForDetail；任务不存在（已删除）则静默忽略。
- 通知不添加操作按钮（用户选择：仅点击跳转）。
- 新增专用单色通知图标 ic_notification（白色透明底 drawable），替换现在直接复用的 ic_launcher_foreground。
- 同一 taskId 已在详情页时不重复处理。

### 批次 2 表单快捷日期 chips（A2）
- AddTaskPanel 的"时间与提醒"区，在"到期日"行下方加三个快捷 chips：今天 / 明天 / 下周一。
- 点 chip 直接设置 dueDateMillis（当天 0 点，local-midnight 约定），日期选择器行为不变。
- 设置日期后若未选时间，提醒时间默认 09:00（复用日期选择器现行为）。
- 选中的 chip 高亮（terracotta container），与日期字段联动；手动改日期后 chips 选中态跟随实际值。
- "下周一"计算基于今天所在周的下一周周一。

### 批次 3 任务描述字段（A3）
- TaskEntity 新增 detail: String?（事前备注），DB v10 → v11 迁移（ALTER TABLE tasks ADD COLUMN detail TEXT）。
- TaskFormState、TaskUiModel、toUiModel() 同步加字段。
- AddTaskPanel：标题字段下方加"补充说明"多行可选输入，编辑模式回填。
- TaskDetailScreen：象限 chip 与"事项信息"区之间展示描述（有值才显示）。
- 备份兼容：taskToJson 输出 detail，taskFromJson 用 nullableString 读取（旧备份缺字段回退 null）；BACKUP_VERSION 按 codec 现有 bump 惯例决定。
- 单测：覆盖 detail 的 normalize/round-trip。

### 批次 4 智能视图分组 + 列表动画（A4 + B2）
- "今天"视图分两组：过期（dueAt < 今天0点）与 今天，各组带 Label Medium 组头（含计数），复用 quadrant-detail section label 风格。
- "近期"视图按天分组，组头显示相对日期（如"明天 · 周四"），含计数。
- 组头是普通 item，不引入 sticky header。
- HomeSmartTaskList 的 items 加 animateItem()，完成/删除时行平滑收合（默认 easing，无弹簧）。
- 分组逻辑放 HomeModels.kt（纯函数），补单元测试。

### 批次 5 触感 + 静默完成 + 空态 CTA（B1 + B3 + B4）
- 全局触感（LocalHapticFeedback）：滑动越过触发阈值 LongPress；长按拖拽开始 LongPress；勾选完成 LongPress。
- 右滑完成改为静默完成：不弹心得对话框；完成后走统一顶部通知（TopNoticeHost/Snackbar）"已完成"，附"写心得"action 打开 CompletionNoteDialog 补记。周期任务右滑取消完成仍走现有确认对话框。所有反馈统一走顶部通知（用户明确要求）。
- 智能视图空态加"新增事项"tonal 按钮（今天/近期/收集箱/搜索无结果），四象限空态保持现状。
- 勾选框路径（TaskRow/TaskDetail）仍弹心得对话框，主路径不变。

### 批次 6 孤儿入口修复
- RecurringTaskHistoryPanel 目前无入口（taskForHistory 从未被赋值）。
- TaskDetailScreen 的"已完成 N 次"meta 行改为可点击，打开该 panel。

## 非目标
- 不做 WebDAV 自动备份、每日过期摘要通知。
- 不改勾选路径的心得对话框行为。
- 不引入新依赖。

## 验证
- gradlew :app:testDebugUnitTest 全绿（含新增分组测试）。
- gradlew :app:assembleDebug 编译通过。
- 每批次独立 commit，遵守现有 commit 风格（feat: / ui: / fix:）。