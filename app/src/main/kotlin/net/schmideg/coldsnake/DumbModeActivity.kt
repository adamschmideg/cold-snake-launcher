package net.schmideg.coldsnake

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

const val EXTRA_DURATION_SECONDS = "duration_seconds"

/**
 * The restricted "dumb mode" screen. For now it's just a countdown —
 * the actual fixed app grid / grayscale / notification restrictions
 * (see Notes.md) aren't built yet.
 */
class DumbModeActivity : AppCompatActivity() {

    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dumb_mode)

        val durationSeconds = intent.getIntExtra(EXTRA_DURATION_SECONDS, 60)
        val countdownText = findViewById<TextView>(R.id.countdownText)

        timer = object : CountDownTimer(durationSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000L).toInt() + 1
                countdownText.text = getString(R.string.countdown_format, secondsLeft)
            }

            override fun onFinish() {
                finishAffinity()
            }
        }.start()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
