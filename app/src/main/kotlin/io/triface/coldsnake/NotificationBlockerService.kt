package io.triface.coldsnake

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * While a dumb-mode session is active, snoozes every incoming notification
 * for the time remaining in the session. Snoozing (not cancelling) means
 * Android re-shows the original notification on its own once the session
 * ends — nothing is lost, just deferred.
 */
class NotificationBlockerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val remainingMillis = DumbModeState.remainingMillis() ?: return
        snoozeNotification(sbn.key, remainingMillis)
    }
}
