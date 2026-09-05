# Global UI consistency: M3 pickers and polish (全局 UI 统一：M3 选择器与细节抛光)

## Goal

全局 UI 统一，重点是新增页面与日期时间选择器。当前 `showDatePicker`/`showTimePicker` 使用 `android.app.DatePickerDialog`/`TimePickerDialog`（XML 主题样式），与 Compose M3 界面视觉割裂。替换为 Compose M3 选择器并对新增面板做一致性抛光。

## What I already know

- `ui/logic/PlatformHelpers.kt` L180-212：`showDatePicker`/`showTimePicker` 用 android.app 对话框（Calendar + DatePickerDialog/TimePickerDialog）
- 调用点：`ui/panels/AddTaskPanel.kt` chips 行的日期/时间 chip（唯一调用方）
- `ui/theme/DianMaoTheme.kt`：lightColorScheme + Typography，M3 选择器可直接继承
- minSdk 31，Compose M3 DatePicker/TimePicker API 可用（material3 1.1+）
- 新增面板现状（上轮已重构）：sticky 捕获头 + chips 行 + 折叠子事项区
- 用户确认范围：**全面统一** = 选择器替换 + 新增面板细节抛光 + 全局一致性审计（按钮/chips/输入框/对话框）

## Requirements

### 1. Compose M3 日期时间选择器（核心）

- **DatePicker**：用 `DatePickerDialog`（M3）+ `rememberDatePickerState` 替换 `showDatePicker`
  - 显示模式 `DatePickerDisplayMode.Picker`（日历视图）
  - 主题自动继承 DianMaoTheme（terracotta 主色、RowShape 圆角）
  - 确认/取消 TextButton（对齐对话框规范）
  - 选中后回写 `dueDateMillis`（UTC→local 语义与现有一致：当天 00:00 local）
- **TimePicker**：用 M3 `TimePicker`（24h，`is24Hour = true`）+ 自由 Dialog+Card 容器（M3 无 TimePickerDialog 组合件，官方建议自定义容器）
  - 确认/取消 TextButton
  - 选中后回写 `dueTimeMinutes`
- **迁移方式**：`showDatePicker`/`showTimePicker` 命令式函数删除，改为 AddTaskPanel 内的 Compose 状态（`showDatePickerDialog`/`showTimePickerDialog` Boolean + 条件渲染对话框组件）
- 新组件放 `ui/components/DateTimePickers.kt`：`DianMaoDatePickerDialog`、`DianMaoTimePickerDialog`

### 2. 新增面板细节抛光

- chips 行视觉节奏复核（选中态/未选态/disabled 态对比度）
- 折叠子事项区展开态与面板间距节奏统一
- 保存按钮与 pill 反馈的间距层级

### 3. 全局一致性审计（小修为主）

- 对话框：确认三个对话框按钮/图标/圆角一致（上轮已 TextButton 化，复核）
- 输入框：全部走 `quietTextFieldColors()`（复核无遗漏）
- chips：FilterChip 选中态统一 terracotta container
- 按钮形状：filled/text 用默认 M3，tonal/outlined 用 RowShape（复核）

## Acceptance Criteria

- [ ] 日期选择器为 Compose M3 DatePickerDialog，主题继承正确
- [ ] 时间选择器为 Compose M3 TimePicker（24h），Dialog+Card 容器
- [ ] android.app DatePickerDialog/TimePickerDialog 引用清零
- [ ] 选择日期/时间后 chips 行正确显示值，保存语义不变
- [ ] 新增面板与全局组件风格一致
- [ ] `.\gradlew.bat :app:testDebugUnitTest --no-daemon` 通过
- [ ] `.\gradlew.bat :app:lintDebug --no-daemon` 通过
- [ ] `.\gradlew.bat :app:assembleDebug --no-daemon` 通过

## Definition of Done

- 三项 Gradle 检查全绿
- DESIGN.md 更新（选择器组件规范）
- CHANGELOG 更新

## Out of Scope

- 深色模式
- 新功能
- 数据层变更

## Technical Notes

- M3 DatePicker API：`androidx.compose.material3.DatePickerDialog`、`rememberDatePickerState(initialSelectedDateMillis)`、`DatePicker(state)`
- M3 TimePicker API：`rememberTimePickerState(initialHour, initialMinute, is24Hour = true)`、`TimePicker(state)`
- 毫秒语义：`DatePickerState.selectedDateMillis` 是 UTC 午夜，转 local 当天需 `Instant.ofEpochMilli(utc).atZone(UTC).toLocalDate().atStartOfDay(ZoneId.systemDefault())` 转换（与现有 midnightMillis 语义对齐）
- 旧 `showDatePicker`/`showTimePicker` 删除后 `Calendar`/`DatePickerDialog`/`TimePickerDialog` import 清理
