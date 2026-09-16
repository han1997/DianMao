---
name: DianMao
description: A personal Android task planner for four-quadrant priority, reminders, and completion notes.
colors:
  terracotta-accent: "#A84534"
  terracotta-on: "#FFFBFA"
  terracotta-container: "#F5DED9"
  terracotta-on-container: "#3A100A"
  calendar-blue: "#2B6C9F"
  calendar-on: "#F8FCFF"
  calendar-container: "#DCEAF4"
  calendar-on-container: "#071F31"
  app-surface: "#F8F8F5"
  raised-surface: "#FFFCF9"
  surface-variant: "#EDEEEA"
  primary-ink: "#1E211F"
  secondary-ink: "#5A625E"
  divider: "#D9DDD8"
  quadrant-important-urgent: "#A94332"
  quadrant-important-not-urgent: "#795C16"
  quadrant-not-important-urgent: "#2D6DA8"
  quadrant-not-important-not-urgent: "#4E6D68"
  scrim: "#211815"
typography:
  headline-large:
    fontFamily: "system-ui, sans-serif"
    fontSize: "27sp"
    fontWeight: 600
    lineHeight: "33sp"
  title-large:
    fontFamily: "system-ui, sans-serif"
    fontSize: "20sp"
    fontWeight: 600
    lineHeight: "26sp"
  title-medium:
    fontFamily: "system-ui, sans-serif"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "22sp"
  body-large:
    fontFamily: "system-ui, sans-serif"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
    letterSpacing: "0sp"
  body-medium:
    fontFamily: "system-ui, sans-serif"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "20sp"
    letterSpacing: "0sp"
  label-large:
    fontFamily: "system-ui, sans-serif"
    fontSize: "14sp"
    fontWeight: 500
    lineHeight: "18sp"
    letterSpacing: "0sp"
  label-medium:
    fontFamily: "system-ui, sans-serif"
    fontSize: "12sp"
    fontWeight: 500
    lineHeight: "16sp"
    letterSpacing: "0sp"
rounded:
  panel: "18dp"
  sheet-top: "22dp"
  row: "12dp"
  chip: "8dp"
  inline-14: "14dp"
  pill: "999dp"
spacing:
  screen-h: "20dp"
  screen-v: "8dp"
  row-h: "16dp"
  row-v: "12dp"
  section-h: "16dp"
  section-v: "14dp"
  sheet-h: "20dp"
  sheet-v: "16dp"
  gap-xl: "18dp"
  gap-lg: "14dp"
  gap-md: "10dp"
  gap-sm: "6dp"
  gap-xs: "3dp"
components:
  button-filled:
    backgroundColor: "{colors.terracotta-accent}"
    textColor: "{colors.terracotta-on}"
    rounded: "{rounded.row}"
  button-tonal:
    backgroundColor: "{colors.terracotta-container}"
    textColor: "{colors.terracotta-on-container}"
    rounded: "{rounded.row}"
    padding: "14dp 12dp"
  button-outlined:
    textColor: "{colors.primary-ink}"
    rounded: "{rounded.inline-14}"
    padding: "0dp 11dp"
  fab:
    backgroundColor: "{colors.terracotta-accent}"
    textColor: "{colors.terracotta-on}"
    rounded: "{rounded.pill}"
  task-row-card:
    backgroundColor: "{colors.raised-surface}"
    rounded: "{rounded.row}"
  quadrant-panel-home:
    backgroundColor: "{colors.surface-variant}"
    rounded: "{rounded.panel}"
  quadrant-detail-header:
    rounded: "{rounded.inline-14}"
  time-focus-lane:
    backgroundColor: "{colors.raised-surface}"
    textColor: "{colors.primary-ink}"
    rounded: "{rounded.row}"
  task-form-section:
    backgroundColor: "{colors.surface-variant}"
    rounded: "{rounded.panel}"
  fullscreen-secondary:
    backgroundColor: "{colors.app-surface}"
    rounded: "{rounded.inline-14}"
    content-width-cap: "600dp"
  chip-summary:
    rounded: "{rounded.chip}"
    padding: "9dp 7dp"
  chip-meta:
    backgroundColor: "{colors.surface-variant}"
    rounded: "{rounded.chip}"
    padding: "7dp 3dp"
  input-outlined:
    backgroundColor: "{colors.raised-surface}"
    rounded: "{rounded.row}"
  quadrant-dot:
    size: "12dp"
    rounded: "{rounded.pill}"
