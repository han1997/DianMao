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
- Room
- Calendar Provider / AlarmManager
- WebDAV
- minSdk 31

## 当前状态

当前版本是可运行的 Compose 首屏原型，数据通过 Room 保存在本机。已经包含：

- 四象限筛选
- 快速新增事项
- 原生日期/时间选择器
- 每天、每周、每月、每年、自定义间隔（每 N 周期）与指定周几（工作日预设）的重复规则；重复只需设置到期日，日历响铃与到点提醒独立控制各自的同步/重排
- 系统 Calendar Provider 写入；无可写日历或权限不足时打开系统日历插入页兜底
- 已记录 Calendar Provider 事件 ID，编辑事项时更新既有日历事件，关闭日历同步或删除事项时清理已托管事件
- AlarmManager 本机提醒与 Android 13+ 通知权限请求
- 设备重启或重新打开应用后恢复未来提醒，重复提醒会批量补偿到下一次未来时间
- Room schema 导出与显式迁移链
- JSON 本地备份导出/恢复
- WebDAV 手动上传备份与下载恢复，支持局域网 HTTP 地址；公网备份建议使用 HTTPS
- 已添加事项的编辑与删除
- 完成事项与完成备注
- 移动端安全区适配
- 备份/恢复、归档、新增/编辑事项与重复历史以全屏次级页面呈现,与主页同一视觉体系

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
