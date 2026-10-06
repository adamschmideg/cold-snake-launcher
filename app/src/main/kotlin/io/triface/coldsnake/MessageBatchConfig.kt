package io.triface.coldsnake

import android.content.Context

/**
 * Apps the user has marked as "messages" — SMS, WhatsApp, Messenger, whatever
 * they use. Notifications from these apps are batched to a fixed rhythm
 * (see NotificationBlockerService) instead of arriving one at a time —
 * only while a dumb-mode session is active. No interval setting on
 * purpose: one less thing to fiddle with.
 */
object MessageBatchConfig {
    private const val PREFS_NAME = "cold_snake_message_batch"
    private const val KEY_PACKAGES = "message_packages"

    const val INTERVAL_MINUTES = 10
    const val INTERVAL_MILLIS = INTERVAL_MINUTES * 60 * 1000L

    fun messagePackages(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_PACKAGES, emptySet()) ?: emptySet()

    fun setMessagePackages(context: Context, packages: Set<String>) {
        prefs(context).edit().putStringSet(KEY_PACKAGES, packages).apply()
    }

    /** Millis until the next fixed wall-clock boundary, so messages arriving close together surface together. */
    fun millisUntilNextBoundary(nowMillis: Long = System.currentTimeMillis()): Long {
        val next = ((nowMillis / INTERVAL_MILLIS) + 1) * INTERVAL_MILLIS
        return next - nowMillis
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
