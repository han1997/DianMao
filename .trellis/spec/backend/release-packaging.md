# Release Packaging Guidelines

## Version source of truth

- `gradle.properties` holds `app.versionCode` and `app.versionName`.
- `app/build.gradle.kts` reads them into `defaultConfig` with fallbacks
  (`1` / `"1.0.0"`); never hardcode versions in the build script again.

## Releasing

```powershell
.\gradlew.bat :app:bumpVersion        # versionCode + 1, versionName patch + 1
# or: -PversionNameBump=minor|major  / -PversionNameBump=none
.\gradlew.bat :app:assembleRelease
```

Output: `app/build/outputs/apk/release/dianmao-<versionName>.apk`
(debug builds produce `dianmao-<versionName>-debug.apk`).

## Signing

- Release signing is enabled only when `keystore.properties` exists at the
  repo root; otherwise the build stays unsigned so other machines keep
  compiling.
- `keystore/` and `keystore.properties` are git-ignored. The upload key is a
  PKCS12 keystore — **store and key passwords MUST be identical** (PKCS12
  rejects distinct key passwords; we hit this 2026-09).
- Losing `keystore/dianmao-upload.keystore` or its password means the app can
  no longer be updated on devices (applicationId stays
  `fun.han1997.dianmao`). Keep an offline backup.

## Verification

```powershell
apksigner verify --print-certs app/build/outputs/apk/release/dianmao-<version>.apk
```
