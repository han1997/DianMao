# UI/交互 Polish 全流程

## 背景

DianMao 当前功能已成型（拖拽、Smart View、备份、通知跳转），但可访问性基线与设计一致性存在 5 类已知缺口。本任务一次性完成全部 5 项打磨。

## 范围

### A. 全局 Reduced-Motion
- 新增 `ui/theme/ReducedMotion.kt`：`LocalAnimationsEnabled: ProvidableCompositionLocal<Boolean>`
- 在 `MainActivity`（或根 Composable）读 `Settings.Global.ANIMATOR_DURATION_SCALE != 0f` 提供该值
- 提供减小动画的辅助 API（如 `rememberAnimationsEnabled()`、动画时长/位移为 0 的 `spring`）
- 接入全部 6 处动画点：
  - `ui/components/TaskRow.kt:96` animateFloatAsState（完成徽标）
  - `ui/components/TaskRow.kt:109` graphicsLayer（拖拽 lift）
  - `ui/screens/HomeScreen.kt:174` AnimatedVisibility
  - `ui/screens/HomeScreen.kt:483` animateColorAsState（象限底色）
  - `ui/screens/TaskDetailScreen.kt:553` AnimatedVisibility（完成历史折叠）
  - `ui/screens/QuadrantDetailScreen.kt:232` AnimatedVisibility（已完成折叠）
  - `ui/panels/AddTaskPanel.kt:569` AnimatedVisibility
- reduced 模式下：动画直接跳终值（duration=0），不渲染中间帧

### B. 备份面板加载态
- `ui/panels/BackupPanel.kt`：上传 / 恢复 / 加载列表期间：
  - 显示 `CircularProgressIndicator`
  - 对应按钮 `enabled = false`
- 用 `remember { mutableStateOf(false) }`（必要时 `rememberSaveable`）记录 in-progress 标志
- 结束（成功/失败）时复位

### C. 拖拽排序 TalkBack 语义
- `MainActivity.kt:517` 长按拖拽所在 item 加 `Modifier.semantics { customActions = ... }`：
  - "上移" / "下移" 两个 CustomAccessibilityAction
  - 非首项才注入"上移"，非末项才注入"下移"
  - 触发时执行与手势相同的数据重排逻辑

### D. Alpha token 归一化
- 新增 `ui/theme/DianMaoTokens.kt`：
  ```kotlin
  object DianMaoAlpha {
      const val subtle = 0.08f   // 极淡 chip 底
      const val muted  = 0.62f   // 次级文本
      const val strong = 0.78f   // 强调次级
  }
  ```
- 全文扫 `copy(alpha = X)`，按下表归并：
  - 0.045/0.05/0.07/0.08/0.10 → subtle
  - 0.48/0.50/0.54/0.62/0.65/0.68 → muted
  - 0.70/0.72/0.78/0.82 → strong
- 替换文件：`HomeScreen.kt`、`QuadrantDetailScreen.kt`、`CommonComponents.kt` 及其它命中处

### E. 小修
1. `ui/screens/TaskDetailScreen.kt:857` 触摸目标 44dp → 48dp
2. `ui/panels/BackupPanel.kt:292` 密码可见性 IconButton 加 `sizeIn(minWidth=48.dp, minHeight=48.dp)`
3. `ui/components/TaskRow.kt:102` 完成动画 LaunchedEffect 竞态：改用 `snapshotFlow` 读取目标值

## 验收标准
- [ ] `./gradlew :app:assembleDebug` 通过
- [ ] `./gradlew :app:lintDebug`（如已配置）无新增警告
- [ ] 系统动画关闭后（开发者选项），上述 6 处动画直接到位无过渡
- [ ] 备份上传中按钮 disabled 且有进度指示
- [ ] TalkBack 下可对列表项执行"上移/下移"
- [ ] 全库 `copy(alpha =` 命中只剩 token 引用
- [ ] 两处 48dp 触摸目标合规

## 非目标
- 不重写主题色板 / Typography
- 不做 i18n
- 不动 swipe-to-complete 手势逻辑本身
