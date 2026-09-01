package io.triface.coldsnake

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Two independent reasons to hold a notification back, checked in order:
 *
 * 1. During a dumb-mode session: snooze everything until the session ends,
 *    except apps assigned to a custom grid slot (deliberately reachable).
 * 2. All the time, for apps the user has marked as "messages": snooze to
 *    the next fixed 10-minute boundary, so they arrive as an occasional
 *    calm batch instead of one at a time — see MessageBatchConfig.
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
            snoozeNotification(sbn.key, MessageBatchConfig.millisUntilNextBoundary())
        }
    }
}
