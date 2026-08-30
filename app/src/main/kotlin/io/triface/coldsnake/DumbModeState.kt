package io.triface.coldsnake

/**
 * Shared session state that NotificationBlockerService reads to decide
 * whether to snooze incoming notifications, and for how long.
 */
object DumbModeState {
    @Volatile
    var sessionEndAtMillis: Long? = null
        private set

    // Set when DumbModeActivity is left (Home/Recents/back) without us having
    // started the leave ourselves, e.g. via an in-grid app launch. Read once
    // by whichever screen resumes next — DumbModeActivity itself if the same
    // task is still around, otherwise HomeActivity on its next launch.
    @Volatile
    private var interruptedRemainingMillis: Long? = null

    fun sessionStarted(durationSeconds: Int) {
        sessionEndAtMillis = System.currentTimeMillis() + durationSeconds * 1000L
        interruptedRemainingMillis = null
    }

    fun sessionEnded() {
        sessionEndAtMillis = null
        interruptedRemainingMillis = null
    }

    fun remainingMillis(): Long? {
        val endAt = sessionEndAtMillis ?: return null
        val remaining = endAt - System.currentTimeMillis()
        return if (remaining > 0) remaining else null
    }

    fun markInterrupted() {
        interruptedRemainingMillis = remainingMillis()
    }

    /** Returns and clears the pending interruption, if any — one-shot so it's only reported once. */
    fun consumeInterruption(): Long? {
        val value = interruptedRemainingMillis
        interruptedRemainingMillis = null
        return value
    }
}
