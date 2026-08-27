package io.triface.coldsnake

import android.content.Context

/**
 * Tracks the early-exit cooldown: giving up on a session locks out starting
 * a new one for a period scaled to how much time was skipped.
 */
object Cooldown {
    private const val PREFS_NAME = "cold_snake_cooldown"
    private const val KEY_COOLDOWN_UNTIL_MILLIS = "cooldown_until_millis"

    // Penalty multiplier applied to the time skipped by exiting early.
    private const val PENALTY_MULTIPLIER = 2

    fun startCooldown(context: Context, remainingSeconds: Int) {
        val cooldownSeconds = remainingSeconds * PENALTY_MULTIPLIER
        val cooldownUntilMillis = System.currentTimeMillis() + cooldownSeconds * 1000L
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_COOLDOWN_UNTIL_MILLIS, cooldownUntilMillis)
            .apply()
    }

    /** Seconds remaining in the cooldown, or 0 if none is active. */
    fun remainingSeconds(context: Context): Int {
        val cooldownUntilMillis = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_COOLDOWN_UNTIL_MILLIS, 0L)
        val remainingMillis = cooldownUntilMillis - System.currentTimeMillis()
        return if (remainingMillis > 0) ((remainingMillis + 999) / 1000L).toInt() else 0
    }
}
