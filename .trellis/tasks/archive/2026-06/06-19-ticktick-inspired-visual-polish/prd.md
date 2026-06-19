# TickTick-inspired Visual Polish

## Goal

Refresh DianMao's Android UI with a TickTick-inspired level of calm polish:
fast daily capture, clean task scanning, restrained semantic color, and a more
consistent visual system across the home board, task rows, add/edit panel,
detail page, archive, and backup surfaces. The redesign should respect
DianMao's four-quadrant product model rather than cloning TickTick's exact
information architecture.

## What I Already Know

* The user asked for a comprehensive beautification pass referencing TickTick.
* Product context says DianMao is inspired by TickTick but organized around
  priority and timing rather than a strict task/todo split.
* Design context names the north star as "Quiet Daily Control": calm, practical,
  Android-native, compact, and polished.
* Current implementation is a single-module Jetpack Compose Android app.
* Main UI code is concentrated in `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`.
* Current surfaces include:
  * `DianMaoTheme`
  * `AppHeader`
  * `QuadrantBoard` / `QuadrantPane`
  * `TaskRow`
  * `AddTaskPanel`
  * `TaskDetailScreen`
  * archive and backup panels
* Current palette already uses restrained warm primary, calendar blue secondary,
  neutral surfaces, and priority colors.
* Compose specs require accessible header actions, wrap-safe summary rows,
  explicit BackHandler state capture, and no layout overlap on narrow screens.
* Current active git state includes pre-existing workspace journal changes that
  should not be mixed into this feature unless the user explicitly asks.

## Assumptions

* "Reference TickTick" means adopt comparable product qualities: fast capture,
  polished task rows, readable list density, calm surfaces, familiar Android
  controls, and subtle state feedback.
* It should not copy TickTick trademarks, exact UI assets, or a pure list-only
  hierarchy that erases DianMao's four-quadrant model.
* This is mainly a Compose UI refresh, not a database or reminder behavior
  change.
* Chinese UI copy should be preserved or clarified where touched.

## Requirements

* Use Direction A: quadrant-first polish. Keep the four-quadrant board as the
  first screen and primary organization model.
* Preserve DianMao's current warm red primary accent instead of moving to
  TickTick's green identity.
* Target balanced compact density: show more useful task information per screen
  than the current UI while preserving readable text, 48dp action targets, and
  comfortable Android touch behavior.
* Establish a more consistent visual system for spacing, surface tone, corner
  radii, dividers, chip styles, and semantic metadata.
* Refine the home screen so it feels calmer and more TickTick-like while keeping
  four-quadrant scanning useful.
* Improve task rows for faster scanning: clearer completion affordance, title
  hierarchy, metadata grouping, and less visual heaviness.
* Refine add/edit flow so capture feels primary and classification controls feel
  secondary.
* Refine task detail page with better hierarchy for title, metadata, subtasks,
  completion notes, history, and bottom actions.
* Keep Android-native controls and accessibility semantics.
* Avoid decorative dashboard metrics, heavy card grids, glass effects,
  gradient text, and noisy gamified visuals.
* Preserve existing behavior for tasks, reminders, calendar sync, backups,
  archived tasks, subtasks, and completion history.

## Acceptance Criteria

* [ ] Home screen presents a calmer, more polished first impression while
      preserving four-quadrant navigation.
* [ ] Task rows are easier to scan and have consistent completion, metadata,
      edit, and detail affordances.
* [ ] Add/edit panel emphasizes quick task entry before optional details.
* [ ] Detail page has stronger hierarchy and a cleaner action area.
* [ ] Archive and backup surfaces match the refreshed visual vocabulary.
* [ ] Text and controls do not overlap on narrow mobile screens.
* [ ] Accessibility semantics for key actions are preserved.
* [ ] Existing app behavior is unchanged outside visual/interaction polish.
* [ ] `.\gradlew.bat :app:lintDebug --no-daemon` passes.
* [ ] `.\gradlew.bat :app:assembleDebug --no-daemon` passes.
* [ ] `git diff --check` passes.

## Open Questions

* None.

## Feasible Directions

### Direction A: Quadrant-first polish (Recommended)

Keep the four-quadrant board as the first screen, but reduce visual heaviness
and make each quadrant feel like a clean task lane. Add list-like task row
refinement and more consistent controls throughout.

Chosen by user.

Pros:
* Preserves DianMao's differentiating product model.
* Lower behavioral risk.
* Still delivers a comprehensive visual upgrade.

Cons:
* Less visually close to TickTick's list-first home.

