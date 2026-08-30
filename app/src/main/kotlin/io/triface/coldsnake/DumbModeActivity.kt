package io.triface.coldsnake

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.AlarmClock
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

const val EXTRA_DURATION_SECONDS = "duration_seconds"

/**
 * The restricted "dumb mode" screen: a fixed grid of essential apps plus
 * a countdown. The actual grayscale/notification restrictions from
 * Notes.md aren't built yet — this is just the app grid and the timer.
 */
class DumbModeActivity : AppCompatActivity() {

    private var timer: CountDownTimer? = null
    private var durationSeconds = 0
    private var secondsLeft = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dumb_mode)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                confirmGiveUp()
            }
        })

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
        setUpCustomSlotTile(R.id.appCustom1, slotIndex = 1)
        setUpCustomSlotTile(R.id.appCustom2, slotIndex = 2)
        setUpCustomSlotTile(R.id.appCustom3, slotIndex = 3)

        durationSeconds = intent.getIntExtra(EXTRA_DURATION_SECONDS, 60)
        val countdownText = findViewById<TextView>(R.id.countdownText)

        secondsLeft = durationSeconds
        DumbModeState.sessionStarted(durationSeconds)
        timer = object : CountDownTimer(durationSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                secondsLeft = (millisUntilFinished / 1000L).toInt() + 1
                countdownText.text = getString(R.string.countdown_format, secondsLeft)
            }

            override fun onFinish() {
                secondsLeft = 0
                Stats.recordSession(this@DumbModeActivity, durationSeconds, completed = true)
                DumbModeState.sessionEnded()
                stopLockTaskSafely()
                finishAffinity()
            }
        }.start()
    }

    private fun confirmGiveUp() {
        AlertDialog.Builder(this)
            .setTitle(R.string.give_up_title)
            .setMessage(R.string.give_up_message)
            .setPositiveButton(R.string.give_up_confirm) { _, _ ->
                val secondsSurvived = durationSeconds - secondsLeft
                Stats.recordSession(this, secondsSurvived, completed = false)
                DumbModeState.sessionEnded()
                stopLockTaskSafely()
                finishAffinity()
            }
            .setNegativeButton(R.string.give_up_cancel, null)
            .show()
    }

    private fun stopLockTaskSafely() {
        try {
            stopLockTask()
        } catch (e: IllegalArgumentException) {
            // Not in lock task mode (e.g. startLockTask failed earlier); nothing to stop.
        }
    }

    private fun setUpCustomSlotTile(viewId: Int, slotIndex: Int) {
        val tile = findViewById<TextView>(viewId)
        val assignedPackage = GridConfig.getSlotPackage(this, slotIndex)
        if (assignedPackage == null) {
            tile.text = ""
            tile.isClickable = false
            return
        }

        val label = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(assignedPackage, 0))
        }.getOrNull() ?: assignedPackage

        tile.text = label
        tile.setOnClickListener {
            val launchIntent = packageManager.getLaunchIntentForPackage(assignedPackage)
            if (launchIntent != null) {
                launchOrToast(launchIntent)
            } else {
                Toast.makeText(this, R.string.no_app_found, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchOrToast(intent: Intent) {
        // Screen pinning blocks launching any other app's activity, same as
        // it blocks Home/Recents — so unpin for this deliberate, in-grid
        // launch. onResume re-pins once we're back from it.
        stopLockTaskSafely()
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (DumbModeState.remainingMillis() != null) {
            try {
                startLockTask()
            } catch (e: IllegalArgumentException) {
                // Screen pinning unavailable on this device/OS version.
            }
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