---

# Design System: DianMao

## 1. Overview

**Creative North Star: "The Day Compass"**

DianMao is a personal command surface for navigating daily commitments. Time is the organizing axis: what needs attention now, what can wait, and what deserves long-term investment. The warm terracotta accent marks the direction (primary action, urgency, active focus) while the quiet neutral surfaces keep the view uncluttered. Every screen should feel like a well-arranged desk where you know where everything is without scanning.

The interface sits between TickTick's practical task density and Google Calendar's native time awareness, borrowing Microsoft To Do's approachable calm but grounding it in the four-quadrant priority model. Calendar-linked items get their own blue voice so scheduling semantics stay legible without turning the app into a calendar clone. The product explicitly rejects Feishu-style operational weight, gamified check-in mechanics, and Notion-like document sprawl.

**Key Characteristics:**
- Android-native Material 3 foundation with warm personal accent.
- Fast capture first, structured refinement second.
- Restrained color: terracotta accent below 10% of any task screen surface.
- Flat-by-default elevation; shadows only for menus, drag state, and the 6dp form-dialog cards. Bottom action bars are frameless — primary actions sit directly on the page background.
- Confident, restrained components that feel native and unhurried.
- Light-scheme primary, dark-scheme supported. The Compose theme defines a warm `darkColorScheme` (deep warm brown-grey surfaces; terracotta and calendar blue lightened for contrast; containers invert to deep tinted washes; quadrant tints stay low-alpha), so the quiet-warm language holds at night. The XML app theme flips `windowLightStatusBar` and the navigation-bar color, and the XML-versioned launcher layers stay aligned with the palette. Dark surfaces are tuned to read correctly on the light-influenced palette; keep both schemes in mind when adding surfaces.

## 2. Colors

The palette is restrained: warm terracotta for action and priority, calendar blue only for scheduling semantics, and tinted neutrals that carry a faint warmth without becoming beige. Quadrant colors are semantic overlays, not decorative accents.