### Direction B: Hybrid Today + quadrant board

Make the first impression more TickTick-like by adding or emphasizing a
scannable "Today / focus" list above or before the quadrant board, while keeping
the board as a secondary organization view.

Pros:
* Feels closer to TickTick's daily workflow.
* Better for fast scanning of immediate tasks.

Cons:
* Larger scope and more IA decisions.
* More risk of duplicating task representations or crowding the home screen.

### Direction C: List-first redesign

Move the app toward a TickTick-style primary task list and make quadrants an
attribute or filter.

Pros:
* Strongest TickTick resemblance.

Cons:
* Conflicts with DianMao's product purpose.
* Highest risk and likely requires more than visual polish.

## Out of Scope

* Copying TickTick brand assets, exact layouts, or proprietary visual details.
* Changing Room schema or task/reminder/calendar data behavior.
* Adding new major navigation destinations unless explicitly chosen.
* Implementing a full design system extraction into separate files unless the
  implementation naturally requires it.

## Definition of Done

* Requirements are confirmed by the user.
* UI code follows existing Compose patterns and product design rules.
* Lint/build verification passes.
* CHANGELOG is updated if the visible app experience changes enough to warrant
  release notes.
* Commit plan is prepared for user confirmation.

## Decision (ADR-lite)

**Context**: The user wants a TickTick-inspired visual polish, but DianMao's
product model is centered on four-quadrant priority organization.

**Decision**: Preserve the quadrant-first home structure and apply TickTick-like
calm density, clearer task rows, restrained semantic color, and consistent
surface treatment across the existing app surfaces.

**Consequences**: The redesign improves perceived quality without changing the
core information architecture. It will feel inspired by TickTick's practical
polish rather than becoming a clone of TickTick's list-first home.

## Color Decision

**Context**: TickTick's visual identity leans green, while DianMao already has a
warm red primary accent in `DianMaoTheme` and `DESIGN.md` describes a grounded
warm accent.

**Decision**: Keep DianMao's warm red as the primary action and selection color.
Use TickTick as a reference for restraint, clarity, and task-row polish rather
than color identity.

**Consequences**: The app keeps its own brand signal while still becoming
cleaner and more familiar to users who like TickTick's product feel.

## Density Decision

**Context**: TickTick is valued for practical task density, but DianMao is a
mobile Android app with quadrant panels, metadata chips, subtasks, and detail
actions that still need reliable touch targets.

**Decision**: Use balanced compact density. Tighten excess padding, reduce
visual borders, make metadata lighter, and improve scanning, while preserving
readable text and standard action sizes.

**Consequences**: The UI should fit more meaningful content on screen without
becoming a cramped checklist clone.

## Technical Approach

* Add a small set of local visual constants/helpers in `MainActivity.kt` for
  refreshed surfaces, dividers, metadata chips, and task row rhythm.
* Refresh `DianMaoTheme` with a slightly cleaner neutral palette while keeping
  the warm primary accent.
* Refine `AppHeader`, summary pills, `QuadrantBoard`, `QuadrantPane`, and
  `TaskRow` toward a lighter list-like polish.
* Refine `AddTaskPanel`, `TaskFormSection`, `ToggleRow`, and metadata controls
  so capture is visually primary and optional details are quieter.
* Refine `TaskDetailScreen`, archive, and backup surfaces to share the same
  surface treatment and action vocabulary.
* Keep behavior and data flow unchanged.

## Implementation Plan

1. Theme and shared visual vocabulary.
2. Home screen, quadrant board, compact task preview, and task rows.
3. Add/edit, archive, backup, and detail surfaces.
4. Lint/build verification and visual regression review.

## Verification

* `.\gradlew.bat :app:compileDebugKotlin --no-daemon` passed.
* `.\gradlew.bat :app:lintDebug --no-daemon` passed after rerunning with a
  longer timeout.
* `.\gradlew.bat :app:assembleDebug --no-daemon` passed.
* `git diff --check` passed with only line-ending warnings.
* Targeted search found no negative letter spacing, `Color.White`,
  `Color.Black`, debug prints, or `Log.` calls in `MainActivity.kt`.

## Technical Notes

* Design register: product.
* Impeccable context loaded from `PRODUCT.md` and `DESIGN.md`.
* Relevant specs:
  * `.trellis/spec/backend/compose-patterns.md`
  * `.trellis/spec/backend/directory-structure.md`
  * `.trellis/spec/backend/quality-guidelines.md`
* Relevant code:
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
  * `app/src/main/res/values/themes.xml`
  * `app/src/main/res/values/colors.xml`
