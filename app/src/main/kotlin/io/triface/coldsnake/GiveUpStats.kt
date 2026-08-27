package io.triface.coldsnake

import android.content.Context

/**
 * Tracks early exits from dumb-mode sessions: how many times the user gave
 * up, and how many seconds were skipped in total. No penalty is applied —
 * this is just data, for scoring/insight to be designed later.
 */
object GiveUpStats {
    private const val PREFS_NAME = "cold_snake_give_up_stats"
    private const val KEY_GIVE_UP_COUNT = "give_up_count"
    private const val KEY_TOTAL_SECONDS_SKIPPED = "total_seconds_skipped"

    fun recordGiveUp(context: Context, secondsSkipped: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_GIVE_UP_COUNT, 0)
        val totalSecondsSkipped = prefs.getLong(KEY_TOTAL_SECONDS_SKIPPED, 0L)
        prefs.edit()
            .putInt(KEY_GIVE_UP_COUNT, count + 1)
            .putLong(KEY_TOTAL_SECONDS_SKIPPED, totalSecondsSkipped + secondsSkipped)
            .apply()
    }

    fun giveUpCount(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_GIVE_UP_COUNT, 0)

    fun totalSecondsSkipped(context: Context): Long =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_TOTAL_SECONDS_SKIPPED, 0L)
}