### Primary
- **Terracotta Accent** (#A84534 / oklch(46% 0.12 25)): The single warm anchor. Used for primary actions (add, save, confirm), current selection, focused input indicators, FAB, and the Important-Urgent quadrant. Warmer and more personal than stock Material red, but never saturated enough to feel alarming.
- **Terracotta On** (#FFFBFA): Text and icon color on terracotta backgrounds. Near-white with a faint warm tint.
- **Terracotta Container** (#F5DED9): Light warm wash for tonal buttons, selected chip backgrounds, and quadrant panel tints.
- **Terracotta On Container** (#3A100A): Deep warm ink for text inside terracotta containers.

### Secondary
- **Calendar Blue** (#2B6C9F / oklch(48% 0.10 240)): Used exclusively for calendar-linked items, scheduled time cues, and system-calendar integration. Must never appear as a general action color or compete with the terracotta accent.
- **Calendar On** (#F8FCFF): Text on calendar blue backgrounds.
- **Calendar Container** (#DCEAF4): Light blue wash for calendar chip surfaces.
- **Calendar On Container** (#071F31): Deep navy text inside calendar containers.

### Error (warm)
- **Warm Error** (#9C3B2E): Desaturated brick red in the terracotta family for destructive actions, overdue markers, and error states. Replaces the stock Material red, which reads cool-foreign next to the warm paper palette.
- **Warm Error Container** (#F3DAD5): Light warm wash behind destructive icon containers and error toasts.
- **Tonal semantics rule:** because the theme maps `secondaryContainer` to calendar blue, every tonal button or selected chip that is NOT a scheduling semantic must set its colors explicitly to the terracotta container (`primaryContainer`). Default-colored tonal controls render blue — that blue is reserved for date/time/calendar semantics only. Cloud-sync status, archive actions, and subtask add buttons are terracotta or neutral, never blue.

### Neutral
- **App Surface** (#F8F8F5): The default screen background. Warm near-white with just enough tint to avoid sterile coolness. Warmth comes from the accent, not the canvas.
- **Raised Surface** (#FFFCF9): Task rows, quadrant panels, sheets, and form areas. A half-step warmer than app surface to create tonal layering without shadow.
- **Surface Variant** (#EDEEEA): Subtle neutral for panel containers and secondary surface areas. Slightly cooler than the warm raised surface.
- **Primary Ink** (#1E211F): Main text color. Strong enough for long daily reading and system font scaling. Nearly black but with a faint warm undertone.
- **Secondary Ink** (#5A625E): Metadata, dates, quadrant labels, completed-task context. Never makes content look disabled unless the item is actually inactive.
- **Divider** (#D9DDD8): Thin separators for list structure, calendar grouping, and panel boundaries. Used at 0.34 alpha for subtle presence.

### Quadrant (semantic overlays)
- **Important-Urgent** (#A94332): "重急" items. Closely related to terracotta accent but distinguished as a semantic category color, not the action accent itself. Used at 8% alpha for panel tints.
- **Important-Not-Urgent** (#795C16): "重要" items. Warm amber-gold for long-term value tasks.
- **Not-Important-Urgent** (#2D6DA8): "紧急" items. A time-pressure blue, distinct from calendar blue.
- **Not-Important-Not-Urgent** (#4E6D68): "低压" items. Muted teal-green for deferrable tasks.

### Named Rules

**The Ten Percent Accent Rule.** Color is functional, not decorative. On ordinary task screens, saturated terracotta accent should stay below roughly 10% of the visible surface. Its rarity is the point.

**The Time Has Its Own Voice Rule.** Calendar-linked and alarmed items may use calendar blue, but only for scheduling semantics. The calendar color is never used as general decoration or a second accent.

**The Quadrant Overlay Rule.** Quadrant colors appear as low-alpha surface tints and small indicator dots, never as saturated backgrounds. Home-board quadrant panes use 0.045 alpha (about 4.5%); quadrant detail headers and empty states use 0.08 alpha. If a quadrant panel looks like it was painted with its category color, the alpha is too high.

## 3. Typography

**Display Font:** System sans (system-ui, sans-serif, Android default)
**Body Font:** Same family (system-ui, sans-serif, Android default)
**Label/Mono Font:** Same family. No separate mono or label font planned.

**Character:** Native, readable, and slightly softer than a technical dashboard. One sans family carries the entire product. The scale is tight (1.125-1.2 between steps) because a task app has many text elements and exaggerated contrast creates noise.

### Hierarchy
- **Display** (SemiBold, 27sp, 33sp line-height): Reserved for screen titles. Currently defined but not yet applied; the live screen titles (app header, sheet titles) use Title Large. Document this step as the canonical top-of-screen size and migrate titles onto it as the UI evolves. Not for marketing-style hero text inside daily screens.
- **Headline** (Bold, varies by context): Section headers in detail views, calendar stat numbers. Applied as weight overrides on headlineSmall/headlineMedium, not as a separate scale step.
- **Title Large** (SemiBold, 20sp, 26sp line-height): Secondary-destination titles ("新增事项", "备份与恢复", "归档事项"), app header ("DianMao"), and detail-screen headings.
- **Title Medium** (SemiBold 16sp / Medium 15sp, 22sp line-height): Task row titles (15sp Medium for density), form section headers, date picker labels.
- **Body Large** (Normal/Medium, 16sp, 24sp line-height, 0sp tracking): Task descriptions, completion notes, text fields. Keep long notes readable rather than compressed.
- **Body Medium** (Normal, 14sp, 20sp line-height, 0sp tracking): Secondary body text, metadata explanations.
- **Label Large** (Medium, 14sp, 18sp line-height, 0sp tracking): Buttons ("添加", "取消"), tonal button text, prominent status text.
- **Label Medium** (Medium, 12sp, 16sp line-height, 0sp tracking): Quadrant chips, summary pills, date badges, compact meta text. Never all-caps.

### Named Rules

**The Native Reading Rule.** If text would feel strange in a normal Android settings or calendar app, it is too stylized for DianMao.

**The One Family Rule.** The entire product uses the system sans stack. No display pairing, no mono supplement. If weight and scale cannot create the needed hierarchy, the hierarchy is wrong, not the font.

## 4. Elevation

Flat by default, layered only when structure requires it. Depth comes from tonal surface differences and spacing; shadows appear on floating actions, bottom sheets, menus, and dragged task items. A task row at rest is a flat surface with a faint border, never a floating card.

### Elevation Vocabulary
- **Rest (0dp)**: Task row cards, detail stat cards, quadrant board, full-screen secondary destinations. No shadow. Depth implied by tonal surface difference and a quiet border at 0.14–0.18 alpha.
- **Sheet (6dp)**: Removed from the primary flows — add/edit, backup, archive, and recurring history are now full-screen destinations (see Components). A 6dp sheet Card still wraps form dialogs such as the completion-note and time picker.
- **Drag (8dp)**: Dragged task item. `graphicsLayer.shadowElevation` rises to 8dp while the item is held, returns to 0 at rest; scale nudges to 1.02.
- **Task detail bottom bar (frameless)**: The action bar at the foot of `TaskDetailScreen` and the add/edit form's save button sit directly on the page background with no tonal/shadow frame — one frameless treatment shared by all full-screen destinations.

### Motion Vocabulary

State changes only. No orchestrated entrances, no scroll-driven sequences, no decorative choreography. Default `FastOutSlowInEasing` (cubic-bezier(0.4, 0, 0.2, 1)) on every animated value; no custom beziers. Durations sit in the 140–280ms comfort band (relaxed 2026-09 from the original 120–220ms for a smoother feel; the state-change-only principle is unchanged).

- **Sheet enter** (200ms fade + 280ms slide, 1/4 travel): Retained for legacy sheet contexts; no longer used by the primary add/backup/archive surfaces, which are full-screen destinations that swap like task detail (no entrance choreography).
- **Sheet exit** (160ms fade + 220ms slide, 1/4 travel): Mirror of enter, faster. The asymmetry (faster exit than enter) is deliberate — users perceive exits as less important than entrances.
- **Quadrant pane tint** (200ms `animateColorAsState`): Background color reacts to data changes (completion state, open count). Not a click animation — clicking a quadrant navigates immediately.
- **Task completion scale** (180ms, 0.985 vs 1.0): Subtle shrink on the row when marked done. Reads as a quiet "settled" gesture, not a celebration.
- **Save-feedback reveal** (140ms fade + 180ms slide, 1/3 travel): Capture-first save pill appears under the title field. Auto-clears after 2200ms.
- **Collapsed group reveal** (220ms fade + 220ms slide, 1/4 travel in; 160ms fade + 180ms slide out): The task-detail completion history group and the quadrant-detail completed group expand/collapse in place. Only these two collapsibles are registered; do not add more choreographed reveals.
- **Detail body expansion** (220ms fade + 220ms slide, 1/4 travel in; 160ms fade + 180ms slide out): The task-detail completion-history and quadrant-detail completed group expand from a collapsed row. Same state-change vocabulary, no spring, no bounce.
- **Drag lift** (instant, no tween): `graphicsLayer.shadowElevation` jumps to 8dp and scale to 1.02 on drag start; returns to 0 at drop. No easing — drag tracks the finger.
- **Drag commit debounce** (50ms): Delay before persisting reorder, to coalesce rapid swaps.
- **List item placement** (default `animateItemPlacement`): Reordered items glide into their new slot. Uses Material default duration/easing; do not override.

### Named Rules

**The Surface Before Shadow Rule.** Use background tone and spacing before adding shadow. A task row should not look like a floating card unless it is being dragged or presented in a sheet context.

**The Responsive Shadow Rule.** Shadows appear only in response to state changes (sheet open, drag start, menu appear). No element casts a shadow at rest unless it is a floating overlay.

**The State-Change Only Rule.** Motion conveys state (sheet opening, completion, drag, feedback). No entrance choreography, no scroll-driven sequences, no decorative motion. If a transition doesn't tell the user something changed, remove it.

**The One Easing Rule.** Every animated value uses `FastOutSlowInEasing` (cubic-bezier(0.4, 0, 0.2, 1)). No custom beziers, no bounce, no elastic. If the default doesn't feel right, the duration or the property is wrong, not the curve.

**The Finger-Tracked Rule.** Drag lift and drop apply elevation and scale instantly — no tween. The dragged element tracks the finger; easing here would feel sticky. Easing returns only when the item settles into its new position via `animateItemPlacement`.

## 5. Components

Confident and restrained. Every interactive element feels native and unhurried. Standard Material 3 affordances are the foundation; custom patterns are limited to task-specific needs (quiet borders, quadrant tints, capture-first input flow).

### Buttons
- **Shape:** RowShape (12dp radius) for tonal and outlined buttons. Default M3 shape for filled and text buttons. FAB uses CircleShape.
- **Primary (Filled):** Terracotta accent background (#A84534) with on-primary text (#FFFBFA). Reserved for save, confirm, and "完成本次" completion actions.
- **Tonal:** Terracotta container (#F5DED9) background with on-container text (#3A100A). RowShape (12dp), 14dp horizontal / 12dp vertical padding. Used for "添加" sub-task, quadrant selector, and calendar actions.
- **Outlined:** RowShape (14dp variant), 11dp vertical padding. Cancel and secondary actions in backup panel.
- **Text:** Default M3. Dialog dismiss, inline secondary actions.
- **FAB:** CircleShape, terracotta accent background. "新增事项" floating action.

### Cards / Containers
- **Task Row Card:** RowShape (12dp), 0dp elevation, quiet border at 0.18 alpha. Raised surface (#FFFCF9) background for active tasks, surfaceVariant at 0.38 alpha for completed tasks.
- **Full-Screen Secondary Destination:** Backup/settings, archive, add/edit task, and recurring completion history are full-screen surfaces like `TaskDetailScreen` — edge-to-edge app-background `Surface` with a single `windowInsetsPadding(safeDrawing)` content column and a quiet anchored top bar (back `IconButton` + optional tonal leading `IconTile` + title/subtitle + trailing actions). Content width is capped at 600dp and centered on large/expanded windows so forms read as a focused column instead of stretching edge to edge. No sheet card, no drag handle, no dimming scrim over the underlying screen. Shared scaffold: `FullScreenScaffold` in `ui/components/FullScreenScaffold.kt`.
- **Form Dialogs (completion note, date/time, repeat, reminder-lead):** No longer bottom sheets. They render as centered `PanelShape` cards on the raised surface with a quiet hairline border and 6dp elevation inside the shared `QuietDialogContainer` / `BrandedPickerDialog` family (see Dialogs). The add/edit, backup, archived, and recurring-history surfaces remain full-screen destinations; the drag handle and 90%/88%/75% sheet heights no longer apply.
- **Quadrant Panel (home board):** PanelShape (18dp radius), no shadow. Quadrant color at 0.045 alpha as a background tint, animated over 200ms on data change. Outer board surface carries a quiet border at 0.14 alpha. A 12dp color dot anchors the quadrant label; the open-count number sits top-right in the quadrant color.
- **Quadrant Detail Header / Empty State:** Inline 14dp radius. Quadrant color at 0.08 alpha as a background tint. This is the only place the 8% tint shows up; the home board stays at 4.5%. The header is a single sticky row (back + 12dp quadrant dot + title + open count) with a quiet border at 0.16 alpha — never a shadow when stuck. The quadrant description and stat dots live in a non-tinted intro block above the list so the 8% tint area stays small.
- **Task Form Section:** PanelShape (18dp), surfaceVariant at 0.26 alpha, quiet border at 0.14 alpha.
- **Collapsed Completed Group:** surfaceVariant at 0.20 alpha, RowShape, quiet border at 0.12 alpha. Header row carries a Label Medium title, a count, and a chevron; content expands in place (see Collapsed group reveal). Used for the task-detail completion history and the quadrant-detail completed section.
- **Time Focus Lane:** RowShape (12dp), raised surface background, quiet border at 0.14 alpha. Per-row background uses the lane color (error / primary / secondary) at 0.07 alpha with a 0.12-alpha border in the same color.

### Inputs / Fields
- **Style:** RowShape (12dp). Surface background for both focused and unfocused states. No filled container contrast.
- **Focus:** Primary accent at 0.45 alpha for the focused indicator line. Unfocused indicator at 0.18 alpha of outline. On the add/edit form the focused indicator is the accent moment — the title field carries no emphasized container tint.
- **Disabled:** SurfaceVariant at 0.18 alpha for container, outline at 0.10 alpha for indicator.
- **Capture-First Pattern:** Quick-capture field visible immediately at the top of the full-screen add/edit form; priority and time/reminder settings sit in labeled sections beneath it, and heavier editors (subtasks) sit lower in the same scrolling column.
- **Capture Auto-Focus:** In add mode the title field requests focus on open (`FocusRequester` + short delay) so the keyboard is up on the first frame. Edit mode does not auto-focus.
- **IME Done Save:** On the add form the keyboard Done action saves and continues capture (same as the save button). In edit mode Done only clears focus.
- **Password Visibility Toggle:** Secret fields show a trailing eye toggle that swaps `PasswordVisualTransformation` for plain text. The toggle resets to hidden when the destination reopens.

### Chips
- **Style:** ChipShape (8dp). Compact and semantic. Summary pills, "今日" badge, meta chips, quadrant labels.
- **State:** Selected chips use terracotta container background with on-container ink. Unselected remain neutral with quiet border.
- **Add/Edit setting sections (full-screen form):** Settings are grouped under
  labeled `TaskFormSection` cards. "优先级" holds the four quadrant options in a
  single segmented row (`SegmentedButton`, 8dp ColorDot), the same family the
  home smart-view switcher uses. "时间与提醒" holds five verb→value setting rows (到期日 /
  提醒时间 / 同步系统日历 / 响铃提醒 / 重复), each with a one-line subtitle that
  explains the setting, and the disabled rows state the dependency reason
  ("需先设置到期日") instead of silently dimming. Value rows show the current
  value once set and the verb when unset ("添加到期日"); the two boolean rows
  (calendar, alarm) use a trailing M3 `Switch`; repeat opens a dropdown. Rows
  are separated by hairline dividers and follow the task-detail "verb becomes
  value" dialect so form and detail read the same language.
- **Quadrant Dots:** 12dp circles in quadrant color, used as visual anchors inside chips and list items.

### Detail Page Patterns
- **Hero (TaskDetailScreen):** Completion checkbox and title share one line; a quadrant chip (12dp dot + shortTitle) sits beneath. Metadata reads as full-width icon rows inside a TaskFormSection — each row shows a verb when unset ("添加提醒") and the value when set (To Do's verb-becomes-value pattern). Calendar-linked rows use calendar blue (secondary) for scheduling semantics only.
- **Sticky Slim Header (QuadrantDetailScreen):** One row: back + 12dp quadrant dot + title + open count, tinted at 0.08 alpha with a quiet border. No shadow at rest.
- **Sectioned List:** Label Medium section headers ("未完成" expanded, "已完成" collapsed with count). Completed rows keep the completed-row treatment (surfaceVariant 0.38 alpha, border 0.12).
- **Settings-Page Backup Screen (full-screen):** Scrolling structure inside the full-screen destination: sync status header card (WebDAV host parsed from the URL + connection state chip) → 本地备份 → WebDAV 配置 (pure form with one trailing 保存设置 text action) → 云端同步 → status line last. Exactly one filled primary button is visible ("上传备份"; "导出本地" takes the primary slot only when cloud is unconfigured).
- **Meta-Enriched Archive Row:** ColorDot + full-contrast title (onSurface) + one Label Medium meta line (quadrant shortTitle · due date · completion note preview, ellipsized) + explicit Unarchive button. Header carries a count ("归档事项 · N").

### Dialogs
- **One dialog family.** All compact dialogs (two-button confirms, the
  completion-note form) render inside the shared `QuietDialogContainer`
  (`ui/components/QuietDialogContainer.kt`): a full-window warm paper scrim
  (`#211815` at 0.16 alpha) with tap-to-dismiss, a centered `PanelShape` Card on
  the raised surface with a quiet hairline border and 6dp elevation, an optional
  tonal `IconTile` header, and right-aligned TextButton actions. This is the
  same chrome the date/time pickers use, so every modal reads as one family.
- **Destructive confirms** keep the error color on the confirm TextButton and
  the text names the exact object.
- **Dismiss actions are TextButton**, never outlined — outlined dismiss adds a third visual weight to a two-button dialog.

### Date & Time Pickers
- Both pickers share one branded dialog container in
  `ui/components/DateTimePickers.kt` (`BrandedPickerDialog`): a full-window warm
  paper scrim (`#211815` at 0.16 alpha) with tap-to-dismiss, a centered
  `PanelShape` Card on the raised surface with a quiet hairline border and 6dp
  elevation, and an app-styled action row. The underlying control stays the
  Material 3 `DatePicker` / `TimePicker` (native control, branded surface).
- **Date:** M3 `DatePicker` (calendar mode). Title "选择到期日". Converts between
  the app's local-midnight convention and the picker's UTC-midnight state
  explicitly; confirm/cancel are TextButton ("确定"/"取消").
- **Time:** M3 `TimePicker` (24-hour). Title "选择提醒时间". TextButton actions
  right-aligned. No M3 TimePickerDialog composable exists; the shared branded
  wrapper replaces the old plain-card container.
- The legacy `android.app.DatePickerDialog`/`TimePickerDialog` are removed; all pickers are Compose M3 and theme-consistent.

### Navigation
- **Main Scaffold:** Single-screen layout with FAB. No side navigation, no drawer.
- **Full-screen destinations:** Task detail, backup/settings, archive, add/edit task, and recurring history all render as full-screen branches in the root Box, layered by the `resolveInAppBackTarget` priority model. Each owns its safe area via `FullScreenScaffold`.
- **Back:** BackHandler for destination navigation. Standard Android back gesture supported. Each destination's top-bar back routes through the same centralized dismiss mutation as the system-back branch.

### Quiet Border Pattern
A signature DianMao pattern. Borders use the outline color at low alpha (0.10-0.22) instead of full-strength outlines. This creates structure without heaviness. The default is 0.22 alpha; completed items drop to 0.12. Time-sensitive lanes use 0.14. If the border reads as a hard line, the alpha is too high.

### Capture-First Save Pattern
A signature DianMao flow. When the user saves a newly-created task from the add/edit form, the screen does NOT dismiss. Instead it shows a small save-feedback pill ("已保存到 [quadrant], 可继续记录") in `primaryContainer` at 0.54 alpha with a quiet border at 0.08 alpha, clears the title and detail fields, and stays open so the user can capture the next item. The feedback auto-dismisses after 2200ms. This is the visual expression of "capture before classification": the user keeps flow, structure lands quietly.

## 6. Do's and Don'ts

### Do:
- **Do** make capture faster than classification; the user should be able to write the item first.
- **Do** keep same-day tasks and long-term responsibilities in the same visual item model.
- **Do** make calendar-linked and alarmed items visually recognizable with calendar blue, without turning the screen into a calendar clone.
- **Do** support completion notes as a first-class part of the detail view.
- **Do** keep motion in the 140–280ms comfort band and use it only to convey state (sheets, completion, drag, feedback). See Motion Vocabulary for the exact specs; default `FastOutSlowInEasing` everywhere, no custom beziers, drag lift instant.
- **Do** use quiet borders (outline at 0.10–0.22 alpha) instead of full-stroke outlines to create structure without heaviness.
- **Do** keep quadrant color as a low-alpha surface tint: 4.5% on the home board, 8% on quadrant detail headers and empty states, plus indicator dots. Never as a saturated background.

### Don't:
- **Don't** copy Feishu-style operational weight, dense enterprise panels, or workspace navigation.
- **Don't** add gamified check-in app visuals such as streak pressure, badges, rewards, or celebratory clutter.
- **Don't** drift into Notion-like document sprawl, block editing, or page-heavy organization.
- **Don't** make the user choose between "task" and "todo"; that split is explicitly outside the product model.
- **Don't** use unusual Android controls that make the app feel less native.
- **Don't** use noisy productivity dashboards or decorative metrics as the main screen.
- **Don't** use the terracotta accent on more than ~10% of any task screen surface.
- **Don't** use calendar blue for anything other than scheduling semantics.
- **Don't** add shadows to task rows at rest. If a row looks like a floating card, the elevation is wrong.
- **Don't** use gradient text, side-stripe borders >1px, glassmorphism, or hero-metric templates.
- **Don't** add entrance choreography, scroll-driven sequences, custom easing curves, bounce, or elastic. Motion conveys state only — see The State-Change Only Rule.

## Launcher icon (2026-09 redesign)

The launcher mark is the **paw-print quadrant**: four toe beans colored with the quadrant palette (terracotta/ochre/blue/sage) arranged 2×2, with a larger terracotta main pad below — the '点' in 点猫. Toes stay inside the 66dp adaptive safe circle. Monochrome uses the same silhouette. Sources: `res/drawable/ic_launcher_foreground.xml`, `ic_launcher_monochrome.xml`; the warm paper background layer is unchanged.
