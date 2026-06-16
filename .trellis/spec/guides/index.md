# Thinking Guides

> Purpose: expand your thinking so easy-to-miss edge cases become explicit before coding.

---

## Why Thinking Guides?

Most bugs and avoidable rework come from missing one dimension of the problem:

- cross-layer flow was not traced end to end
- repeated logic drifted across files
- edge cases were not modeled up front
- future maintainers inherit hidden assumptions

Use these guides as short pre-flight checklists.

---

## Available Guides

| Guide | Purpose | When to Use |
|-------|---------|-------------|
| [Code Reuse Thinking Guide](./code-reuse-thinking-guide.md) | Identify patterns and reduce duplication | When you notice repeated patterns |
| [Cross-Layer Thinking Guide](./cross-layer-thinking-guide.md) | Think through data flow across layers | Features spanning multiple layers |
| [Overlay Navigation Thinking Guide](./overlay-navigation-thinking-guide.md) | Avoid wrong-page back behavior across stacked UI states | Screens with panels, sheets, dialogs, or in-page detail states |

---

## Quick Reference: Thinking Triggers

### When to Think About Cross-Layer Issues

- [ ] Feature touches 3+ layers (API, service, component, database)
- [ ] Data format changes between layers
- [ ] Multiple consumers need the same data
- [ ] You are not sure where some logic belongs

Read [Cross-Layer Thinking Guide](./cross-layer-thinking-guide.md)

### When to Think About Code Reuse

- [ ] You are writing similar code to something that exists
- [ ] You see the same pattern repeated 3+ times
- [ ] You are adding a new field to multiple places
- [ ] You are modifying any constant or config
- [ ] You are creating a new utility/helper function

Read [Code Reuse Thinking Guide](./code-reuse-thinking-guide.md)

### When to Think About Overlay Navigation

- [ ] More than one visible UI layer can respond to back
- [ ] A screen mixes overlay state and in-page detail state
- [ ] You are adding or changing `BackHandler`
- [ ] A bug report mentions returning to the wrong page or closing the underlying screen

Read [Overlay Navigation Thinking Guide](./overlay-navigation-thinking-guide.md)

---

## Pre-Modification Rule

Before changing any value or shared behavior, search first.

```bash
grep -r "value_to_change" .
```

This catches hidden coupling before it turns into a regression.

---

## How to Use This Directory

1. Before coding: skim the relevant guide.
2. During coding: if something feels repetitive or stateful, re-check the guide.
3. After a bug: add the new lesson so the next session starts with it.

---

**Core Principle**: 30 minutes of thinking saves hours of debugging.
