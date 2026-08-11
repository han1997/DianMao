# Changelog

- Redesigned the in-app icon treatment and launcher icon around a four-quadrant
  compass mark, with quieter icon containers for header actions, metadata chips,
  quadrant detail headers, and reminder/calendar controls.

- Added TickTick-style home smart views for four-quadrant browsing, today,
  upcoming, and unscheduled inbox tasks, with search over task and subtask
  titles.

- Fixed recurring tasks in the home time-focus lane so their displayed reminder
  date/time uses the current repeat cycle instead of the original stored date.

- Added a compact home time-focus lane that surfaces up to three unfinished
  overdue, today, or nearest upcoming timed tasks above the quadrant board.

- Improved capture-first workflow: quadrant detail pages now expose quick add,
  and saving a new task keeps the sheet open with confirmation feedback while
  resetting fields for the next capture.

- Extended the quiet task form polish to secondary controls: subtasks, date/time
  actions, repeat chips, and completion-note editors now share the same rounded
  low-noise input vocabulary.

- Refined quadrant detail and task form polish: quadrant detail rows no longer
  repeat the current quadrant tag, and add/edit controls now use a stronger
  capture field plus quieter quadrant, time, reminder, and repeat setting rows.

- TickTick-inspired visual polish: refine the warm-neutral theme, quieter home
  header, lighter quadrant board, list-like task rows, compact metadata chips,
  and more consistent sheet/detail/archive surfaces.

## 2026-06-15

- 修复二级页面编辑事项时返回行为：BackHandler 现在使用预计算的 backTarget 而非在回调中重复计算，确保返回时优先关闭编辑面板再回到二级页面，避免直接跳回主页面。

- 修复 Android 14+ 预测性返回手势（左滑返回）无法关闭新增/编辑事项面板和备份面板的问题；将 activity-compose 从 1.8.1 升级到 1.9.3，使 BackHandler 正确参与预测性返回调度链。

- 修复编辑/新增事项面板和备份面板打开时，按返回键会关闭底层页面而非面板的问题；现在返回键会先关闭最顶层的面板，再关闭象限详情，最后退出应用。

- 为开启日历同步的事项记录 Calendar Provider 事件 ID，编辑标题、时间或重复规则时更新既有系统日历事件，避免重复插入。
- 关闭事项日历同步或删除事项时，会尝试清理 DianMao 已托管的系统日历事件。
- 新增 Room 2→3 迁移，为任务表添加可空 `calendarEventId` 字段。
- 提醒通知发送前增加 Android 13+ 通知权限检查，避免无权限时触发 lint/运行时风险。
- 新增设备重启后的提醒恢复 receiver，重新调度未完成事项的未来本机提醒。
- 应用启动时会补偿已过期的重复提醒，将每天/每周/每月提醒推进到下一次未来时间后再调度。
- 新增重复提醒时间补偿的本地单元测试。
- 启用 Room schema 导出，提交当前数据库 schema，明确后续迁移基准。
- 新增 JSON 备份格式与本地文件导出/恢复入口，恢复时会清空不可跨设备复用的日历事件 ID。
- 新增 WebDAV 备份设置、手动上传当前备份和下载恢复能力。
- 收紧首页顶部留白，改为更轻量的标题区和统计状态带。
- 首页改为非滚动视口布局，四象限面板随剩余高度自适应。
- 压缩备份与恢复面板布局，常态下不需要滚动即可完成本地和 WebDAV 操作。
- 允许用户配置的局域网 HTTP WebDAV 地址，修复 Android 明文流量拦截导致的上传失败。
- 进一步压缩象限详情页、任务行和首页预览密度，减少大面积留白。
- 重构新增/编辑事项面板，表单内容可按需滚动，底部保存按钮固定显示。
- 更新 README 当前状态。

## 2026-06-14

- 为已添加事项新增编辑入口，可修改内容、象限、日期时间、日历同步、响铃和重复提醒。
- 为已添加事项新增删除入口和确认对话框，删除时同步取消本机提醒。
- 编辑事项时复用新增事项面板，保存后会按最新提醒设置重新调度 AlarmManager。
- 修复新增事项面板内点击层级，确保“事项内容”输入框可正常获得焦点和编辑。
- 新增重复提醒设置，支持不重复、每天、每周、每月。
- 为重复提醒新增 Room 字段、显式 TypeConverter 和 1→2 数据库迁移。
- 提醒广播触发后会按重复规则推进下一次时间并重新调度 AlarmManager。
- 系统日历写入和插入兜底会携带重复规则。
- 接入 Room 本地持久化，事项不再只保存在内存中。
- 新增任务数据层、Repository、DAO 与数据库初始化逻辑。
- 新增原生日期选择器和时间选择器，替换手写时间标签输入。
- 接入系统 Calendar Provider；无可写日历或权限不足时打开系统日历插入页兜底。
- 接入 AlarmManager 本机提醒、通知渠道、提醒广播接收器和 Android 13+ 通知权限请求。
- 更新新增事项面板的日历/提醒开关文案和禁用态，避免未设置时间时保存无效提醒。
- 更新 README 当前状态和剩余待办。

## 2026-06-13

- 初始化 git 仓库。
- 将 Android 模板工程改造为 Kotlin + Jetpack Compose + Material 3。
- 新增 DianMao 首屏原型：
  - 四象限任务筛选。
  - 今日关注摘要。
  - 快速新增事项面板。
  - 时间标签、系统日历意图、响铃意图。
  - 完成事项并记录完成备注。
- 新增 `PRODUCT.md` 和 `DESIGN.md`，记录产品方向和 seed 版设计方向。
- 新增 `README.md`，记录技术栈、当前状态、运行方式和开发约定。
- 将 Gradle wrapper 下载超时时间调整为 60 秒，降低首次同步失败概率。
- 将 Gradle/AGP 调整为本机已缓存组合，并指定 Android Studio JBR，避免首次构建自动下载 JDK。
- 移除 Gradle daemon 自动 toolchain 下载配置，使用本机 Android Studio JBR 构建。
- 启用 AndroidX 与 Jetifier，满足 Compose/AndroidX 依赖要求。
- 修正 Kotlin 包名中 `fun` 关键字的转义，匹配现有 Android namespace。
- 移除不必要的已弃用窗口颜色 API 调用，保留主题层系统栏配置。
