package com.musify.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.musify.browser.browser.SingleTabViewModel
import com.musify.browser.ui.screens.EngineSpikeScreen
import com.musify.browser.ui.screens.LandingScreen
import com.musify.browser.ui.screens.SingleTabBrowserScreen
import com.musify.browser.ui.theme.MusifyTheme

enum class ScreenState {
    BROWSER,
    LANDING,
    SPIKE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusifyTheme {
                var currentScreen by remember { mutableStateOf(ScreenState.BROWSER) }
                val browserViewModel = remember { SingleTabViewModel() }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        ScreenState.BROWSER -> {
                            SingleTabBrowserScreen(
                                viewModel = browserViewModel,
                                modifier = Modifier.padding(innerPadding),
                                onOpenSpikeDebug = { currentScreen = ScreenState.SPIKE }
                            )
                        }
                        ScreenState.LANDING -> {
                            BackHandler { currentScreen = ScreenState.BROWSER }
                            LandingScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLaunchSpike = { currentScreen = ScreenState.SPIKE }
                            )
                        }
                        ScreenState.SPIKE -> {
                            BackHandler { currentScreen = ScreenState.BROWSER }
                            EngineSpikeScreen(modifier = Modifier.padding(innerPadding))
                        }
                    }
                }
            }
        }
    }
}
