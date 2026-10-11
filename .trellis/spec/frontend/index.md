# Frontend Development Guidelines

> 本目录是 DianMao 的 Jetpack Compose 前端开发规范入口。

---

## Overview

所有 UI 开发必须先读 [原型优先 UI 开发契约](./prototype-first.md),对照 `design/mockup/BASELINE.md` 中的已确认基准。
**原型未覆盖的 UI 先补原型并经用户确认,不能直接实现 Compose。**

设计令牌权威来源是 `DESIGN.md` 与 `app/src/main/java/fun/han1997/dianmao/ui/theme/`(`DianMaoTheme.kt` / `DianMaoTokens.kt`)。

---

## Guidelines Index

| Guide | Description | Status |
|-------|-------------|--------|
| [Prototype First](./prototype-first.md) | 强制原型先行、版本与确认门槛、功能保留、完整状态及视觉验收 | Active |
| [Design Prototypes](./design-prototypes.md) | `design/` 原型的归属、token 来源、离线规则、嵌套交互陷阱与冒烟测试 | Active |

---

## Quick Reference: 开始一项 UI 改动前

- [ ] 在 `design/mockup/` 找到对应的已确认原型页面与场景
- [ ] 若缺失页面/交互/状态 → 先补原型,取得用户确认(`BASELINE.md` 记录)
- [ ] 区分:已有能力 / 原型漏项 / 原型新增能力 / 原生差异
- [ ] 确认没有删除既有功能(批量操作、拖拽排序、滑动、重复规则、小组件等)
- [ ] 明暗双主题、完整状态(加载/空/错误/禁用/进行中/确认)都覆盖
- [ ] 验收:同场景明暗截图 + 320/390dp + 字体缩放,编译检查

细节见 [原型优先契约](./prototype-first.md)。

---

## Related

- Android 层规范(数据、平台、发布):[Backend Guidelines](../backend/index.md)
- 跨层思考清单:[Thinking Guides](../guides/index.md)

---

**Language**: 文档以中文为主,代码与标识符保持英文。