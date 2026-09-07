# Dark theme

## Goal

Ship a hand-tuned dark color scheme (DESIGN.md explicitly defers dark mode as
a future task). All visual tokens are centralized in DianMaoTheme, and recent
consistency work removed hard-coded whites/blacks, making the cost manageable.

## What I already know

- DianMaoTheme defines a single lightColorScheme; DESIGN.md says dark currently
  falls back to M3 defaults and new surfaces must read on light palette until
  then.
- Warm paper identity must translate to a warm dark (not pure black): deep
  warm brown-grey surfaces, terracotta accent slightly lightened for contrast,
  calendar blue adjusted, quadrant tints kept low-alpha.
- XML theme (themes.xml) also needs a dark variant (status bar / nav bar).
- Some places reference `surface`/`background`/`surfaceVariant` only — good;
  audit for any remaining hard-coded colors (warmScrimColor is fine in both).

## Requirements

- 手工调校 darkColorScheme(暖黑底、陶土/蓝 accent 提亮、低alpha象限tint)。
- 跟随系统深浅;主题切换无需重启。
- XML values-night 主题同步(系统栏颜色)。
- 全屏审核:对话框暖 scrim、picker、segmented、switch 在暗色下对比度达标。

## Acceptance Criteria

- [ ] 系统暗色下全 app 无纯白闪烁、无可读性问题;浅色行为不变。
- [ ] 色板遵循暖色家族(DESIGN.md 补充暗色 token 文档)。
- [ ] lint/test/assemble 绿;真机目检关键屏。

## Out of Scope

- 应用内手动主题切换开关(跟随系统即可,后续可加)。

## Technical Notes

- 新增 `darkColorScheme(...)`;DESIGN.md 增补暗色 token 表。
