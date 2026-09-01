package io.triface.coldsnake

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Two independent reasons to hold a notification back, checked in order:
 *
 * 1. During a dumb-mode session: snooze everything until the session ends,
 *    except apps assigned to a custom grid slot (deliberately reachable).
 * 2. All the time, for apps the user has marked as "messages": snooze to
 *    the next fixed 10-minute boundary, and record it in PendingMessages
 *    so the batch can be read as a plain sender+snippet list — checking
 *    in doesn't mean opening the real app and getting pulled into
 *    everything else in it. See MessageBatchConfig.
 *
 * Snoozing (not cancelling) means Android re-shows the original
 * notification on its own once the hold is over — nothing is lost, just
 * deferred.
 */
class NotificationBlockerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val remainingMillis = DumbModeState.remainingMillis()
        if (remainingMillis != null && sbn.packageName !in GridConfig.assignedPackages(this)) {
            snoozeNotification(sbn.key, remainingMillis)
            return
        }

        if (sbn.packageName in MessageBatchConfig.messagePackages(this)) {
            recordPending(sbn)
            snoozeNotification(sbn.key, MessageBatchConfig.millisUntilNextBoundary())
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
