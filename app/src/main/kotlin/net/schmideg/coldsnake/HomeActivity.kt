package net.schmideg.coldsnake

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * The launcher's HOME screen. Registering this Activity with an ACTION_MAIN /
 * CATEGORY_HOME intent-filter is what lets the user set Cold Snake as their
 * default launcher.
 */
class HomeActivity : AppCompatActivity() {

    private var selectedDurationMinutes = 15
    private lateinit var durationButtons: Map<Button, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val duration1 = findViewById<Button>(R.id.duration1)
        val duration15 = findViewById<Button>(R.id.duration15)
        val duration30 = findViewById<Button>(R.id.duration30)
        val duration60 = findViewById<Button>(R.id.duration60)
        val startSession = findViewById<Button>(R.id.startSession)

        durationButtons = mapOf(
            duration1 to 1,
            duration15 to 15,
            duration30 to 30,
            duration60 to 60,
        )

        durationButtons.keys.forEach { button ->
            button.setOnClickListener {
                selectedDurationMinutes = durationButtons.getValue(button)
                updateDurationSelectionUi()
            }
        }
        updateDurationSelectionUi()

        startSession.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.session_started_toast, selectedDurationMinutes),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun updateDurationSelectionUi() {
        durationButtons.forEach { (button, minutes) ->
            val isSelected = minutes == selectedDurationMinutes
            val backgroundColorRes = if (isSelected) R.color.phosphor else R.color.teal
            val textColorRes = if (isSelected) R.color.ground else R.color.cold_white
            button.backgroundTintList = ContextCompat.getColorStateList(this, backgroundColorRes)
            button.setTextColor(ContextCompat.getColor(this, textColorRes))
        }
    }
}
