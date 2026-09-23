# 应用图标重设计：日之罗盘

## 背景

现有爪印图标（2026-09 爪印四象限方案）整体造型不被用户喜欢，本次推翻重来。
设计调性：**极简现代** —— 干净纯色背景 + 简洁几何图形（iOS/Linear 式克制感）。

## 概念：日之罗盘 (The Day Compass)

呼应 DESIGN.md 的产品北极星 "The Day Compass"：圆形表盘 + 一根品牌色指针，
指向「今天该做什么」。图形抽象、工具感、一眼可识别。不含方位刻度（用户已确认）。

## 几何规格

所有前景图形落在自适应图标安全圆内（中心 54,54，半径 33，直径 66dp）。

| 元素 | 规格 |
|---|---|
| 背景层 | 纯色 `#F8F8F5`（暖纸底，保持现状**不变**） |
| 表盘环 | 细圆环，fill `#F5DED9`（terracotta container）；外半径 27，环厚 2（内半径 25），用双圆相减画法实现 |
| 指针 | 细长菱形，fill `#A84534`（terracotta accent），指向右上 45°，尖端距中心约 20，尾端距中心约 8 |
| 轴心 | 圆点，fill `#1E211F`（primary ink），半径 2.2，压住指针根部 |

Monochrome 层：同一几何（环 + 指针），单色 `#000000` 剪影（轴心点保留）。

## 范围

- 改：`app/src/main/res/drawable/ic_launcher_foreground.xml`
- 改：`app/src/main/res/drawable/ic_launcher_monochrome.xml`
- 改：`DESIGN.md`「Launcher icon」章节（替换爪印描述为日之罗盘）
- 不动：`ic_launcher_background.xml`、`AndroidManifest.xml`、全部 webp
  （minSdk 31，adaptive vector 优先）、`mipmap-anydpi/*.xml` 层引用结构

## 验收

1. `:app:processDebugResources`（或 assembleDebug）通过，vector XML 合法
2. 图形不超出 66dp 安全圆（桌面圆角蒙版下不裁切）
3. 单色层剪影在 themed icon 下清晰可辨（环 + 指针形状完整）
4. 用户在设备/模拟器上确认视觉效果，允许对指针角度/环厚快速迭代
