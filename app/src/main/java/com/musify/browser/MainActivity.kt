package com.musify.browser

import android.content.Intent
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
import com.musify.browser.tabs.FileTabSessionStore
import com.musify.browser.tabs.TabsViewModel
import com.musify.browser.ui.screens.BrowserShellScreen
import com.musify.browser.ui.screens.EngineSpikeScreen
import com.musify.browser.ui.screens.LandingScreen
import com.musify.browser.ui.theme.MusifyTheme
import java.io.File

enum class ScreenState {
    BROWSER,
    LANDING,
    SPIKE
}

class MainActivity : ComponentActivity() {

    private var activeTabsViewModel: TabsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionFile = File(filesDir, "musify_tabs_session.json")
        val sessionStore = FileTabSessionStore(sessionFile)

        setContent {
            MusifyTheme {
                var currentScreen by remember { mutableStateOf(ScreenState.BROWSER) }
                val tabsViewModel = remember { TabsViewModel(store = sessionStore) }
                activeTabsViewModel = tabsViewModel

                androidx.compose.runtime.LaunchedEffect(intent) {
                    handleExternalIntent(intent, tabsViewModel)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        ScreenState.BROWSER -> {
                            BrowserShellScreen(
                                viewModel = tabsViewModel,
                                modifier = Modifier.padding(innerPadding),
                                onOpenSpikeDebug = { currentScreen = ScreenState.SPIKE },
                                onOpenRoadmap = { currentScreen = ScreenState.LANDING }
                            )
                        }
                        ScreenState.LANDING -> {
                            BackHandler { currentScreen = ScreenState.BROWSER }
                            LandingScreen(
                                modifier = Modifier.padding(innerPadding),
                                onOpenBrowser = { currentScreen = ScreenState.BROWSER },
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

    override fun onPause() {
        super.onPause()
        activeTabsViewModel?.persistSession()
    }

    override fun onStop() {
        super.onStop()
        activeTabsViewModel?.persistSession()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        activeTabsViewModel?.let { handleExternalIntent(intent, it) }
    }

    private fun handleExternalIntent(intent: Intent?, viewModel: TabsViewModel) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val dataStr = intent.dataString
            if (!dataStr.isNullOrBlank()) {
                viewModel.createNewTab(url = dataStr, selectImmediately = true)
            }
        }
    }
}
