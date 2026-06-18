# Fix quadrant detail header regressions

## Goal

Fix the quadrant detail header regressions introduced by the current `MainActivity.kt` UI refactor while keeping the quadrant detail screen stable on narrow devices and accessible for touch and screen-reader users.

## What I already know

* The current task scope is limited to `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`.
* The modified `QuadrantDetailHeader` replaced `IconButton` with a `Box(...).clickable`, reducing button semantics and shrinking the touch target to `36.dp`.
* The modified stats row replaced weighted summary pills with three inline stat items in a plain `Row`, which can overflow or compress badly on narrow screens or with larger font scales.
* The title row also lost the previous weighted layout, increasing truncation pressure for longer titles.
* `:app:compileDebugKotlin` currently passes, so the issue is a UI/interaction regression rather than a compile failure.

## Assumptions

* The recent visual refresh is intentional and should be preserved unless the user prefers a full revert.
* No behavior changes are needed outside the quadrant detail header and empty state presentation.

## Open Questions

* None.

## Requirements

* Restore a proper accessible back action with button semantics and at least a `48.dp` touch target.
* Make the header stats resilient on narrow widths and larger font scales.
* Keep the quadrant detail header visually coherent with the recent refresh unless the user explicitly asks for a revert.
* Avoid introducing new compile errors or changing unrelated task-detail behavior.
* Use `FlowRow` or an equivalent wrap-safe layout for the header stats.

## Acceptance Criteria

* [ ] The back action uses appropriate button semantics and a touch target that is at least `48.dp`.
* [ ] The stats area remains readable without obvious overflow or broken compression on narrow layouts.
* [ ] The header still compiles cleanly in `:app:compileDebugKotlin`.
* [ ] No unrelated behavior changes are introduced in the quadrant detail screen.
* [ ] The header stats wrap safely under narrow width or larger font scale conditions.

## Definition of Done

* Code updated in the affected Compose UI only
* Build verification run for the touched Android module
* Review confirms the identified regressions are addressed

## Out of Scope

* Redesigning the entire quadrant detail screen
* Refactoring unrelated task list or board components
* Broad typography or theming changes outside this header/empty-state area

## Technical Notes

* Relevant file: `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
* Relevant spec: `.trellis/spec/backend/compose-patterns.md`
* Current review findings:
  * Back button accessibility regression from `IconButton` to `Box.clickable`
  * Stats row responsiveness regression from weighted pills to plain inline row
  * Increased truncation risk in the title row
