# Remove panel scrim overlay entirely

## Goal

用户对全屏遮罩满意，但希望进一步：**完全去除遮罩背景色**。弹层（AddTaskPanel / BackupPanel / ArchivedPanel）打开时不再压暗背景，只保留面板卡片本身。

## Requirements

* R1: 三处弹层（AddTaskPanel.kt:246、BackupPanel.kt:126、BackupPanel.kt:495 ArchivedPanel）的 `.background(Color(0xFF211815).copy(alpha = 0.32f))` 移除
* R2: 保留点击遮罩区域关闭面板的行为（透明层仍拦截点击）
* R3: 面板卡片保留 elevation 阴影，与背景区分

## Acceptance Criteria

* [ ] 打开新增/编辑、备份、归档面板时背景不再变暗
* [ ] 点击面板外区域仍可关闭
* [ ] `:app:assembleDebug` 通过

## Out of Scope

* 其他 UI 调整
