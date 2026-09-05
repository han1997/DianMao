# Research: Settings/backup panels, archive lists, confirmation dialogs

- **Query**: How do mainstream apps (TickTick, Microsoft To Do, Google Calendar/Material apps) design settings/backup panels, archive/trash lists, and confirmation dialogs — mapped onto DianMao's BackupPanel, ArchivedPanel, and 3 dialogs
- **Scope**: mixed (internal code + external sources)
- **Date**: 2026-09-04

---

## Part 1: External Findings

### Source reliability note

- Fetched live this session: `developer.android.com` (Dialogs page, Bottom sheets page, M3 package summary), `m3.material.io` component meta-descriptions (guidelines pages are JS-rendered; only their canonical meta summaries + spec knowledge retrievable), `support.ticktick.com` (help-center JSON), `support.google.com` (Gmail archive, Calendar delete/hide).
- **Microsoft support host is unreachable from this network** (connection forcibly closed on every attempt). Microsoft To Do findings below come from established product knowledge of the shipped app and are marked `[not live-verified]`. Treat details as directional, not cited.

### 1. Material 3 official guidance

**Dialog anatomy** (developer.android.com Compose Dialogs, fetched live):
- `AlertDialog` slots: `icon`, `title`, `text`, `confirmButton`, `dismissButton`, `onDismissRequest`.
- Official guidance: **use AlertDialog when you only need a two-button dialog**. If the dialog needs forms, images, or a more complex button set, use the freeform `Dialog` composable with a `Card` container instead.
- Canonical example pairs `icon = Icon(Icons.Default.Info)` + title + text + two TextButtons.
- m3.material.io dialogs overview (meta): "Dialogs provide important prompts in a user flow. Use dialogs to make sure users act on information."
- M3 spec knowledge (from m3.material.io specs, not live-verified text): basic dialogs carry **max two text buttons** on the bottom, right-aligned (stacked vertically on narrow widths); actions are text buttons, not filled/outlined; confirmation dialogs list extra choices as buttons; destructive confirm should be visually distinguishable on the confirm action. Scrim is mandatory for modal dialogs.

**Bottom sheets** (developer.android.com, fetched live):
- `ModalBottomSheet(onDismissRequest) { /* ColumnScope content */ }`; control via `SheetState`; **remove the sheet from composition after hiding**.
- m3.material.io bottom sheets overview (meta): "Bottom sheets show secondary content anchored to the bottom of the screen. There are two types: standard and modal." Modal sheets are for in-flow secondary tasks.

**Related component metas** (m3.material.io, fetched live):
- Text fields: "typically appear in forms and dialogs."
- Lists: "continuous, vertical indexes of text and images. Use lists to help users find a specific item and act on it."
- Snackbar: "short updates about app processes at the bottom of the screen. They can disappear or remain until the user takes action." — i.e., transient operation feedback belongs in snackbars, not permanent status boxes, when the operation completes and the surface closes.

### 2. TickTick (support.ticktick.com, fetched live)

- **Settings IA is hierarchical and account-centric**: "click the avatar in the top left corner, then go to **Settings → Account → Backup & Import**". Settings is a multi-level page tree, not one long sheet.
- **Backup & Import page content is grouped into exactly three verbs**: generate backups, import existing backups, import tasks from other applications. Backup actions and import actions sit on the same page but as separate sections.
- Help-center top-level groups observed: Task, Calendar, Eisenhower Matrix, Pomodoro, Habit, Collaboration, Import & Integration, Account and Security, Design Principles, Payment. The app's settings page mirrors this: group headers (Account, General, Features…) with rows beneath.
- **Task detail page** organizes fields as labeled sections (Date & Reminder, Move to List, Priority) with inline value rows — section header + value row + trailing affordance, repeated down the page.
- No in-app WebDAV; TickTick backups are generated-file based (their cloud), which is why their backup page is so simple: it's two verbs + one import source list.

### 3. Microsoft To Do `[not live-verified — network blocked]`

- **Settings page structure**: full-height panel opened from the account avatar/menu. Grouped sections with plain section headers (e.g. General/Theme, Notifications, Connected accounts, Help), each row = icon + title (+ short description) + trailing control (switch, chevron, value). No cards-in-cards; the page is a flat list with hairline dividers.
- **Danger-zone separation**: account-level destructive actions (Sign out, Delete account) sit in their own group at the bottom of settings, visually separated from preference rows.
- **List management**: each list has a header page (name, count, share/flatten/theme actions in a "…" menu) — actions live in the header, not inline per-row.
- **Completed/archived tasks**: completed items collapse into the "Completed" smart list. Item anatomy is same row as active tasks but de-emphasized (grayed title, strikethrough on some surfaces); per-row restore = unchecking the circle; list-level "Clear list"/"Clear completed" lives in the header menu, not per row. There is no separate "archive" concept — To Do uses done-state + list membership.
- **Empty states**: single muted glyph + one short sentence ("Nothing here yet"), centered, no CTA button.

