# UI polish: secondary and new pages (二级页面、新增页面 UI 美化)

## Goal

参考主流同类软件（TickTick、Microsoft To Do、Todoist、Things 3、Google Tasks/Calendar），对 DianMao 的二级页面和新增/面板页面进行**结构性重排**级 UI 美化，使整体视觉与交互达到主流应用的精致度，同时保持 DianMao 现有设计语言（暖陶土色 + 安静中性面 + 低 alpha 象限色）。

## Requirements

### 1. TaskDetailScreen（事项详情页）— 方案 B「To Do 式 verb rows」中改

- Hero 区：完成 checkbox + 标题同行（To Do/Things 模式），象限 chip（12dp dot + shortTitle）在标题下方
- 元数据从 chips FlowRow 改为**全宽 icon rows**（TaskFormSection 内）：提醒/日期（Schedule）、系统日历（CalendarMonth）、响铃（Alarm）、重复（Repeat）— 未设置时显示动词（"添加提醒"），设置后显示值（verb-becomes-value）；日历行用 calendar blue（Time Has Its Own Voice Rule）
- 子任务：保留 bordered container，加 To Do 式计数行（"已完成 x/y"）作为 section subtitle
- 完成历史：改为底部折叠组（Google Tasks Completed-card 模式）— 折叠卡片头部带计数，150ms fade 展开；每条记录保留 Label Medium muted 时间戳
- 底部 action bar 保留（DESIGN.md L229 文档化例外）；可考虑将删除移入顶栏 overflow 降低底栏密度
- 顶栏保留（back + 标题 + edit）

### 2. QuadrantDetailScreen（象限详情页）— 方案 B「sticky 窄头 + 分节列表」中改

- 头部压缩为单行 sticky 头：back + 象限 dot + 标题 + 未完成计数；tint 保持 0.08 alpha；sticky 时仅用 quiet border（0.14-0.16）不用阴影
- 描述 + 3 个统计 dots 移到列表上方的非 tinted intro 块（缩小 8% tint 面积）
- 列表分节：「未完成」（默认展开）+「已完成」（默认折叠）— Label Medium 分节头（Todoist 模式）
- 已完成行沿用现有 completed 处理（surfaceVariant 0.38 alpha、border 0.12）
- 空状态保留现有卡片（0.07 alpha tint）

### 3. AddTaskPanel（新增/编辑面板）— 方案 A「sticky 捕获头 + 折叠详情体」

- **Sticky 头（不滚动）**：drag handle → 紧凑 header（标题 + close，副标题一行或去掉）→ 标题字段（**自动聚焦 + 键盘弹出**，仅 add 模式；1-3 行自适应，替代 min 4 行/118dp）→ **元数据 chips 行**：`[象限 chip w/ ColorDot] [日期 chip] [时间 chip] [日历 toggle-chip] [响铃 toggle-chip] [重复 chip]`，chip 显示当前值（未设置显示"未设置"），点击打开现有 picker；选中态 terracotta container，未选态 neutral + quiet border；依赖链（日历/响铃需日期，重复需日期+响铃）用 chip disabled 态表达
- **IME Done = 保存**（add 模式）：键盘完成键触发保存并继续记录
- **滚动体（默认折叠）**：「子事项」「备注/详情」各为可展开行（chevron，AnimatedVisibility 180ms fade+slide）；子事项展开后是现有 SubTaskEditor
- 保存按钮 pinned 在 sticky 头底部（full-width primary）
- 保留：90% sheet、capture-first save（保存后不关闭 + pill 2200ms）、编辑模式保存后关闭、象限跨保存保留
- 强调色收敛：emphasized title card 的 primaryContainer tint 缩小或去掉，聚焦指示线成为 accent 时刻

### 4. BackupPanel（备份面板）— 方案 A1「设置页式」

- 滚动设置页结构：
  1. 同步状态头卡（WebDAV host 从 URL 解析 + 连接状态 chip）
  2. 「本地备份」FormSettingBlock（导出/恢复两按钮，不变）
  3. 「WebDAV 配置」FormSettingBlock 纯表单：URL + 用户名 + 密码（**加可见性切换**）+ 尾部「保存设置」text/outlined 动作
  4. 「云端同步」FormSettingBlock：上传备份（filled primary）+ 下载恢复（outlined）
  5. 状态行 pinned 最后
- **全页面只有一个 filled primary 按钮**（上传备份；云端未配置时视觉主位让给导出本地）
- status: String? 签名与 string-match 展示逻辑保留（仅重设样式）

### 5. ArchivedPanel（归档面板）— 方案 B1「元数据丰富行」

- 行 = ColorDot + 全对比度标题（onSurface，替代 0.7 alpha）+ 元数据行（象限 label + due/archived 日期 + completionNote 预览，单行省略）+ 显式 Unarchive 按钮
- 头部加计数（"归档事项 · 12"）
- 去掉装饰性 drag handle（居中卡片不可拖动）
- 空状态保留（已符合主流配方）

### 6. 对话框 — 全按建议

- 三个对话框 dismiss 按钮从 OutlinedButton 改为 **TextButton**（对齐 M3 官方示例与 DESIGN.md 自身 Buttons 规范）
- CompletionNoteDialog 迁移为自由 **Dialog + Card**（官方：表单对话框用自由容器）
- DeleteTaskDialog 保持 error 色 confirm；文本保持点名对象（"确定删除"{title}""）
- UncompleteRecurringTaskDialog 保持 TextButton confirm（M3 默认，10% 规则最安全）

