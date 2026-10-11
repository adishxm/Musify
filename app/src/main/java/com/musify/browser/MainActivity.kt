package com.musify.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.musify.browser.ui.screens.EngineSpikeScreen
import com.musify.browser.ui.screens.LandingScreen
import com.musify.browser.ui.theme.MusifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusifyTheme {
                var showSpike by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (showSpike) {
                        BackHandler {
                            showSpike = false
                        }
                        EngineSpikeScreen(modifier = Modifier.padding(innerPadding))
                    } else {
                        LandingScreen(
                            modifier = Modifier.padding(innerPadding),
                            onLaunchSpike = { showSpike = true }
                        )
                    }
                }
            }
        }
    }
}