### 4. Google apps (support.google.com, fetched live)

- **Gmail archive pattern**: archive removes the message from the inbox but it remains findable under "All mail"; **restore = "Move to Inbox"** — exactly one explicit action. Archiving is presented as a reversible hide, never as deletion. Empty state of a filtered/empty list is a single centered line + glyph.
- **Google Calendar removal pattern**: the help text explicitly offers a softer alternative before destruction: "If you think you might need the calendar later, you can **hide** it instead." Options are tiered: hide (reversible) → remove from list (recoverable) → delete (permanent, warned).
- **Google settings IA** (Calendar web / Gmail): left column of category links + right detail pane on wide screens; on mobile, grouped rows under section headers. WebDAV-like account forms (server/username/password) follow the pattern: fields grouped under one card/section with a single trailing save action, not a save button per field.
- **M3 dialog conventions in Google apps**: destructive confirms use a red text confirm button ("Delete") with plain text cancel; the dialog text names the exact object being destroyed ("Delete all events from your primary calendar?"). Non-destructive prompts can use filled/tonal confirm.

### 5. Well-regarded Android archive/trash patterns

- **Files by Google / AOSP DocumentsUI trash**: trash list rows = file name + meta line (days left / original path) + per-row overflow (restore, delete forever); a top-level "Empty trash" is separated from rows; a banner states retention ("Items are deleted after 30 days").
- **Google Photos trash**: retention banner at top; row-level restore via selection (long-press → "Restore" in top bar) on phones; per-row menu on web. Empty state: centered glyph + "Nothing in trash".
- Common anatomy across these: **(meta line) + (restore as the primary affordance) + (destructive action pushed into overflow or separated zone)**. Swipe actions are common for delete-in-place but restore is almost always an explicit button/menu item, because restore-from-trash is rare and must not be triggered accidentally.

### Empty-state patterns (cross-app summary)

- Consistent recipe: centered muted icon (or glyph tile) + one sentence, secondary ink color, no buttons, no illustration clutter. Optional single quiet CTA only when an action can fix the emptiness (e.g., "Add task"). TickTick and To Do both keep archive empty states to icon + sentence.

---

## Part 2: Internal Findings (DianMao as-built)

### Files Found

| File Path | Description |
|---|---|
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` L3416–3674 | `BackupPanel` — 88%-height Card sheet |
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` L3677–3844 | `ArchivedPanel` + `ArchivedTaskItem` — 75%-height centered Card |
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` L4009–4041 | `DeleteTaskDialog` |
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` L4044–4076 | `UncompleteRecurringTaskDialog` |
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` L4132–4180 | `CompletionNoteDialog` |
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` L2736–2790 | `FormSettingBlock` (section component) |
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` L4079–4129 | `ToggleRow` (icon+title+subtitle+switch row) |
| `app/src/main/java/fun/han1997/dianmao/backup/BackupSettingsStore.kt` | `WebDavSettings(url, username, password)` persistence |
| `app/src/main/java/fun/han1997/dianmao/backup/WebDavBackupClient.kt` | WebDAV upload/download client |
| `DESIGN.md` | Design constraints (see below) |
| `.trellis/tasks/09-04-ui-polish-secondary-pages/prd.md` | Task scope; explicitly forbids business-logic/data changes |

### Current BackupPanel anatomy (L3416–3674)

- Overlay: Box with scrim `#211815 @ 0.16` + `imePadding`, bottom-centered; Card `SheetShape` (22dp top), 6dp elevation, `fillMaxHeight(0.88f)`, surface color.
- Header row: IconTile(Settings, primary) + Title Large "备份与恢复" + subtitle "本地文件与 WebDAV 同步" + `HeaderActionButton(Close)`.
- Section 1 — `FormSettingBlock("本地备份", "导出当前数据，或从文件恢复")`: row of `Button(导出本地)` (filled, primary) + `OutlinedButton(本地恢复)`, both with `InlineButtonIcon`.
- Section 2 — `FormSettingBlock("WebDAV", "保存地址后可手动上传或下载恢复")`: URL field (labeled, placeholder shows https/lan examples); username + password side-by-side in one Row (password uses `PasswordVisualTransformation`, **no visibility toggle**); then Row[Outlined 保存设置 + Filled 上传备份]; then full-width Outlined 下载恢复.
- Status line: only when `status` non-blank; a Surface whose container/error color and icon are chosen by **string-matching the status text** (`失败`, `上传`, `下载`, `导出`, `设置已保存` — L3620–3633). IconTile + bodyMedium text, maxLines 2.
- Notable: no section between form and buttons beyond the block itself; the primary "上传备份" button is inside the WebDAV block, competing with "导出本地" (also filled primary) for the single primary slot. No last-backup timestamp/summary, no connection test, no account avatar/name row (TickTick anchors its settings on the account identity; DianMao has none).

