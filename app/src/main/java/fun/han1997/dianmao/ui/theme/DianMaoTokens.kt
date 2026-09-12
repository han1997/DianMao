package `fun`.han1997.dianmao

/**
 * Four-level alpha scale for tonal text/surface treatments
 * (ui-polish tasks, 2026-09).
 *
 * Before these tokens, `copy(alpha = X)` appeared with ~15 distinct values
 * across the UI for the same visual intent. New code must use one of:
 *
 * - [subtle] 0.08 — faint backgrounds, pane tints, ghost borders
 * - [muted]  0.62 — icons, dividers, decorative fills (NOT readable text;
 *   0.62 onSurfaceVariant on the dark background is ~3.5:1, below WCAG 4.5)
 * - [mutedText] 0.75 — secondary body text, empty-state copy, quiet labels
 * - [strong] 0.78 — emphasized secondary text, active meta labels
 *
 * Values outside the scale (e.g. tonal chips built from
 * `surfaceVariant.copy(alpha = 0.26f)`) are left alone; only the
 * secondary-text / subtle-tint ranges were normalized.
 */
internal object DianMaoAlpha {
    const val subtle = 0.08f
    const val muted = 0.62f
    const val mutedText = 0.75f
    const val strong = 0.78f
}
