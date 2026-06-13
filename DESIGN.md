<!-- SEED: re-run $impeccable document once there's code to capture the actual tokens and components. -->
---
name: DianMao
description: A personal Android task planner for four-quadrant priority, reminders, and completion notes.
---

# Design System: DianMao

## 1. Overview

**Creative North Star: "Quiet Daily Control"**

DianMao should feel like a calm personal command surface: quick to capture into, easy to scan during the day, and polished enough that repeated daily use feels considered. The design direction sits between TickTick's practical task density, Microsoft To Do's approachable calm, and Google Calendar's native time awareness.

The interface should serve the task model, not compete with it. Four-quadrant organization, scheduled reminders, calendar links, and completion notes need to remain visible and understandable without turning the app into a project-management dashboard. The product rejects Feishu-style operational weight, gamified check-in mechanics, and Notion-like document sprawl.

**Key Characteristics:**
- Android-native familiarity with personal polish.
- Fast capture first, structured refinement second.
- Restrained color, with emphasis reserved for priority, time, and active action.
- Smooth feedback and transitions, but no choreographed page entrances.
- Compact enough for daily use, never dense enough to feel corporate.

## 2. Colors

The palette should be restrained: clean neutral Android surfaces with one warm, grounded accent used sparingly for primary action, urgency, and important state.

### Primary
- **Grounded Warm Accent** ([to be resolved during implementation]): Used for primary creation actions, current selection, high-confidence focus states, and the most important quadrant or time-sensitive cues. It should read warmer and more personal than stock Android purple, but never loud or gamified.

### Secondary
- **Calendar Blue** ([to be resolved during implementation]): Used only when representing calendar-linked items, scheduled time, or system-calendar integration. It must not compete with the primary action color.

### Neutral
- **Clean App Surface** ([to be resolved during implementation]): The default screen background. Keep it close to true neutral so warmth comes from the accent and not from a beige app canvas.
- **Raised Task Surface** ([to be resolved during implementation]): The surface for task rows, quadrant panels, sheets, and completion-note areas.
- **Readable Ink** ([to be resolved during implementation]): Primary text. It must stay strong enough for long daily reading and system font scaling.
- **Secondary Ink** ([to be resolved during implementation]): Metadata, dates, quadrant labels, and completed-task context. Never make task content look disabled unless it is actually inactive.
- **Divider Line** ([to be resolved during implementation]): Thin separators for list structure, calendar grouping, and panel boundaries.

### Named Rules

**The Ten Percent Accent Rule.** Color is functional, not decorative. On ordinary task screens, saturated accent color should stay below roughly 10% of the visible surface.

**The Time Has Its Own Voice Rule.** Calendar-linked and alarmed items may use a distinct blue time cue, but only for scheduling semantics. Do not use the calendar color as general decoration.

## 3. Typography

**Display Font:** Warm sans direction ([font to be chosen during implementation], Android system fallback)
**Body Font:** Warm sans direction ([font to be chosen during implementation], Android system fallback)
**Label/Mono Font:** Not planned.

**Character:** Typography should feel native, readable, and slightly softer than a technical dashboard. Use one sans family across the product unless implementation reveals a strong reason to split display and body.

### Hierarchy
- **Display** (medium weight, app-level only): Reserved for empty states, first-run moments, and major summaries. Do not use oversized marketing-style display type inside daily task screens.
- **Headline** (medium or semibold): Screen titles, quadrant names, and focused task-detail headers.
- **Title** (medium): Task titles, sheet headers, section headers, and calendar group labels.
- **Body** (regular): Task descriptions, completion notes, metadata explanations, and settings copy. Keep long notes readable rather than compressed.
- **Label** (medium, compact): Buttons, chips, reminder labels, quadrant labels, and small status text. Avoid all-caps tracking.

### Named Rules

**The Native Reading Rule.** If text would feel strange in a normal Android settings or calendar app, it is too stylized for DianMao.

## 4. Elevation

DianMao should be flat by default and layered only when structure requires it. Depth comes primarily from tonal surfaces, spacing, and sheet behavior; shadows should appear on floating actions, bottom sheets, menus, and dragged or focused task items.

### Named Rules

**The Surface Before Shadow Rule.** Use background tone and spacing before adding shadow. A task row should not look like a floating card unless it is being dragged, selected, or shown in a modal/sheet context.

## 5. Components

No committed components exist yet. These component directions should guide the first implementation pass.

### Buttons
- **Shape:** Soft Android-native corners, not pill-shaped everywhere.
- **Primary:** Reserved for adding, saving, confirming completion, and enabling alarms/calendar sync.
- **Hover / Focus:** On Android, prioritize pressed, focused, disabled, and loading states over desktop hover language.
- **Secondary / Ghost:** Use for cancel, postpone, edit, and low-risk actions.

### Chips
- **Style:** Quadrant, time, reminder, and calendar chips should be compact and semantic.
- **State:** Selected chips should be unmistakable without becoming saturated badges.

### Cards / Containers
- **Corner Style:** Mildly rounded surfaces for quadrant panels, task detail areas, and note blocks.
- **Background:** Neutral tonal layering rather than heavy outlines.
- **Shadow Strategy:** Flat at rest; elevated only for overlays, floating actions, drag state, or active sheets.
- **Internal Padding:** Comfortable enough for thumb use and text scaling.

### Inputs / Fields
- **Style:** Fast capture field first, with progressive controls for quadrant, date, alarm, and completion note.
- **Focus:** Focus state should make the current input clear without flooding the screen with accent color.
- **Error / Disabled:** Use plain language and native Android affordances. Disabled controls must still be legible.

### Navigation
- **Style:** Keep navigation minimal. Prioritize today, quadrant view, calendar-linked view, completed notes, and settings only if those destinations are actually implemented.

## 6. Do's and Don'ts

### Do:
- **Do** make capture faster than classification; the user should be able to write the item first.
- **Do** keep same-day tasks and long-term responsibilities in the same visual item model.
- **Do** make calendar-linked and alarmed items visually recognizable without turning the screen into a calendar clone.
- **Do** support completion notes as a first-class part of the detail view.
- **Do** use smooth Android state transitions for opening sheets, selecting quadrants, completing tasks, and setting alarms.

### Don't:
- **Don't** copy Feishu-style operational weight, dense enterprise panels, or workspace navigation.
- **Don't** add gamified check-in app visuals such as streak pressure, badges, rewards, or celebratory clutter.
- **Don't** drift into Notion-like document sprawl, block editing, or page-heavy organization.
- **Don't** make the user choose between "task" and "todo"; that split is explicitly outside the product model.
- **Don't** use unusual Android controls that make the app feel less native.
- **Don't** use noisy productivity dashboards or decorative metrics as the main screen.
