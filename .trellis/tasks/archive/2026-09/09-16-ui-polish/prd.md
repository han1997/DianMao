# UI完善 (ui-polish)

## Goal

系统性 UI 视觉打磨:对照 `DESIGN.md` 全面审计 DianMao 所有 Compose 界面与组件,修复实现与设计规范的偏差。范围仅限亮色模式,不改交互逻辑与功能语义。

## Requirements

- 以 `DESIGN.md` 为唯一真相源;发现不一致时改代码对齐文档(经用户确认的方向)。
- 审计覆盖全部 UI 文件:
  - 主题: `ui/theme/DianMaoTheme.kt`、`DianMaoTokens.kt`、`ReducedMotion.kt`
  - 屏幕: `HomeScreen.kt`、`CalendarMonthScreen.kt`、`QuadrantDetailScreen.kt`、`ReviewScreen.kt`、`TaskDetailScreen.kt`
  - 面板: `AddTaskPanel.kt`、`BackupPanel.kt`、`Dialogs.kt`
  - 组件: `CommonComponents.kt`、`DateTimePickers.kt`、`FullScreenScaffold.kt`、`QuietControls.kt`、`QuietDialogContainer.kt`、`SubTaskComponents.kt`、`TaskRow.kt`
  - 桌面小部件: `widget/DianMaoWidget.kt`
- 落盘完整审计报告 `research/audit-report.md`,按文件分组列出每条偏差 + 对应 DESIGN.md 条款。
- 修复所有确认的偏差(或经用户同意后保持现状并在报告中标注)。
- 完成后更新 DESIGN.md 仅当"实现反而更好"且用户同意的例外项。

## Acceptance Criteria

- [ ] `research/audit-report.md` 存在且按文件记录偏差与依据
- [ ] 所有确认偏差已修复或标注同意保持
- [ ] `.\gradlew lintDebug` 通过
- [ ] `.\gradlew compileDebugKotlin` 通过
- [ ] 无功能回归,不改变业务语义
- [ ] `DianMaoTheme.kt` 定义 `lightColorScheme`;不做暗色 palette(保留 M3 default fallback)

## Definition of Done

- 审计报告完成
- 修复 + lint/compile green
- DESCRIPTION 下的 DESIGN.md 同步(如有例外项)
- 提交代码(Phase 3.4)

## Technical Approach

1. 依次通读每个 UI 文件,逐项比对 DESIGN.md 的 token / 组件 / 命名规则。
2. 偏差归并进 `research/audit-report.md`。
3. 批量修复(按文件分组,保持小意图清晰的修改)。
4. 自检 + lint / compile。
5. 例外项(现状优于文档)经用户确认后 `trellis-update-spec` 同步 DESIGN.md。

## Decision (ADR-lite)

**Context**: 代码与 DESIGN.md 可能出现不一致。
**Decision**: 默认改代码对齐 DESIGN.md;例外项(现状更好)列出由用户决定是否入文档。
**Consequences**: 保持文档为单一真相源;少数例外通过 spec update 收敛。

## Out of Scope

- 暗色模式自定义(`darkColorScheme` 属未来任务)
- 交互/功能变更、新增屏幕或组件
- 动效体系调整(仅修复偏离规格的动效值)
- 翻译/文案内容改动

## Technical Notes

- Pertinent spec layers: `.trellis/spec/backend/index.md`
- DESIGN.md 位于仓库根目录,非 `.trellis/spec/`;更新文档需走 `trellis-update-spec` 通道并经用户确认。
- Build 命令: `gradlew lintDebug`、`gradlew compileDebugKotlin`