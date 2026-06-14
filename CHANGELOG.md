# Changelog

## 2026-06-14

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
