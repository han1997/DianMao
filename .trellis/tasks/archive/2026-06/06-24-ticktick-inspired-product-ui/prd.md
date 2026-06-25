# TickTick-Inspired Product and UI Improvements

## Goal

Improve DianMao's daily task-management experience by borrowing the useful parts
of TickTick's product model: faster scanning, smart task views, better planning
surfaces, and more polished page structure, while preserving DianMao's
four-quadrant identity and capture-first workflow.

## What I Already Know

* The user wants functionality and UI improved with TickTick/嘀嗒清单 as the
  reference.
* DianMao is a personal Android productivity app built with Kotlin, Jetpack
  Compose, Material 3, Room, Calendar Provider, AlarmManager, and WebDAV.
* The product explicitly wants TickTick-like lightweight task experience without
  a rigid task/todo split.
* Current features already include four-quadrant organization, quick add,
  date/time picker, system calendar sync, local alarms, daily/weekly/monthly
  repeat, subtasks, completion notes/history, archive, backup/restore, and a
  compact home time-focus lane.
* Current UI is mostly in `MainActivity.kt`; data is in `TaskModels.kt`,
  `TaskRepository.kt`, `TaskDao.kt`, and Room schema files.
* Existing design system calls for a restrained product UI: native Android
  controls, warm terracotta accent below 10%, quiet borders, compact task rows,
  no noisy dashboards, no gamification, and no unusual controls.
* TickTick-style gaps in DianMao are smart views, broader scheduled browsing,
  search/filter, unplanned/inbox planning, tags/lists, natural-language capture,
  widgets, habits, and focus/Pomodoro.

## Assumptions

* The first implementation should be an MVP, not a full TickTick clone.
* The safest first pass should avoid Room schema changes unless the user
  explicitly prioritizes tags/lists or natural-language capture.
* UI changes should keep the app light, dense, and Android-native rather than
  adding marketing-style hero sections, decorative cards, or dashboard metrics.

## Research References

* [`research/ticktick-patterns.md`](research/ticktick-patterns.md) - TickTick
  patterns mapped to DianMao's current product and code constraints.

## Requirements (Evolving)

* MVP direction: **Home Command Center**.
* Preserve DianMao's four-quadrant model as a core organizing layer.
* Preserve capture-first add/edit flow.
* Improve home page scanning and task discovery.
* Add smart home views on top of existing task data, without adding tags/lists
  or changing the persisted task schema in this iteration.
* First version smart views:
  * `四象限`: current home quadrant board.
  * `今天`: overdue, current-day, and today's later timed tasks.
  * `近期`: timed active tasks in the next 7 local days.
  * `收集箱`: active tasks without due time.
* Add search over the currently selected smart view, matching task titles and
  subtask titles.
* Support quick browsing of time-sensitive tasks and unplanned tasks from the
  home screen.
* Improve page UI with product-register discipline: familiar controls,
  compact density, quiet surfaces, stable dimensions, accessible touch targets,
  and no decorative dashboard clutter.
* Keep calendar/alarm/repeat behavior reliable and unchanged unless explicitly
  in scope.

## Candidate Approaches

### Approach A: Home Command Center (Recommended)

Add smart views and filtering on top of existing data.

* How it works: add a restrained segmented control for `四象限`, `今天`, `近期`,
  and `收集箱/未计划`; add search/filter state; show task rows in the selected
  view using existing data and row components.
* Pros: visible TickTick-style improvement, improves daily use, mostly UI/helper
  logic, likely no Room migration.
* Cons: does not add tags/lists or natural-language capture yet.

### Approach B: Quick Capture Intelligence

Make adding tasks more TickTick-like by parsing dates, priority, repeat, or
shortcuts from the title.

* How it works: parse input like "明天 9 点 开会" into due date/time, infer repeat
  or quadrant shortcuts, and preview detected metadata in the add sheet.
* Pros: improves the most frequent action, high perceived intelligence.
* Cons: parsing ambiguity, Chinese date handling, more tests, and possible UX
  edge cases.

### Approach C: Lists/Tags Expansion

Add persisted tags or list folders similar to TickTick.

* How it works: add new fields/entities, migrations, backup codec support, tag
  chips, and filtered views.
* Pros: major organization capability.
* Cons: broader data-model blast radius and heavier UI; risk of diluting the
  four-quadrant model.

## Acceptance Criteria (Evolving)

* [x] Chosen MVP has clear in-scope and out-of-scope boundaries.
* [ ] Home provides a clear smart-view switcher for `四象限`, `今天`, `近期`,
  and `收集箱`.
* [ ] Search filters the selected smart view by task title and subtask text.
* [ ] The selected smart view shows the right subset of active tasks using
  deterministic helper logic.
* [ ] The existing four-quadrant board remains available as a primary view.
* [ ] Home/task UI remains compact, native, and consistent with `DESIGN.md`.
* [ ] New helper behavior has focused unit tests where possible.
* [ ] Android lint/build checks pass for Compose changes.
* [ ] `CHANGELOG.md` is updated for visible product behavior changes.

## Definition of Done

* PRD confirmed by the user.
* Implementation follows Trellis specs and product design context.
* Relevant tests and build/lint checks pass.
* Durable conventions are added to `.trellis/spec/` if new patterns emerge.
* Work is committed, task archived, and session journal recorded.

## Out of Scope (Temporary)

* Full TickTick clone.
* Tags/list folders and related Room migrations.
* Natural-language task parsing.
* Pomodoro/focus timer.
* Habit tracking.
* Android home-screen widget.
* Collaboration, sync accounts, or cloud task sync beyond existing backup.
* Calendar view replacement for the Android system calendar.

## Decision (ADR-lite)

**Context**: The user wants TickTick-inspired product and UI improvements, but
the app already has reminders, repeat rules, subtasks, completion notes, and a
four-quadrant model. A large data-model expansion would increase migration and
backup risk before the core browsing experience is improved.

**Decision**: Build the first iteration as a Home Command Center: smart views,
filter/search-style task discovery, and scannable task lists over the existing
active task data.

**Consequences**: This keeps the first pass lower-risk and visibly improves
daily use. Tags/lists, natural-language parsing, widgets, habits, and Pomodoro
remain future tasks.

## Technical Notes

* This is a complex product/UI task, so requirements need convergence before
  implementation.
* User confirmed the first version smart views: `四象限`, `今天`, `近期`,
  `收集箱`, plus search within the current view.
* Impeccable register: product. Scene: a single Android owner reviews and
  captures tasks throughout the day, often one-handed on a phone, needing fast
  scanning more than visual novelty.
* Potentially relevant files:
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskModels.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskRepository.kt`
  * `app/src/test/java/fun/han1997/dianmao/TimeFocusItemsTest.kt`
  * `DESIGN.md`
  * `PRODUCT.md`
  * `.trellis/spec/backend/compose-patterns.md`
  * `.trellis/spec/backend/quality-guidelines.md`
