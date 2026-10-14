# PRD: 查缺补漏 — 多提前量重复提醒 + 恢复安全 + 测试盲区

来源:2026-10-14 全仓功能盘点(读代码 + 测试覆盖审计)。

## 背景

DianMao 维护规范,但盘点出若干真实缺陷与覆盖盲区。以下按严重度排序,均已在源码中定位。

## 已确认缺陷

### D1(高)多提前量 + 重复任务会一次到期推进多个周期

- `alerts/TaskReminderReceiver.kt:80-85`:提醒广播触发后无条件调用
  `repository.advanceRepeatAfterReminder(taskId)` 并 `scheduler.schedule(nextTask)`。
- 任务若设置多个提前量(如 准时 / 提前30分 / 提前1天),每个提前量各有一个闹钟;
  逐个触发时会**各自推进一次周期**,导致到期日一次跳 N 个周期,并把后置提前量
  挂到错误的到期日上。
- 单提前量(`reminderLeadMinutes.size == 1`)不受影响,所以长期未被发现。
- 相关:`ReminderScheduler.schedule` 对 `triggerAt <= now` 的提前量静默跳过;
  推进后旧周期尚未触发的提前量闹钟未被清理。

### D2(中)本地恢复缺少「恢复前快照」,且两条恢复路径行为不一致

- WebDAV 恢复在破坏性导入前会先上传 `dianmao-backup-before-restore.json`
  快照,失败即中止(`MainActivity.kt:1341-1353`,`CHANGELOG v1.1.0` 明确记录为安全要求)。
- 本地文件恢复(`MainActivity.kt:399-417`)直接 `importBackupJson`,**没有快照**,
  也没有任何确认弹层。同样是清空 `tasks` + `task_completions` 的破坏性操作。
- 结论:同一风险操作两条路径安全等级不同。

### D3(中)破坏性恢复无二次确认

- 本地恢复与 WebDAV 恢复按钮均无确认弹层(`BackupPanel.kt:228-241`、`320-338`),
  单击即清空全部本地事项与完成历史。与删除/批量删除/取消重复完成等已有确认弹层的
  交互惯例不一致。

### D4(中)备份编解码零测试

- `data/TaskBackupCodec.kt`(版本 7,手写 JSON schema)无任何测试:无 round-trip、
  无版本兼容、无字段增删回归。字段改名或版本号变更会静默破坏用户恢复。
- `TaskConverters.kt` 的 `toQuadrantType` / `toRepeatRule` 用 `valueOf`,遇到未知值直接抛异常,
  会让整表读取崩溃;`toSubTasks` 的 `getString("title")` 缺键即抛。均无测试。
- 注:规范(`quality-guidelines.md`)要求「不要写依赖 `org.json` 的本地 JVM 测试」,
  需先确认是否引入 Robolectric,或把这些纯函数抽出为不依赖 org.json 的形状再测。

### D5(低)通知权限请求时机

- `MainActivity.kt:1155-1166`:保存事项时,若申请 `POST_NOTIFICATIONS` 权限的
  `launch(...)` 与 `scheduler.schedule(saved)` 在同一个 try 内,申请是异步的;
  首条提醒可能因权限尚未授予而未展示。属交互时序问题,非数据损坏。

## 非目标

- 不重构 `MainActivity` 状态枢纽;不动 `resolveInAppBackTarget`/BackHandler。
- 不引入 Robolectric / MockK;D4 通过「抽纯函数 + 现有 junit」达成。
- 不修 D5(通知权限申请时序):本次范围不含,记录备查。

## 已确认范围(2026-10-14,用户多选)

- **D1** 多提前量重复提醒多次推进 — 修 + 回归测试。
- **D2** 本地恢复补「恢复前快照」。
- **D3** 恢复加二次确认弹层。
- **D4** 备份 codec / 转换器补测试,方式=改写为可测纯函数,不新增依赖。

## 实现要点

### D1
- 推进周期的判定从「广播触发」改为按到期时刻幂等 / 「本周期内已推进」标记,
  避免同一到期日被多个提前量闹钟各推进一次。
- 需抽一个纯函数承载判定,便于 JVM 单测。

### D2
- 本地恢复复用 WebDAV 的「导出当前 → 写快照 → 再导入」顺序;快照写到哪里需定
  (本地恢复场景没有 WebDAV 目录,考虑写入 app 私有目录)。
- 保留「快照失败即中止」。

### D3
- 复用现有确认弹层形状(`QuietDialogContainer` + `BatchDeleteConfirmDialog`)。
- 原型优先契约:确认是否需先在 `design/mockup/` 补该确认态并经用户确认。

### D4
- 将 `TaskBackupCodec` 的 JSON 拼装/解析与 `TaskConverters` 的枚举/子任务解析
  抽出为不依赖 `org.json`/Android 的纯函数,用现有 junit 覆盖 round-trip、
  未知枚举值、缺键子任务等边界。

## 验收标准

- [x] D1:多提前量重复任务一次到期只推进一个周期;回归测试覆盖(`RepeatReminderAdvanceGuardTest`)。
- [x] D2:本地恢复与 WebDAV 恢复采用一致的「快照失败即中止」流程(本地写公共下载目录)。
- [x] D3:恢复前有确认弹层,文案与既有弹层一致(原型新增第 9 个对话框,BASELINE v1.8)。
- [x] D4:备份 codec + 转换器有可运行的 JVM 回归测试,不新增依赖(`BackupCodecRulesTest`)。
- [x] `:app:assembleDebug` + `:app:lintDebug` + `:app:testDebugUnitTest` + `git diff --check` 全绿。
- [x] 原型冒烟测试通过。
- [x] `CHANGELOG.md` 记录本次修复(v1.2.7)。

## 完成记录(2026-10-14)

版本 v1.2.7(versionCode 21)。改动:
- `alerts/ReminderScheduler`、`TaskReminderReceiver`、`TaskActionReceiver`、`data/TaskRepository`、
  `data/TaskModels`(新增 `shouldAdvanceRepeatAfterReminder`)。
- `ui/logic/PlatformHelpers`(新增 `writeLocalRestoreSnapshot`)、`backup/WebDavUrl`(新增本地快照常量)、
  `MainActivity`(本地/WebDAV 恢复改为先确认后执行)。
- `ui/panels/Dialogs.kt`(新增 `RestoreConfirmDialog`)。
- `data/BackupCodecRules.kt`(新文件)、`data/TaskBackupCodec.kt`、`data/TaskConverters.kt`。
- 原型:`design/mockup/app.js`、`smoke-test.cjs`(9 个对话框 + 恢复确认断言)、
  `BASELINE.md` v1.8、`README.md`;截图 `design/mockup/shots-1014/`。
- 规范:`.trellis/spec/backend/error-handling.md`、`database-guidelines.md`。
- 新增测试:`RepeatReminderAdvanceGuardTest`(4)、`BackupCodecRulesTest`(13)。
