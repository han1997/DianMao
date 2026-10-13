# PRD: 像素级对齐已确认原型

## 目标

参照 `design/mockup/`(用户 2026-10-11 确认为基准)对正式 Compose UI 做**像素级**对齐,
保证软件界面与原型一致。规则见 `.trellis/spec/frontend/prototype-first.md` 第 0 节。

## 基准与验证方法

- 基准:原型 `design/mockup/index.html`(基准 commit 见 `design/mockup/BASELINE.md`)。
- 模拟器:AVD `nordic-ui-api34`(API 34),`wm density 421` → 逻辑宽度 **410dp = 原型屏宽**,
  使两图可直接并排比对。
- 采集:`.tmp-shots/proto-<page>-<theme>.png`(原型,806×1640)与设备截图;
  用 ImageMagick `montage` 并排。每屏需产出「原型 vs 实现」并排图作为验收证据。

## 逐屏差异清单(2026-10-12 完成)

| 页面 | 已实施 | 状态 |
|---|---|---|
| 图标(全局) | 41 个原型细线描边图标经 `gen_icons.py` 生成为 `DianMaoIcons`,全部 `Icons.*` 调用点替换 | 完成 |
| 首页 | header 2dp 间距/40dp 盒/18dp 图标/tile 55%;智能视图切换器改 `QuietSegmented`;时间关注 lane 图标 18dp | 完成 |
| 象限详情 | header 改固定 scaffold bar(surface 底 + 底部发丝线,pad 8/16);空态改 form-section accent(18dp r/pad 11-14/12sp hint);折叠 chev 18dp;`⋮` 透明 34dp | 完成 |
| 事项详情 | header eyeline 在上;提醒行仅开关无提前量 | 完成 |
| 新增/编辑 | `QuietField` 替换全部 `OutlinedTextField`(44dp box + 72dp 多行);section 图标 tile 30/18dp | 完成 |
| 日历 | 周行零间距(48dp/行);FAB 仅首页/象限;FAB 56dp 16r 6dp 阴影 | 完成 |
| 复盘 | `.block` 图标改 20dp tile/12dp 图标;事件行顶对齐;share 行 32/66dp min-width | 完成 |
| 设置(一级) | 新建 `SettingsScreen`(profile hero + 外观/数据/关于) | 完成 |
| 主题(二级) | 新建 `AppearanceScreen`(三模式 radio + 双色预览) | 完成 |
| 小组件与通知 | 新建 `WidgetNotificationsScreen`(宽 8/窄 4 + 通知预览) | 完成 |
| 备份与恢复 | 三个字段改 `QuietField`;sync-head gap 4dp | 完成 |
| 归档 | 行 padding 改 14dp | 完成 |
| 对话框 | 容器 max-width 330dp | 完成 |
| 多选模式 | header 改「关闭 + 已选 N + 全选」;底栏改原型双按钮(移动象限/删除所选) | 完成 |
| 其它全局 | `CompletionMark` 去多余 padding;`InlineStatDot` 用完整 --c 色;`InlineButtonIcon` 20dp tile/15dp 图标 | 完成 |

### 工具链(新增,可复用)

- `design/mockup/tools/measure-proto.cjs` — 原型 DOM 几何/排版导出(390×808 逻辑空间)。
- `design/mockup/tools/measure-device.sh` — 设备 uiautomator 层级导出并归一到 390 宽。
- `design/mockup/tools/diff-geometry.py` — 按文本匹配两侧元素并报告 dx/dy/dw/dh 差值。
- `design/mockup/tools/gen_icons.py` — 原型 SVG sprite → Compose ImageVector。
- `design/mockup/tools/capture-proto.cjs` / `compare.sh` — 12 屏 ×2 主题截图与并排图。

## 范围

- 仅改 UI:布局、间距、字号、字重、圆角、颜色、透明度、图标、文案、块顺序、入口与返回链路。
- 不改变业务逻辑、数据模型、平台集成行为。

## 非目标

- 不新增原型没有的元素或能力;外观三模式与设置两级化为**已要求新增**,
  按原型实现并在 `BASELINE.md` 登记为待确认范围。

## 验收标准

- [ ] 每屏产出明/暗两张并排图,可见差异为零或逐条登记为允许偏差。
- [ ] `:app:assembleDebug` + `:app:lintDebug` + `:app:testDebugUnitTest` 通过。
- [ ] 320dp / 390dp / 410dp 无横向溢出;字体 1.0 与 1.3 不破版。
- [ ] 明暗双主题均与原型一致。

## 验收进展(2026-10-13)

- 并排图补齐(compare.sh):`cmp-{settings,appearance,widget,backup,archive,review,quadrant}-{light,dark}.png`,
  加上既有 home/detail/add/calendar/quadrant 等共 12 屏 ×2 主题。
- 几何 diff(443dpi,390×808dp 归一化):新增屏 x 向偏差 ≤3.8dp;
  行内间距与原型一致(appearance 69/69、widget 204/204/159、settings 组内匀速)。
  存在与已验收首页相同的 ~-20dp 系统纵向偏移(设备真实状态栏 vs 原型假状态栏),属既有基线。
- **对话框**:原型 `dialogs` 页是 8 个预览卡片堆叠的静态展示页,设备侧无对应单屏;
  登记为**允许偏差**,容器规格(330dp max-width / 18dp 圆角 / 发丝边框 / 6dp 阴影)
  经共享 `QuietDialogContainer` 与既有 `dev-datepicker`/`dev-timepicker` 证据核对。
- **允许偏差**:WebDAV URL 字段的 `supportingText`(“备份写入目录内 …”)原型无此行,
  属实现侧辅助说明,登记为允许偏差。

### 本轮修复(检查阶段发现)

- `BackupPanel`:`3d7e0af` 拆卡片时误将滚动列提前闭合,四个 `FormSettingBlock` 变成
  外层非滚动列的兄弟节点,导致 `weight(1f)` 滚动列塌缩为 0 高——云同步状态头卡
  **完全不可见**,页面尾部(下载恢复/自动备份)无法滚动到达(字体 1.3 亦破版)。
  已把块重新放回滚动列,连接 chip 回到文本列(对齐原型 `.sync-head .tx`)。
  设备复核:头卡渲染、尾部可滚动到、1.0/1.3 无横向溢出。

### 遗留待确认(不属本任务)

- `SettingsScreen` hero 的版本号写死为 `v1.1.2 · The Day Compass`(与原型一致,
  故像素对齐无偏差),但已落后于 `versionName`。需先改原型再取确认,不能单侧修改;
  建议后续改为从 `BuildConfig.VERSION_NAME` 读取并在原型同步占位符。
- WebDAV URL 字段的 `supportingText` 已登记为允许偏差;若要严格对齐,需先在原型补该行。