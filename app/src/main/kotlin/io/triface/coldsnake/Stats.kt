package io.triface.coldsnake

import android.content.Context
import kotlin.math.max

/**
 * Tracks every dumb-mode session (completed or given up early) and derives
 * the two headline scores:
 *
 * - Ice Time: cumulative seconds survived, minus a flat per-session tax.
 *   The tax means many short sessions earn far less than one long session
 *   covering the same total time — rewarding length over frequency,
 *   without needing a superlinear/exponent formula.
 * - Discipline: percentage of sessions finished without giving up.
 */
object Stats {
    private const val PREFS_NAME = "cold_snake_stats"
    private const val KEY_COMPLETED_COUNT = "completed_count"
    private const val KEY_GIVEN_UP_COUNT = "given_up_count"
    private const val KEY_ICE_TIME_SECONDS = "ice_time_seconds"

    // Flat cost subtracted from every session's survived time before it
    // counts toward Ice Time. Short sessions net little or nothing; long
    // sessions barely notice it.
    const val SESSION_TAX_SECONDS = 30

    fun recordSession(context: Context, secondsSurvived: Int, completed: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val completedCount = prefs.getInt(KEY_COMPLETED_COUNT, 0)
        val givenUpCount = prefs.getInt(KEY_GIVEN_UP_COUNT, 0)
        val iceTimeSeconds = prefs.getLong(KEY_ICE_TIME_SECONDS, 0L)

        val taxedSeconds = max(0, secondsSurvived - SESSION_TAX_SECONDS)

        prefs.edit()
            .putInt(KEY_COMPLETED_COUNT, if (completed) completedCount + 1 else completedCount)
            .putInt(KEY_GIVEN_UP_COUNT, if (completed) givenUpCount else givenUpCount + 1)
            .putLong(KEY_ICE_TIME_SECONDS, iceTimeSeconds + taxedSeconds)
            .apply()
    }

    fun iceTimeSeconds(context: Context): Long =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_ICE_TIME_SECONDS, 0L)

    fun completedCount(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_COMPLETED_COUNT, 0)

    fun givenUpCount(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_GIVEN_UP_COUNT, 0)

    /** Percentage of sessions finished without giving up, or null if no sessions yet. */
    fun disciplinePercent(context: Context): Int? {
        val completed = completedCount(context)
        val givenUp = givenUpCount(context)
        val total = completed + givenUp
        if (total == 0) return null
        return (completed * 100) / total
    }
}