### Current ArchivedPanel anatomy (L3677–3844)

- Centered Card `fillMaxWidth(0.92f) × fillMaxHeight(0.75f)`, same scrim/sheet shape/elevation; 38×4dp drag handle (decorative — centered card, not a swipe sheet).
- Header: IconTile(Archive) + "归档事项" + Close.
- Empty state (L3753–3778): centered IconTile(Inventory2, 48dp, muted) + "暂无归档事项" — matches the mainstream recipe exactly.
- List: LazyColumn of `ArchivedTaskItem`: Surface surfaceVariant@0.34 + quietBorder(0.12) → Row[ColorDot(quadrant) + Column(title bodyLarge@0.7 alpha, optional completionNote bodySmall@0.5 alpha, single line) + HeaderActionButton(Unarchive, primary on primaryContainer@0.50)].
- Notable gaps vs mainstream: **no dates** (archived-at, due, quadrant label text — only an unlabeled color dot), no count in header, no per-row meta beyond completion note, unarchive has no confirmation and no snackbar tie-in, only one row shape for what are two distinct item kinds (completed-with-note vs plain archived), drag handle on a non-draggable centered card.

### Current dialogs (L4009–4180)

All three use M3 `AlertDialog` with `icon` slot + title + text + TextButton confirm + OutlinedButton dismiss:

| Dialog | Icon | Confirm | Dismiss | Notes |
|---|---|---|---|---|
| DeleteTaskDialog | Delete (error tint) | TextButton "删除" (error color) | **OutlinedButton** "取消" | Destructive styled correctly (error confirm) |
| UncompleteRecurringTaskDialog | Replay (primary) | TextButton "确认取消完成" (default) | **OutlinedButton** "取消" | Non-destructive; default M3 text button |
| CompletionNoteDialog | EditNote (primary) | TextButton "保存备注" | **OutlinedButton** "稍后再写" | Form dialog (text field inside `text` slot) |

Inconsistency: all three dismiss buttons are **OutlinedButton**, while M3 convention (and the official AlertDialog example) uses TextButton for both actions. Outlined dismiss adds a visual third weight to a two-button dialog.

### DESIGN.md constraints relevant here

- **10% accent rule**: terracotta < ~10% of any screen; two filled primary buttons visible at once in BackupPanel works against it.
- **Quiet borders**: outline alpha 0.10–0.22 (default 0.22; completed 0.12; lanes 0.14). FormSettingBlock uses 0.10.
- **Elevation**: flat at rest; sheets 6dp; ArchivedPanel Surface tonal 2dp; no shadows on rows.
- **Motion**: 120–220ms, state-change only, FastOutSlowIn.
- **Sheets**: raised surface, SheetShape 22dp top; heights add-task 90% / backup 88% / archived 75% (centered).
- Buttons: filled = terracotta (save/confirm/完成本次 only); tonal = container; outlined = RowShape 14dp variant; text = dialog dismiss + inline secondary. *(DESIGN.md §Buttons already prescribes Text for dialog dismiss — the current OutlinedButton dismiss drifts from the spec.)*
- Light-only; status/feedback text must read on light palette.

### Cross-cutting caveats for the implementer

- Status feedback is currently driven by **string matching** on the status message (L3621–3625). PRD forbids data-layer changes; any redesign should keep the `status: String?` signature or confine changes to presentation, e.g. keep string matching but restyle the container.
- `ArchivedPanel` receives `tasks: List<TaskUiModel>`; adding dates/meta to rows is presentation-only (TaskUiModel carries dueDate/quadrant/completionNote — see L4661 usage of completionNote).
- `FormSettingBlock` and `ToggleRow` already implement the M3 "icon + title + description + trailing content" row pattern; reuse rather than new components (PRD lists both as reuse points).

---

## Part 3: Mapping — proposed structural options

### A. BackupPanel reorganization (pick one)

