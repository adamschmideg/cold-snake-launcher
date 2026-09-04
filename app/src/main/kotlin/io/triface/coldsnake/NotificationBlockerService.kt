package io.triface.coldsnake

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Nothing here fires unless a dumb-mode session is active — Cold Snake must
 * not change phone behavior while it isn't running. Two reasons to hold a
 * notification back during a session, checked in order:
 *
 * 1. Apps not assigned to a custom grid slot: snooze until the session ends.
 * 2. Apps assigned to a slot AND marked as "messages": instead of letting
 *    every notification through immediately, snooze to the next fixed
 *    10-minute boundary and record it in PendingMessages, so the batch can
 *    be read as a plain sender+snippet list — checking in doesn't mean
 *    opening the real app and getting pulled into everything else in it.
 *    See MessageBatchConfig.
 *
 * Snoozing (not cancelling) means Android re-shows the original
 * notification on its own once the hold is over — nothing is lost, just
 * deferred.
 */
class NotificationBlockerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val remainingMillis = DumbModeState.remainingMillis() ?: return

        if (sbn.packageName !in GridConfig.assignedPackages(this)) {
            snoozeNotification(sbn.key, remainingMillis)
            return
        }

        if (sbn.packageName in MessageBatchConfig.messagePackages(this)) {
            recordPending(sbn)
            snoozeNotification(sbn.key, minOf(MessageBatchConfig.millisUntilNextBoundary(), remainingMillis))
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        PendingMessages.remove(sbn.key)
    }

    private fun recordPending(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val appLabel = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.packageName, 0)).toString()
        }.getOrDefault(sbn.packageName)

        PendingMessages.add(
            PendingMessages.Item(
                key = sbn.key,
                appLabel = appLabel,
                title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: appLabel,
                text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "",
                contentIntent = sbn.notification.contentIntent,
            ),
        )
    }
}
