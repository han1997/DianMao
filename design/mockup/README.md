# DianMao UI 概念原型 · 01

> 本原型是 UI 开发的强制前置:任何 Compose UI 改动必须先有已确认的原型。
> 强制规则见 [原型优先 UI 开发契约](../../.trellis/spec/frontend/prototype-first.md),
> 确认范围与待确认项见 [BASELINE.md](./BASELINE.md)。

用浏览器直接打开 `index.html` 即可,无需安装依赖或启动服务器。
HTML 会读取同目录的 `style.css`、`app.js`,图标全部内联,完全离线。

本目录是**独立设计预览**,不是 Android 运行时依赖;正式 UI 仍以 Compose 代码为准。

## 结构(参考「原型与介绍分离」的呈现模式)

- 左栏 `studio`:品牌、提案文案、九页章节导航。
- 中栏 `stage`:手机外框 + 可交互预览,顶部可切明/暗主题。
- 右栏 `notes`:每页的设计说明与「试着操作」。

窄屏(< 820px)自动变为全屏手机界面,隐藏三栏外壳。

## 覆盖的屏幕(对应 Compose 实现)

1. 首页:AppHeader、汇总 pills、智能视图切换(四象限/今天/近期/收集箱)、时间关注 lane、四象限板。
2. 象限详情:8% 象限 tint 单行头、intro 统计、未完成列表、已完成折叠组。
3. 事项详情:hero、象限 chip、verb→value 信息行、子事项、完成心得、完成历史折叠、底部操作栏。
4. 新增/编辑:capture-first 字段、优先级 segmented、时间与提醒设置行(含依赖禁用态)、快捷日期、子事项。
5. 日历:周一起始月视图、象限点、选中日列表、记一笔。
6. 复盘:完成概览、近 7 天分布、四象限占比、最近完成。
7. 备份与恢复:同步状态头、本地备份、WebDAV 配置、云端同步、自动备份开关。
8. 归档事项:色点 + 标题 + meta + 取消归档。
9. 对话框:QuietDialogContainer 家族三例。

## 设计来源

- `DESIGN.md`:The Day Compass 设计系统(配色 / 字体 / 圆角 / 间距 / 命名规则)。
- `app/src/main/java/fun/han1997/dianmao/ui/theme/DianMaoTheme.kt`:亮/暗 colorScheme 与形状常量。
- `.../ui/theme/DianMaoTokens.kt`:透明度刻度。
- `.../ui/screens/*` 与 `.../ui/panels/*`:各屏结构与文案。

## 演示边界

- 不连接系统日历,不发送通知,不读写本地或网络数据。
- 任务、统计、同步状态均为内存中的演示数据,刷新即复位。
- 手势(滑动完成、长按拖拽排序)未实现,仅以静态示意。
- 主题默认跟随系统,桌面预览工具栏可手动切换用于验收。

## 验证(可选)

需本机已有 Playwright 时执行:

```
PLAYWRIGHT_MODULE="<playwright 模块绝对路径>" node smoke-test.cjs
```

会检查九页渲染、导航、搜索、折叠、表单保存、日历翻月、密码显隐、归档恢复、
自定义提醒时间(步进/输入/绝非法值)、对话框不超出手机框、顶栏设置图标语义、
明暗主题与 390dp 无横向溢出,并输出截图(`preview-desktop/dark/mobile.png` 与
`preview-date-dialog.png`)。