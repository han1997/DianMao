# Design Prototypes

DianMao keeps browser-openable UI prototypes under `design/` so product and
visual changes can be reviewed before they reach Compose. These files are
**design artifacts, not runtime code** — they are never bundled into the APK and
must not be treated as a source of product behavior.

## Ownership

| Path | Purpose |
|---|---|
| `design/mockup/index.html` | Presentation shell (studio / phone stage / design notes) + inline SVG sprite |
| `design/mockup/style.css` | Token + component styles for the prototype |
| `design/mockup/app.js` | In-memory demo state, navigation, and interactions |
| `design/mockup/smoke-test.cjs` | Optional Playwright checks + screenshot generation |
| `DESIGN.md` | The canonical design system the prototype must mirror |

## Conventions

- **Open offline.** The prototype must work by opening `index.html` from disk:
  no CDN, no fonts, no network. Icons are an inline `<svg><symbol>` sprite.
- **Tokens come from the app, not the prototype.** Every color, radius, and font
  size is copied from `DESIGN.md` and
  `app/src/main/java/fun/han1997/dianmao/ui/theme/DianMaoTheme.kt` /
  `DianMaoTokens.kt`. When the theme changes, update the prototype in the same
  change; a prototype color that exists nowhere in the theme is a bug.
- **Prototype-only chrome is clearly separable.** The three-column
  studio / stage / notes shell (`.studio`, `.stage`, `.notes` in `style.css`) is
  preview furniture. Everything inside `.phone` is the app UI and follows the
  design system; the shell does not.
- **Demo data lives in one place.** `initial()` in `app.js` seeds all tasks,
  history, review stats, and backup state. Screens render from that state; do not
  hard-code task text in HTML.
- **Demo boundaries are documented.** The README and the notes column must state
  that no calendar/notification/network/disk access happens. The prototype is
  in-memory only and resets on reload.

## Common Mistake: Nested interactive HTML elements

**Symptom**: a screen renders the wrong number of components, click targets stop
working, or the browser silently restructures the DOM (e.g. the smoke test
reports fewer `.qpane` panes than expected).

**Cause**: putting a clickable element inside another clickable element — most
commonly `<button>` inside `<button>` (a completion toggle inside a quadrant
pane). HTML forbids interactive content nesting; the parser closes the outer
element early and the DOM no longer matches the template string.

**Fix**: make only one level interactive. Use a `div[data-action]` (with
`role="button"` where appropriate) for the outer container and reserve real
`<button>` elements for the inner controls, or render the inner mark as a
non-interactive `<span class="cm">` and let the outer handler own the click.

**Prevention**: the prototype audit script counts
`button button, button a, a button` and asserts it is zero, and checks each
screen for horizontal overflow and unnamed interactive controls.

## Verification

Run the optional smoke test when Playwright is available:

```
PLAYWRIGHT_MODULE="<path to playwright module>" node design/mockup/smoke-test.cjs
```

Assertion points: all nine screens render; smart-view switching, search
filtering, collapsible groups, form save, calendar month navigation, password
visibility, and archive restore work; light/dark themes both render; 390dp has no
horizontal overflow; and the page produces no errors.