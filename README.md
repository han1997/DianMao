# DianMao

DianMao 是一款自用 Android 工作任务软件，目标是用一套统一的事项模型管理当天任务、长期任务、强时间性任务和完成后的心得。

## 产品方向

- 类似滴答清单的轻量任务体验，但不区分“任务”和“待办”。
- 所有事项都可以放入四象限：重要且紧急、重要不紧急、不重要但紧急、不重要不紧急。
- 有时间性的事项需要支持系统日历和响铃提醒。
- 完成事项时可以追加备注或心得，方便复盘。

## 技术栈

- Android Gradle Plugin
- Kotlin
- Jetpack Compose
- Material 3
- minSdk 31

## 当前状态

当前版本是可运行的 Compose 首屏原型，数据暂存在内存中。已经包含：

- 四象限筛选
- 快速新增事项
- 时间标签
- 系统日历/响铃意图开关
- 完成事项与完成备注
- 移动端安全区适配

尚未接入：

- Room 本地持久化
- 系统 Calendar Provider
- AlarmManager / 通知权限
- 真实日期时间选择器

## 开发约定

- 使用 Android Studio 打开并运行项目。
- 所有源文件保持 UTF-8 编码，避免中文乱码。
- 每次修改代码后追加 `CHANGELOG.md`。
- 有必要的功能说明同步补充到本 README。
- 每次修改完成后由 AI 助手自动创建本地 git commit。
- 提交推送由项目 owner 手动执行。

## 运行

在 Android Studio 中打开项目，等待 Gradle 同步后运行 `app` 配置。

也可以在命令行验证：

```powershell
.\gradlew.bat :app:assembleDebug
```