**Option A1 — "Settings page" pattern (TickTick/To Do model, recommended).**
Turn the sheet body into a scrolling settings page: account/sync header card (WebDAV host parsed from URL + connection state chip, mirroring TickTick's account anchor) → **section 1 "本地备份"** (FormSettingBlock, unchanged two-button row) → **section 2 "WebDAV 配置"** (FormSettingBlock reduced to pure form: URL + username + password rows + one trailing "保存设置" text/outlined action) → **section 3 "云端同步"** (FormSettingBlock or two full-width rows: 上传备份 filled primary + 下载恢复 outlined) → status line pinned as last element. Effect: exactly one filled primary button visible ("上传备份" — or "导出本地" if 云端 unconfigured), config vs. action separated, matches To Do's grouped-rows IA without new components.

**Option A2 — "Two verbs" minimalism (TickTick backup-page model).**
Keep three FormSettingBlocks but rename/reorder by data direction: **"导出"** (本地导出 + 上传备份), **"导入"** (本地恢复 + 下载恢复), **"WebDAV 账户"** (form + 保存设置). Every block gets at most one primary action; the two destructive-adjacent restore actions live together where the user expects warnings. Closest to TickTick's documented "generate / import / import-from-apps" split.

**Option A3 — Status-first dashboard.**
Promote status to a persistent "上次备份" summary row (timestamp + result, from the existing status string) directly under the header, then sections as today. Lowest churn; adds the "state anchor" mainstream settings pages have, but doesn't fix the double-primary-button issue.

### B. ArchivedPanel item anatomy (pick one)

**Option B1 — Meta-enriched row (Gmail/trash model, recommended).**
Row = ColorDot + title (full-contrast onSurface instead of 0.7-alpha) + meta line built from existing fields: quadrant label + due/archived date + completionNote preview (single line, ellipsized). Trailing: explicit Unarchive HeaderActionButton kept (mainstream restores via explicit button, not swipe); optionally long-press/swipe reserved for future delete. Header gains a count ("归档事项 · 12"). Remove the decorative drag handle.

**Option B2 — Two-kind list.**
Group into "已归档" and "已完成" sections with section headers (To Do's Completed-smart-list approach), each row B1-style. More IA churn; only worth it if archived ≠ completed semantically in the data model.

**Option B3 — Selection-mode list (Photos trash model).**
Long-press enters selection; top bar shows count + "取消归档" batch action. Best for large archives, but DianMao's archive is personal-scale; adds state complexity beyond PRD's presentation-only scope.

### C. Dialog styling direction (applies to all three)

- Keep M3 `AlertDialog` for DeleteTask and UncompleteRecurring (official: AlertDialog is for exactly this two-button shape). **CompletionNoteDialog is a form dialog** — per official guidance ("forms and multiple buttons → use Dialog with custom content/Card"), either migrate it to a small `Dialog`+Card at dialog scale, or keep AlertDialog but accept the field-inside-text pattern; both are defensible, the official line prefers the freeform container.
- Switch all **dismiss buttons from OutlinedButton to TextButton** — aligns with the official example and with DESIGN.md's own "Text: dialog dismiss" rule; removes the third visual weight.
- Destructive: DeleteTask keeps error-tinted confirm; consider filled tonal (errorContainer) or keep TextButton per M3 (TextButton is the M3-prescribed dialog action; error color carries the signal). Dialog text should keep naming the exact object ("确定删除“{title}”？") — matches Google Calendar's object-naming pattern.
- Non-destructive confirms (UncompleteRecurring) may use a tonal/filled confirm to shorten reach, but plain TextButton is the M3 default and safest under the 10% rule.
- Icon slot is fine as-is; if the 10% accent rule feels strained, the unaccented alternative is no icon (M3 dialogs default to icon-less) — keep icons only where they aid recognition (Delete/Replay) and consider dropping it on CompletionNote.
- Feedback placement: status strings for backup ops stay in-panel; task-level outcomes (unarchive, delete) already use `snackbarHostState` — keep that split (M3: snackbars for app-process updates, dialogs for interruptions).

---

## Caveats / Not Found

- Microsoft support site unreachable from this environment — To Do findings are directional, flagged `[not live-verified]`.
- m3.material.io guidelines/spec pages are JS-rendered; captured only their meta-descriptions plus canonical M3 spec knowledge. Full spec tables (elevation tokens, color roles per state) were not scraped.
- No live screenshots of TickTick/To Do settings UIs were obtainable (help-center text only); visual details for those two come from the help articles' structural descriptions.
- Internal: BackupPanel status line currently string-matches messages; I did not trace every status string producer beyond `MainActivity` (grep showed snackbar producers at L1128–1407; status string construction is above L3416 and was not exhaustively enumerated).
