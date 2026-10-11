# Release Packaging Guidelines

## Version source of truth

- `gradle.properties` holds `app.versionCode` and `app.versionName`.
- `app/build.gradle.kts` reads them into `defaultConfig` with fallbacks
  (`1` / `"1.0.0"`); never hardcode versions in the build script again.

**HARD RULE (user directive, 2026-09-12; extended 2026-10-11)**: EVERY code-change
session (any task that ships compiled-code changes) MUST, before finishing —
not only when a release is requested:

1. `py -3 ./.trellis/scripts/get_context.py` confirm work is committed or being
   committed; then run `./gradlew :app:bumpVersion` (or `.
.
gradlew.bat` on
   Windows) so every shipped change carries a fresh `versionCode`.
2. Run `./gradlew :app:assembleRelease` to produce the latest release APK —
   every session ends with a freshly built, signed installable APK, not just
   a version bump.
3. Commit the resulting `gradle.properties` change **together with the work**
   (Phase 3.4 batches them into the same change set; when the work is already
   committed, a follow-up `chore: bump to x.y.z` commit is the fix-up path).

A session that ships code without a `versionCode` increment AND a freshly
built release APK violates this rule. Known miss: the 2026-10-11 review
session committed 3 fix/refactor commits without bumping; the bump landed
afterwards as a follow-up commit — do not repeat that.

When the bump also lands user-facing changes, append a CHANGELOG.md entry at
the top (see the v1.0.9 entry for format) in the same commit.

## Releasing

```powershell
.\gradlew.bat :app:bumpVersion        # versionCode + 1, versionName patch + 1
# or: -PversionNameBump=minor|major  / -PversionNameBump=none
.\gradlew.bat :app:assembleRelease
```

Output: `app/build/outputs/apk/release/dianmao-<versionName>.apk`
(debug builds produce `dianmao-<versionName>-debug.apk`).

### Convention: auto version bump + release APK every session

**What**: Every code-change session ends with `:app:bumpVersion` followed by
`:app:assembleRelease`, and the resulting `gradle.properties` change is
committed with the session's work. The freshly built
`dianmao-<versionName>.apk` is the deliverable the user expects after every
round of changes — do not wait for an explicit "请打包" request.

**Why**: A session that changes code but ships the previous versionCode makes
the installed build indistinguishable from the broken one on a device, and a
missing APK means nothing to sideload. This convention was added after the
2026-10-11 review session missed the bump that 2026-09-12's directive already
required.

**Example**:
```powershell
# End of every code-change session, after quality checks pass:
.\gradlew.bat :app:bumpVersion
.\gradlew.bat :app:assembleRelease
apksigner verify --print-certs app/build/outputs/apk/release/dianmao-<version>.apk
# then commit gradle.properties (+ CHANGELOG.md when user-visible) with the work
```

**Related**: [Quality Guidelines](./quality-guidelines.md) for the required
checks that must pass BEFORE bumping and building.

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
