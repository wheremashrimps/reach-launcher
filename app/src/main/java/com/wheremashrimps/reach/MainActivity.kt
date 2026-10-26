package com.wheremashrimps.reach

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.wheremashrimps.reach.ui.LauncherScreen
import com.wheremashrimps.reach.ui.LauncherViewModel
import com.wheremashrimps.reach.ui.ReachTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        // A launcher never closes on Back; Back only closes the drawer.
        onBackPressedDispatcher.addCallback(this) { viewModel.closeDrawer() }

        setContent {
            ReachTheme {
                LauncherScreen(viewModel)
            }
        }
    }

    /** Pressing Home while the launcher is already showing returns to the home page. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.closeDrawer()
    }
}
