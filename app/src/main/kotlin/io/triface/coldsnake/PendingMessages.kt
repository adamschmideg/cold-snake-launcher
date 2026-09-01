package io.triface.coldsnake

import android.app.PendingIntent

/**
 * What's waiting in the current message batch, held in memory by
 * NotificationBlockerService while it snoozes each notification. Backs a
 * plain sender+snippet list (no scroll, no threads) so checking in doesn't
 * mean opening WhatsApp and getting pulled into everything else in it —
 * only the deliberate act of replying does that.
 */
object PendingMessages {
    data class Item(
        val key: String,
        val appLabel: String,
        val title: String,
        val text: String,
        val contentIntent: PendingIntent?,
    )

    private val items = mutableListOf<Item>()

    @Synchronized
    fun add(item: Item) {
        items.removeAll { it.key == item.key }
        items.add(item)
    }

    @Synchronized
    fun remove(key: String) {
        items.removeAll { it.key == key }
    }

    @Synchronized
    fun all(): List<Item> = items.toList()
}
