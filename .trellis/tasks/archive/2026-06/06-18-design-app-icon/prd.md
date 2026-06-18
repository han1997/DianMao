# design app icon

## Goal

Redesign the Android app launcher icon so it matches DianMao's calm, personal task-planning identity and reads clearly at small sizes.

## What I already know

* The app is a personal Android productivity tool for one owner.
* The brand direction is "Simple, fluid, polished" with restrained, native-feeling visuals.
* The current UI uses a warm primary accent (`#A94332`) and a blue secondary color for calendar/time semantics.
* The current launcher icon is the default Android starter icon and should be replaced.
* Existing adaptive icon resources live under `app/src/main/res/mipmap-*` and `app/src/main/res/drawable/`.

## Assumptions (temporary)

* The launcher icon should stay native and compact, not illustrative or mascot-like.
* The icon should work in both adaptive and round variants.
* The icon should avoid generic productivity clichés like a clipboard, checklist, or badge-heavy calendar.

## Open Questions

* Which icon direction should I build?

## Requirements

* Replace the default launcher icon with a custom icon.
* Keep the design readable at small sizes.
* Align the icon with the app's calm, practical identity.
* Preserve Android adaptive icon compatibility.

## Acceptance Criteria

* [ ] The launcher icon is no longer the default Android starter art.
* [ ] The new icon is visually coherent with DianMao's warm, restrained UI palette.
* [ ] The adaptive and round launcher assets both render correctly.

## Definition of Done

* Icon assets updated.
* App builds successfully.
* Visual result reviewed for small-size legibility.

## Out of Scope

* Full brand redesign.
* App-wide visual refactor.
* Marketing logo system.

## Technical Notes

* App theme colors currently lean warm red plus muted blue.
* Relevant files: `app/src/main/res/mipmap-anydpi/ic_launcher.xml`, `app/src/main/res/mipmap-anydpi/ic_launcher_round.xml`, `app/src/main/res/drawable/ic_launcher_background.xml`, `app/src/main/res/drawable/ic_launcher_foreground.xml`.
