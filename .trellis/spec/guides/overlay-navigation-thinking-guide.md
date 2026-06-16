# Overlay Navigation Thinking Guide

> Purpose: prevent back-navigation bugs when multiple UI states overlap.

## The Problem

Back handling becomes fragile when a screen combines:

- an underlying in-page state, such as a detail pane or nested view
- one or more overlays, such as sheets, panels, dialogs, or drawers

If each layer registers its own conditional back handler, behavior can depend on composition order or registration timing instead of the intended UI priority.

## Rules

### 1. Model Back Priority Explicitly

Represent back behavior as ordered state, not incidental handler order.

Good:

- derive a single back target from current visible states
- close the topmost visible layer first

Bad:

- assuming the last `BackHandler` in source or recomposition will always win
- spreading dismiss logic across unrelated composables with overlapping conditions

### 2. Prefer One Source of Truth for Dismissal

If the same overlay can be closed by:

- app bar back
- system back
- predictive back
- scrim tap
- save / cancel completion

then centralize the dismiss operation in one lambda or helper so all paths mutate the same state.

### 3. Write a Regression Test for Priority

For any bug fix involving layered back behavior, add a pure function or small state reducer that can be unit-tested.

Minimum cases:

- top overlay beats underlying detail state
- secondary overlay beats primary overlay
- no overlay falls through to the underlying page or system back

### 4. Propagate New Back Layers Through the Model

When a new sheet, panel, dialog, drawer, or in-page detail state can respond to
back, update the whole ordered model in the same change:

- add a distinct target to the central back-priority enum or state model
- pass the new visibility state into the resolver
- handle the new target in the single back dispatch path
- add or update priority tests that place the new layer above and below
  neighboring layers

Do not treat a local `BackHandler` as a small isolated fix once more than one
visible layer can consume back. That recreates the registration-order bug.

## Checklist

- [ ] What visible states can currently respond to back?
- [ ] What is the exact intended closing order?
- [ ] Is that order represented in data or only implied by composition?
- [ ] Do all dismiss entry points call the same state mutation path?
- [ ] Is there a unit test for the priority order?
- [ ] If a new back-capable layer was added, did the resolver, dispatch path,
      and tests all change together?

## Common Mistake

### Symptom

Closing an edit panel pops the underlying detail page, or closes the wrong layer first.

### Cause

Multiple conditional `BackHandler`s became enabled at the same time, and the actual consumer was determined by registration timing.

### Prevention

Compute the active back target explicitly, then use one handler to dispatch by priority.
When the visible layer set changes, let the resolver signature and tests force
call-site updates instead of adding another conditional handler.
