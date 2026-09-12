# 发版版本管理 + APK 命名 + release 签名

## 背景
当前 `versionCode=1 / versionName="1.0"` 硬编码在 `app/build.gradle.kts`，release 无签名配置，APK 输出名是默认的 `app-release.apk`/`app-release-unsigned.apk`。

## 范围
1. **版本来源**：`gradle.properties` 增加 `app.versionCode` / `app.versionName`，`build.gradle.kts` 读取，带兜底默认值。
2. **`bumpVersion` Gradle 任务**：`versionCode+1`；支持 `-PversionNameBump=patch|minor|major` 进位 versionName；写回 properties。
3. **release 签名**：`keystore/dianmao-upload.keystore`（RSA-3072 / 10000 天 / 随机强密码存 `keystore.properties`），`build.gradle.kts` 在文件存在时启用 signingConfig，不存在则跳过（避免其它环境编译失败）。
4. **APK 输出命名**：`dianmao-${versionName}.apk`，debug / release 统一。
5. `.gitignore` 增加 `keystore/`、`keystore.properties`。

## 非目标
- 不改 `applicationId`/`namespace`/源码包名（保持 `fun.han1997.dianmao` 不变，用户已确认）。

## 验收
- [ ] `bumpVersion` 执行后 `versionCode` 递增并写回
- [ ] `assembleRelease` 产出 `app/build/outputs/apk/release/dianmao-<versionName>.apk`
- [ ] `apksigner verify --print-certs` 通过
- [ ] keystore 文件不在 git 追踪中
- [ ] `./gradlew :app:assembleDebug :app:testDebugUnitTest` 绿
