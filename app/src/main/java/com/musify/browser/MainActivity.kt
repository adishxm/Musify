package com.musify.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.musify.browser.ui.screens.LandingScreen
import com.musify.browser.ui.theme.MusifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusifyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LandingScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
