package io.triface.coldsnake

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
    private lateinit var startSession: Button
    private lateinit var cooldownText: TextView
    private var cooldownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val duration2s = findViewById<Button>(R.id.duration2s)
        val duration10s = findViewById<Button>(R.id.duration10s)
        val duration1m = findViewById<Button>(R.id.duration1m)
        startSession = findViewById(R.id.startSession)
        cooldownText = findViewById(R.id.cooldownText)

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
        updateCooldownUi()
    }

    override fun onPause() {
        cooldownTimer?.cancel()
        super.onPause()
    }

    private fun updateCooldownUi() {
        cooldownTimer?.cancel()
        val remainingSeconds = Cooldown.remainingSeconds(this)
        if (remainingSeconds <= 0) {
            startSession.isEnabled = true
            cooldownText.visibility = android.view.View.GONE
            return
        }

        startSession.isEnabled = false
        cooldownText.visibility = android.view.View.VISIBLE
        cooldownTimer = object : CountDownTimer(remainingSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000L).toInt() + 1
                cooldownText.text = getString(R.string.cooldown_format, secondsLeft)
            }

            override fun onFinish() {
                startSession.isEnabled = true
                cooldownText.visibility = android.view.View.GONE
            }
        }.start()
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
