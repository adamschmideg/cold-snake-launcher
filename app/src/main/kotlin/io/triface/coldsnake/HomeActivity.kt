package io.triface.coldsnake

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * The launcher's HOME screen. Registering this Activity with an ACTION_MAIN /
 * CATEGORY_HOME intent-filter is what lets the user set Cold Snake as their
 * default launcher.
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
    }

    override fun onResume() {
        super.onResume()
        updateStatsUi()
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
