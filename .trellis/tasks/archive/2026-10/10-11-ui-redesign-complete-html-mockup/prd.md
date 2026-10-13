# PRD: UI redesign complete HTML mockup

## Background

DianMao 的 Compose UI 已按 DESIGN.md（"The Day Compass" 设计系统）完成多轮重设计。
本任务产出一份**完整的 HTML mockup**，把当前 App 的全部屏幕以静态高保真网页形式呈现，
用于设计审查、回归对照与后续改版的讨论底稿。

## Goal

一个自包含的 HTML 文件（内联 CSS/JS，无外部依赖，离线可开），
在浏览器中以手机外框呈现 DianMao 的全部屏幕，支持：

- 屏幕切换（顶部标签导航）
- 亮色 / 暗色双主题切换（对应 `DianMaoTheme.kt` 的 light/dark colorScheme）

## Scope — 覆盖的屏幕（忠实还原现有 Compose 实现）

1. **首页**：AppHeader（罗盘标 + 标题 + 搜索/日历/归档/设置图标钮 + 汇总 pills）、
   智能视图切换（四象限/今天/近期/收集箱 SegmentedButton）、四象限板（2×2 象限窗格、
   4.5% 象限色 tint、计数 chip、紧凑任务条）、时间关注 lane、分组任务列表（TaskRow 卡）、FAB
2. **象限详情**：sticky 单行头（返回 + 色点 + 标题 + 未完成数）、intro 统计、
   未完成列表、已完成折叠组、空状态
3. **事项详情**：hero（完成圈 + 标题）、象限 chip、事项信息 verb→value 行、
   子事项只读区、完成心得、统计点、完成历史折叠组、底部 frameless 操作栏
4. **新增/编辑事项**：capture-first 标题/补充字段、优先级 segmented、
   时间与提醒五个设置行（含依赖禁用态说明）、快捷日期 chips、保存按钮
5. **日历**：周一起始月视图网格（今日/选中态、象限点）、选中日任务列表、记一笔
6. **复盘**：完成概览、近 7 天每日分布条、四象限占比条、最近完成记录
7. **备份与恢复**：同步状态头卡、本地备份、WebDAV 配置表单、云端同步、自动备份开关
8. **归档事项**：归档行（色点 + 标题 + meta 行 + 恢复按钮）
9. **对话框示例**：QuietDialogContainer 家族（删除确认、完成心得、日期选择器）叠加展示

## Design fidelity requirements

- 颜色、圆角、间距、字号全部取自 DESIGN.md tokens 与 `DianMaoTheme.kt`
  （terracotta #A84534、calendar blue #2B6C9F、warm paper surfaces、quiet border 0.10–0.22 alpha、
  PanelShape 18dp / RowShape 12dp / ChipShape 8dp 等）
- 遵守命名规则：10% 强调色上限、象限 tint 4.5%/8%、日历蓝仅用于时间语义、
  静默边框、扁平无阴影（对话框 6dp 除外）
- 暗色主题使用 `darkColorScheme()` 的深暖棕灰面板

## Non-goals

- 不实现真实数据逻辑、手势（滑动完成/拖拽排序仅以静态示意或说明标注）
- 不追求 Compose 动画的逐帧还原（mockup 中仅保留必要的展开/收起交互）
- 不修改任何 App 代码

## Acceptance criteria

- [ ] `design/mockup/index.html` 双击即可在浏览器离线打开
- [ ] 9 组屏幕全部可切换查看，亮/暗主题均正确
- [ ] 与 DESIGN.md 逐项对照：配色、圆角、字号、边框 alpha 无明显偏差
- [ ] 无外部网络依赖（图标用内联 SVG）
