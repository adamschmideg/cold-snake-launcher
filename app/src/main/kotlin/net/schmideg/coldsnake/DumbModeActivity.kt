package net.schmideg.coldsnake

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.AlarmClock
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

const val EXTRA_DURATION_SECONDS = "duration_seconds"

/**
 * The restricted "dumb mode" screen: a fixed grid of essential apps plus
 * a countdown. The actual grayscale/notification restrictions from
 * Notes.md aren't built yet — this is just the app grid and the timer.
 */
class DumbModeActivity : AppCompatActivity() {

    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dumb_mode)

        findViewById<TextView>(R.id.appPhone).setOnClickListener {
            launchOrToast(Intent(Intent.ACTION_DIAL))
        }
        findViewById<TextView>(R.id.appMessages).setOnClickListener {
            launchOrToast(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING))
        }
        findViewById<TextView>(R.id.appCamera).setOnClickListener {
            launchOrToast(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }
        findViewById<TextView>(R.id.appClock).setOnClickListener {
            launchOrToast(Intent(AlarmClock.ACTION_SHOW_ALARMS))
        }
        findViewById<TextView>(R.id.appCalendar).setOnClickListener {
            launchOrToast(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR))
        }
        findViewById<TextView>(R.id.appMaps).setOnClickListener {
            launchOrToast(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0")))
        }

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

    private fun launchOrToast(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
