package io.triface.coldsnake

/**
 * Shared session state that NotificationBlockerService reads to decide
 * whether to snooze incoming notifications, and for how long.
 */
object DumbModeState {
    @Volatile
    var sessionEndAtMillis: Long? = null
        private set

    fun sessionStarted(durationSeconds: Int) {
        sessionEndAtMillis = System.currentTimeMillis() + durationSeconds * 1000L
    }

    fun sessionEnded() {
        sessionEndAtMillis = null
    }

    fun remainingMillis(): Long? {
        val endAt = sessionEndAtMillis ?: return null
        val remaining = endAt - System.currentTimeMillis()
        return if (remaining > 0) remaining else null
    }
}
