# Fix task detail status bar overlap

## Goal
Keep task detail content below the system status bar and display cutout without changing existing interactions or visual styling.

## What I already know
- The user reports that the task detail page is obscured by the status bar.
- This is a single-module Android Jetpack Compose app.
- TaskDetailScreen is displayed from MainActivity; other surfaces already use window insets.

## Requirements
- Apply system-reported safe area insets to the task detail surface.
- Preserve the existing background, header, scrolling, actions, and back navigation.
- Avoid fixed status-bar heights and double application of insets.
- Protect content on cutout displays and with both gesture and button navigation.

## Acceptance Criteria
- [ ] The task detail heading and back button stay outside the status bar/cutout.
- [ ] Detail content remains scrollable and bottom actions remain usable.
- [ ] Home/quadrant navigation, editing, and back behavior are unchanged.
- [x] Relevant tests, Android lint, debug build, and git diff checks pass.

## Definition of Done
- Focused regression coverage where practical; manual-only checks called out explicitly.
- Product changelog and durable Compose guidance updated as appropriate.

## Open Questions
None; system safe-area behavior is derivable from the existing implementation.

## Out of Scope
- Visual redesign, new features, changes to stored task data, unrelated panels.

## Technical Notes
- Inspect MainActivity inset ownership and TaskDetailScreen container placement before implementing.
- Use Python UTF-8-safe edits; preserve pre-existing IDE and untracked user files.

## Technical Approach / Decision
- MainActivity uses edge-to-edge rendering and explicitly sets Scaffold contentWindowInsets to zero.
- Home and quadrant containers consume safeDrawing insets in their own branches; the sibling TaskDetailScreen does not inherit those modifiers.
- Keep the outer detail Surface full-screen so its opaque background still covers the underlying screen.
- Apply windowInsetsPadding(WindowInsets.safeDrawing) once to the inner full-size Column that owns the header, scrolling list, and bottom action bar.
- Use Compose inset consumption, not hard-coded status-bar sizes or additional padding on the shared Scaffold.

## Verification Plan
- Run existing unit tests, lintDebug, assembleDebug, and git diff --check sequentially.
- Review that the existing scroll weight, callbacks, and local visual spacing are untouched.
- Device checks: open details from home and quadrant, check header/buttons on cutout devices, scroll long content, check gesture and three-button navigation, open edit then return.
- No connected Android device or configured emulator is currently available. Do not claim visual/device verification unless that changes.
- No JVM source-text test or new UI test dependency for a framework-owned inset modifier; document device checks rather than asserting Android layout through SDK stubs.

## Bug Analysis
### Root Cause Category
- Implicit assumption / missing spec: a sibling detail screen was treated as if it inherited home/quadrant safe-area modifiers, even though the Scaffold delegates inset handling to each content branch.
- Coverage gap: existing JVM navigation tests do not exercise Android window geometry.
### Why Fixes Failed
- Not applicable; this task needed one focused inset-ownership correction.
### Prevention Mechanisms
- Added a source-backed full-screen safe-area contract and device regression checklist to compose-patterns.md, linked from the spec index.
- Preserve the full-screen background and inset only the content; avoid a global Scaffold change or fixed top margin.
### Systematic Expansion
- Existing home/quadrant containers and add/backup/archive panels already use safeDrawing. No adjacent layout patch is required.
### Knowledge Capture
- Updated Compose spec and index. This Android application has no src/templates/markdown/spec mirror to sync.

## Implementation and Validation Results
- Added safeDrawing inset consumption only to TaskDetailScreen's inner Column; the outer opaque Surface, list weight, callbacks, and original spacing are unchanged.
- Updated CHANGELOG.md, the Compose safe-area contract, and its index entry.
- :app:testDebugUnitTest --no-daemon: passed (51 tests, zero failures/errors/skips).
- :app:lintDebug --no-daemon: passed (0 errors, 24 warnings, 1 hint).
- :app:assembleDebug --no-daemon: passed; debug APK generated in app/build/outputs/apk/debug/.
- git diff --check: passed.
- Source review confirms one inset owner around header/list/actions, no fixed offsets, no global Scaffold change, and no navigation/data changes.
- Runtime acceptance checklist remains unverified: adb devices was empty and emulator -list-avds returned no configured device. No new automated Android layout test was introduced.
- User approved the four-file commit plan. Work committed as 9ab2012 (fix: respect task detail safe areas). User requested finish-work to archive the task and record this session; no remote push was performed.
- Pre-existing .idea/compiler.xml, .idea/gradle.xml, .idea/misc.xml, .impeccable/, and .opencode/ changes were left untouched and were excluded from the work commit.
