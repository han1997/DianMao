# 打磨软件 UI 和操作逻辑（第二轮）

## Goal

继续打磨 DianMao 的 UI 和操作逻辑。上一轮（08-11）已覆盖首页/列表浏览路径（AppHeader、HomeSmartControls、TimeFocusLane、QuadrantBoard、TaskRow）。本轮聚焦尚未深入打磨的四个区域：事项详情页、新增/编辑面板、象限详情页、二级面板与对话框。目标是让这些路径在视觉层级、操作反馈和一致性上达到与首页相同的水准。

## What I already know

* 项目是单模块 Android Compose 应用，所有 UI 在 `MainActivity.kt`（4485 行）
* 上一轮已打磨：AppHeader、HomeSmartControls、TimeFocusLane、QuadrantBoard、QuadrantPane、CompactQuadrantTask、TaskRow
* 设计系统（DESIGN.md）：warm neutral、terracotta 主强调、calendar blue 仅用于时间语义、低阴影 Material 3、quiet border、capture-first
* 已有可复用组件：`TaskFormSection`、`FormSettingBlock`、`IconTile`、`CompactMeta`、`quietBorder`、`quietTextFieldColors`、`HeaderActionButton`、`InlineButtonIcon`、`SummaryPill`、`InlineStatDot`
* 形状常量：`PanelShape`(18dp)、`SheetShape`(22dp top)、`RowShape`(12dp)、`ChipShape`(8dp)、`IconTileShape`(14dp)
* spec 要求：`:app:testDebugUnitTest`、`:app:lintDebug`、`:app:assembleDebug`、`git diff --check` 通过
* 不改变数据模型、提醒调度、日历写入、备份格式

## Requirements

### 1. TaskDetailScreen（事项详情页）

* **顶栏**：当前是裸 Row，改为有视觉锚点的结构——使用 surface 背景 + 合理 padding，返回按钮和编辑按钮使用 HeaderActionButton 保持一致性
* **元信息区**：当前 CompactMeta 散落在 Column + FlowRow 中，收拢进一个 TaskFormSection 或 Surface 容器，让元信息有明确边界
* **完成心得**：当前是独立 Column + Surface，改为使用 TaskFormSection 包裹，与表单视觉语言一致
* **统计区**：当前用 headlineMedium 数字 + label，偏 dashboard 感；改为更安静的 InlineStatDot 或 SummaryPill 风格，降低数字字号，让信息服务于扫读而非展示
* **完成历史空状态**：当前居中大图标，改为更安静的 inline 提示，与 QuadrantDetailEmptyState 风格对齐
* **底部操作栏**：当前"完成"用 FilledTonalButton（secondary 色调），主操作应该用 terracotta 主强调；删除按钮是 IconButton，保持但视觉层级要低于主操作；归档按钮保持 secondary
* **"打开系统日历提醒"按钮**：当前全宽 FilledTonalButton 偏重，改为 OutlinedButton 或更轻量的行内操作，降低视觉权重
* **段落间距**：当前 LazyColumn 全部 spacedBy(14.dp)，改为按内容分组，主段落间距更大，段落内更紧凑

### 2. AddTaskPanel（新增/编辑面板）

* **表单间距**：当前 spacedBy(9.dp) 偏紧，调整为 11-12dp 让段落呼吸
* **子事项空状态**："暂无子事项" 当前是裸 Surface + Text，改为更轻量的提示文字或直接隐藏
* **面板头部**：当前 IconTile + 标题/副标题 + 关闭按钮结构合理，微调间距和字号层级
* **保存反馈**：当前 saveFeedback pill 结构合理，保持
* **DateTimePickerRow / ToggleRow / RepeatRulePicker**：已使用 FormSettingBlock，视觉一致，保持

### 3. QuadrantDetailHeader / QuadrantDetailEmptyState（象限详情页）

* **头部返回按钮**：当前是裸 IconButton，改用 HeaderActionButton 保持与全应用一致
* **头部统计**：InlineStatDot 当前 6dp 圆点 + labelMedium 文字，可微调间距和颜色对比
* **空状态**：当前结构合理（IconTile + 标题 + 提示），微调文案和间距

