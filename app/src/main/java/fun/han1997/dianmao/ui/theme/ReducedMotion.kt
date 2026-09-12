package `fun`.han1997.dianmao

import android.provider.Settings
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Global reduced-motion contract (ui-polish task, 2026-09).
 *
 * When the user has disabled animations system-wide
 * (`Settings.Global.ANIMATOR_DURATION_SCALE == 0f`), every decorative
 * animation in the app must jump straight to its end state instead of
 * playing an intermediate motion. `DianMaoTheme` provides the flag via
 * [LocalAnimationsEnabled]; call sites consume it through
 * [motionTween] / [withReducedMotion] instead of branching themselves.
 */
internal val LocalAnimationsEnabled = compositionLocalOf { true }

@Composable
internal fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return rememberMotionScale(context)
}

@Composable
private fun rememberMotionScale(context: android.content.Context): Boolean {
    return androidx.compose.runtime.remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) != 0f
    }
}

/**
 * Returns a [tween] spec with the given duration, or an instant [snap]
 * when system animations are disabled.
 */
@Composable
internal fun <T> motionTween(durationMillis: Int): AnimationSpec<T> {
    return if (LocalAnimationsEnabled.current) {
        tween(durationMillis)
    } else {
        snap()
    }
}

/** Collapses an enter transition to nothing when animations are disabled. */
@Composable
internal fun EnterTransition.withReducedMotion(): EnterTransition {
    return if (LocalAnimationsEnabled.current) this else EnterTransition.None
}

/** Collapses an exit transition to nothing when animations are disabled. */
@Composable
internal fun ExitTransition.withReducedMotion(): ExitTransition {
    return if (LocalAnimationsEnabled.current) this else ExitTransition.None
}
