package io.triface.coldsnake

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * The app's main screen: pick a duration and start a dumb-mode session.
 * A normal launcher-icon app, not the system HOME app — DumbModeActivity's
 * screen pinning handles blocking, not default-launcher registration.
 */
class HomeActivity : AppCompatActivity() {

    private var selectedDurationSeconds = 10
    private lateinit var durationButtons: Map<Button, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val duration2s = findViewById<Button>(R.id.duration2s)
        val duration10s = findViewById<Button>(R.id.duration10s)
        val duration1m = findViewById<Button>(R.id.duration1m)
        val startSession = findViewById<Button>(R.id.startSession)

        durationButtons = mapOf(
            duration2s to 2,
            duration10s to 10,
            duration1m to 60,
        )

        durationButtons.keys.forEach { button ->
            button.setOnClickListener {
                selectedDurationSeconds = durationButtons.getValue(button)
                updateDurationSelectionUi()
            }
        }
        updateDurationSelectionUi()

        startSession.setOnClickListener {
            val intent = Intent(this, DumbModeActivity::class.java)
                .putExtra(EXTRA_DURATION_SECONDS, selectedDurationSeconds)
            startActivity(intent)
        }

        findViewById<Button>(R.id.customizeSlots).setOnClickListener {
            showSlotPicker()
        }
    }

    private fun showSlotPicker() {
        val unlockedCount = GridConfig.unlockedSlotCount(this)
        val labels = (1..GridConfig.SLOT_COUNT).map { slotIndex ->
            when {
                slotIndex > unlockedCount -> getString(
                    R.string.slot_locked_format,
                    slotIndex,
                    GridConfig.iceTimeThresholdMinutes(slotIndex),
                    GridConfig.disciplineThresholdPercent(),
                )
                else -> {
                    val assigned = GridConfig.getSlotPackage(this, slotIndex)
                    if (assigned == null) {
                        getString(R.string.slot_unlocked_unset_format, slotIndex)
                    } else {
                        val label = runCatching {
                            packageManager.getApplicationLabel(packageManager.getApplicationInfo(assigned, 0))
                        }.getOrNull() ?: assigned
                        getString(R.string.slot_unlocked_set_format, slotIndex, label)
                    }
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.customize_slots_title)
            .setItems(labels.toTypedArray()) { _, which ->
                val slotIndex = which + 1
                if (slotIndex <= unlockedCount) {
                    showAppPicker(slotIndex)
                }
            }
            .show()
    }

    private fun showAppPicker(slotIndex: Int) {
        val candidates = GridConfig.candidatesForSlot(this, slotIndex)
        if (candidates.isEmpty()) {
            AlertDialog.Builder(this)
                .setMessage(R.string.no_eligible_apps)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        val labels = candidates.map { packageManager.getApplicationLabel(it).toString() }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(R.string.customize_slots_title)
            .setItems(labels) { _, which ->
                GridConfig.setSlotPackage(this, slotIndex, candidates[which].packageName)
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateStatsUi()
        DumbModeState.consumeInterruption()?.let { remainingMillis ->
            val minutes = (remainingMillis / 1000 / 60).toInt()
            val seconds = (remainingMillis / 1000 % 60).toInt()
            Toast.makeText(
                this,
                getString(R.string.session_interrupted_format, minutes, seconds),
                Toast.LENGTH_LONG,
            ).show()
        }
        if (!isNotificationAccessGranted()) {
            promptForNotificationAccess()
        }
    }

    private fun isNotificationAccessGranted(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }

    private fun promptForNotificationAccess() {
        AlertDialog.Builder(this)
            .setTitle(R.string.notification_access_title)
            .setMessage(R.string.notification_access_message)
            .setPositiveButton(R.string.notification_access_open_settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            .setNegativeButton(R.string.notification_access_skip, null)
            .show()
    }

    private fun updateStatsUi() {
        val iceTimeSeconds = Stats.iceTimeSeconds(this)
        findViewById<TextView>(R.id.iceTimeValue).text = getString(
            R.string.ice_time_format,
            iceTimeSeconds / 60,
            iceTimeSeconds % 60,
        )

        val disciplinePercent = Stats.disciplinePercent(this)
        findViewById<TextView>(R.id.disciplineValue).text = if (disciplinePercent != null) {
            getString(R.string.discipline_format, disciplinePercent)
        } else {
            getString(R.string.discipline_placeholder)
        }
    }

    private fun updateDurationSelectionUi() {
        durationButtons.forEach { (button, seconds) ->
            val isSelected = seconds == selectedDurationSeconds
            val backgroundColorRes = if (isSelected) R.color.phosphor else R.color.teal
            val textColorRes = if (isSelected) R.color.ground else R.color.cold_white
            button.backgroundTintList = ContextCompat.getColorStateList(this, backgroundColorRes)
            button.setTextColor(ContextCompat.getColor(this, textColorRes))
        }
    }
}
