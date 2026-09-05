# Hide FAB while overlay panels are open

## Goal

打开设置（备份）、归档、新增/编辑等弹层时，FAB 仍浮在弹层上方（Scaffold slot z 序高于内容层），遮挡面板内容视野。弹层打开期间应隐藏 FAB。

## What I already know

* FAB 在 `Scaffold.floatingActionButton` slot（MainActivity.kt:354），当前条件 `activePriority == null && taskForDetail == null`
* 弹层状态：`showBackupPanel` / `showArchivedPanel` / `isTaskPanelVisible`（showAddSheet || taskForEdit）
* 弹层 Box 画在 Scaffold 内容层，FAB slot 在其上，且上一任务已移除 scrim，FAB 更显突兀

## Requirements

* R1: FAB 显示条件加上"所有弹层均未打开"：`!showBackupPanel && !showArchivedPanel && !isTaskPanelVisible`
* R2: 条件变化用 `AnimatedVisibility` 平滑隐藏（可选，若 FAB slot 直接支持 modifier）—— 保持简单，先用条件移除
* R3: 关闭弹层后 FAB 恢复

## Acceptance Criteria

* [ ] 打开备份/归档/新增面板时 FAB 不再显示
* [ ] 关闭面板 FAB 恢复
* [ ] `:app:assembleDebug` 通过

## Out of Scope

* 其他浮层（对话框 AlertDialog 不受影响）