### 7. 全局约束（每个页面都适用）

- 10% 强调色规则：terracotta < ~10% 可见表面
- Quadrant Overlay Rule：象限色仅低 alpha tint（首页 4.5% / 详情头与空状态 8%）+ 12dp dots
- 静止无阴影：quiet border（outline 0.10-0.22 alpha）；阴影仅 sheet/menus/drag/详情底栏
- Motion：120-220ms、仅状态变化、FastOutSlowInEasing；无入场编排
- 字体：单系统 sans 家族，Title Large 详情标题、Body Large 笔记、Label Medium chips
- 浅色方案 only

## Acceptance Criteria

- [ ] 事项详情页：hero（checkbox+标题+象限 chip）+ verb rows 元数据 + 子任务 x/y 计数 + 折叠完成历史 + 底栏保留
- [ ] 象限详情页：单行 sticky 头（0.08 tint + quiet border）+ intro 块 + 未完成/已完成分节
- [ ] 新增面板：sticky 捕获头 + 自动聚焦键盘 + chips 行 + 折叠详情体 + IME Done 保存 + capture-first save 不变
- [ ] 备份面板：设置页式四段结构 + 单一 filled 主按钮 + 密码可见性切换
- [ ] 归档面板：元数据丰富行 + 头部计数 + 无装饰拖动条
- [ ] 对话框：TextButton dismiss ×3 + CompletionNoteDialog 自由容器
- [ ] 所有 DESIGN.md named rules 不被破坏
- [ ] `.\gradlew.bat :app:testDebugUnitTest --no-daemon` 通过
- [ ] `.\gradlew.bat :app:lintDebug --no-daemon` 通过
- [ ] `.\gradlew.bat :app:assembleDebug --no-daemon` 通过

## Definition of Done

- 上述三项 Gradle 检查全绿
- `git diff --check` 干净
- DESIGN.md 同步更新（新增 chips 行模式、sticky 头模式、折叠完成组模式、IME 保存行为、密码可见性切换等新注册的模式）
- 无业务逻辑/数据层变更（status: String? 签名保留、回调签名不变）

## Decision (ADR-lite)

**Context**: 用户要求参考主流软件对二级页面与新增页面做全面 UI 美化；深度确认为结构性重排（允许重排信息架构）。

**Decision**（2026-09-04 用户确认）:
- TaskDetailScreen → 方案 B（To Do 式 verb rows）
- QuadrantDetailScreen → 方案 B（sticky 窄头 + 分节列表）
- AddTaskPanel → 方案 A（sticky 捕获头 + chips 行 + 折叠详情体）+ 自动聚焦/紧凑标题 + IME Done 保存
- BackupPanel → 方案 A1（设置页式）
- ArchivedPanel → 方案 B1（元数据丰富行）
- 对话框 → 全按建议（TextButton dismiss + 自由 Dialog 表单）

**Consequences**: AddTaskPanel 改动最大（chips 行替代堆叠卡片、自动聚焦、IME 行为变化）；象限页 sticky 头需注意静止无阴影规则；BackupPanel 需保持 status string-match 契约；DESIGN.md 需注册新 motion/组件模式。

## Out of Scope

- 业务逻辑 / 数据层变更（Room、Repository、回调签名）
- 首页（一级页面）大改 — 仅当组件复用需要时顺带统一
- 深色模式
- 新功能（自然语言解析、批量操作、连接测试等）
- 重复事项历史面板 RecurringTaskHistoryPanel 的大改（仅做与新风格的一致性微调）

## Technical Notes

- 入口文件：`app/src/main/java/fun/han1997/dianmao/MainActivity.kt`（单文件 UI，约 4853 行）
  - TaskDetailScreen: L4519-4853
  - QuadrantDetailHeader/EmptyState: L1993-2116；象限列表组装 L1052-1096
  - AddTaskPanel: L3021-3413
  - BackupPanel: L3416-3674
  - ArchivedPanel/ArchivedTaskItem: L3677-3844
  - 对话框: DeleteTaskDialog L4009 / UncompleteRecurringTaskDialog L4044 / CompletionNoteDialog L4132
  - 复用组件: IconTile L665、HeaderActionButton L691、TaskFormSection L2670、FormSettingBlock L2736、SubTaskEditor L2830、ToggleRow L4079、quietBorder L605、quietTextFieldColors L729
- BackupPanel status string-match 在 L3620-3633 — 重设计必须保留 `status: String?` 契约
- 自动聚焦用 `FocusRequester` + `LaunchedEffect(visible)` gated `!isEditing`
- 依赖链语义：日历/响铃 enabled 需日期；重复 enabled 需日期+响铃 — chips 行用 disabled 态表达

## Research References

- [`research/task-detail-page-patterns.md`](research/task-detail-page-patterns.md) — 详情页范式与 3 方案
- [`research/add-task-capture-patterns.md`](research/add-task-capture-patterns.md) — 捕获流共识与 3 方案
- [`research/settings-dialog-patterns.md`](research/settings-dialog-patterns.md) — 设置/归档/对话框范式与方案
