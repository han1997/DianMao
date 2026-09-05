# Move snackbars to top of app

## Goal

应用内所有 Snackbar 提示（上传成功/失败、保存成功、删除、归档等）目前出现在底部，被 FAB 和键盘遮挡且不显眼。移到顶部更直观。

## What I already know

* 全部提示走单一 `SnackbarHostState`（MainActivity.kt，约 30 处 `showSnackbar`），SnackbarHost 挂在 Scaffold 的 `snackbarHost` slot
* SnackbarHost slot 接受任意 composable，直接在自定义 host 里对齐顶部即可，无需改 30 处调用点
* 顶部需要避让状态栏（上一任务已把 Scaffold insets 清零，宿主 Box 无 insets padding）
* 深色遮罩已移除；Snackbar 自带 surface 样式，浮在内容上没有对比问题

## Requirements

* R1: 自定义顶部 SnackbarHost：`Alignment.TopCenter` + `WindowInsets.statusBars` padding
* R2: Snackbar 视觉保持 M3 默认（不动颜色/文案），仅位置变化
* R3: 所有现有 `showSnackbar` 调用点零改动

## Acceptance Criteria

* [ ] 上传成功/失败等提示出现在屏幕顶部（状态栏下方），不再被 FAB 遮挡
* [ ] 弹层面板打开时提示依然可见（Snackbar z 序在 Scaffold 层，不受面板影响）
* [ ] `:app:assembleDebug` 通过

## Out of Scope

* Toast（系统级）不用，全部走应用内 Snackbar
* 提示文案与时机不变
