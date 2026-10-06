package io.triface.coldsnake

import android.content.Context
import android.content.Intent

/**
 * Tells the optional Cold Snake Notifications companion app when a dumb-mode
 * session starts and ends, so it can hold notifications back for exactly that
 * window. Fire-and-forget: if the companion isn't installed, nothing happens.
 * Cold Snake itself never touches notifications, so it needs no special
 * permission.
 *
 * Both broadcasts go only to the companion package, and are guarded by a
 * signature permission both apps declare — granted silently when both are
 * signed with the same key, and keeping other apps from receiving or faking
 * session events. Keep this protocol in sync with the companion's
 * SessionEvents.
 */
object SessionEvents {
    const val COMPANION_PACKAGE = "io.triface.coldsnake.notifications"
    const val PERMISSION = "io.triface.coldsnake.permission.SESSION_EVENTS"

    const val ACTION_SESSION_STARTED = "io.triface.coldsnake.action.SESSION_STARTED"
    const val ACTION_SESSION_ENDED = "io.triface.coldsnake.action.SESSION_ENDED"

    /** Long: wall-clock time the session ends, so the companion can expire it even if SESSION_ENDED never arrives. */
    const val EXTRA_END_AT_MILLIS = "end_at_millis"

    /** String array: apps assigned to a custom grid slot, deliberately reachable during the session. */
    const val EXTRA_ALLOWED_PACKAGES = "allowed_packages"

    fun sessionStarted(context: Context, endAtMillis: Long) {
        send(
            context,
            Intent(ACTION_SESSION_STARTED)
                .putExtra(EXTRA_END_AT_MILLIS, endAtMillis)
                .putExtra(EXTRA_ALLOWED_PACKAGES, GridConfig.assignedPackages(context).toTypedArray()),
        )
    }

    fun sessionEnded(context: Context) {
        send(context, Intent(ACTION_SESSION_ENDED))
    }

    private fun send(context: Context, intent: Intent) {
        context.sendBroadcast(intent.setPackage(COMPANION_PACKAGE), PERMISSION)
    }
}