### 4. BackupPanel / ArchivedPanel / Dialogs（二级面板与对话框）

* **ArchivedPanel 一致性**：
  * 当前用 PanelShape(18dp) + tonalElevation，BackupPanel 和 AddTaskPanel 用 SheetShape(22dp top) + Card 6dp elevation——统一为 SheetShape + Card 6dp
  * 当前无 drag handle，AddTaskPanel 和 BackupPanel 有——补上 38×4dp drag handle
  * ArchivedTaskItem 当前只有标题 + 完成备注 + 取消归档按钮，补充象限色点让归档事项保留来源语义
* **BackupPanel 状态反馈**：当前状态栏始终用 CloudUpload 图标，改为根据状态类型（上传/下载/导出/恢复/错误）切换图标
* **DeleteTaskDialog**：确认按钮当前是 TextButton + error 色，破坏性操作应更明确——保持 TextButton 但确保 error 色对比足够
* **UncompleteRecurringTaskDialog**：当前用 Alarm 图标，语义不符——改为 Replay 或 Undo 图标
* **CompletionNoteDialog**：结构合理，保持

## Acceptance Criteria

* [ ] TaskDetailScreen 顶栏、元信息区、完成心得、统计区、底部操作栏视觉层级更清晰
* [ ] TaskDetailScreen 底部主操作使用 terracotta 主强调色
* [ ] AddTaskPanel 表单段落间距更舒适
* [ ] QuadrantDetailHeader 返回按钮使用 HeaderActionButton
* [ ] ArchivedPanel 与 BackupPanel/AddTaskPanel 的 sheet 形状、elevation、drag handle 一致
* [ ] ArchivedTaskItem 包含象限色点
* [ ] BackupPanel 状态反馈图标根据操作类型切换
* [ ] UncompleteRecurringTaskDialog 图标语义正确
* [ ] Calendar blue 仍只用于时间/日历语义，terracotta 仍只用于主强调
* [ ] 不改变任务数据模型、提醒、日历、重复规则、备份或导航底层契约
* [ ] 更新 CHANGELOG.md
* [ ] `:app:testDebugUnitTest`、`:app:lintDebug`、`:app:assembleDebug`、`git diff --check` 通过

## Definition of Done

* 改动聚焦已确认的 UI/交互范围
* 不新增外部依赖
* 不误提交任务外既有脏文件（.idea/misc.xml、.trellis/.template-hashes.json、.impeccable/、.opencode/）
* 如形成新的可复用 UI 或操作逻辑规则，更新 `.trellis/spec/backend/compose-patterns.md`

## Technical Approach

* 在 `MainActivity.kt` 内做聚焦的 Compose 调整，优先复用已有组件
* TaskDetailScreen：用 TaskFormSection/Surface 包裹元信息和完成心得；统计区改用 InlineStatDot FlowRow；底部操作栏主操作改用 Button + primary 色
* AddTaskPanel：调整 spacedBy 和子事项空状态
* QuadrantDetailHeader：返回按钮改用 HeaderActionButton
* ArchivedPanel：改用 Card + SheetShape + 6dp elevation + drag handle；ArchivedTaskItem 加 ColorDot
* BackupPanel：状态栏图标根据 status 文本内容切换
* Dialogs：UncompleteRecurringTaskDialog 图标改为 Replay

## Out of Scope

* 新字段、标签、清单、附件、模板、自然语言解析、AI 创建任务
* 数据库 schema、备份格式、WebDAV 协议、Calendar Provider 写入语义、AlarmManager 调度语义
* 深色主题重设计
* 大规模拆分 MainActivity.kt
* 首页/列表浏览路径的再次打磨（上一轮已完成）

## Technical Notes

* 目标文件：`app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
* 相关 spec：
  * `.trellis/spec/backend/index.md`
  * `.trellis/spec/backend/compose-patterns.md`
  * `.trellis/spec/backend/quality-guidelines.md`
* 设计参考：`DESIGN.md`（颜色、排版、elevation、motion 规则）
* 已有可复用组件：TaskFormSection、FormSettingBlock、IconTile、CompactMeta、quietBorder、quietTextFieldColors、HeaderActionButton、InlineButtonIcon、SummaryPill、InlineStatDot、ColorDot
