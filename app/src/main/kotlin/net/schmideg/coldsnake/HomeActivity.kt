package net.schmideg.coldsnake

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * The launcher's HOME screen. Registering this Activity with an ACTION_MAIN /
 * CATEGORY_HOME intent-filter is what lets the user set Cold Snake as their
 * default launcher.
 */
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }
}
