package com.musify.browser.engine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.musify.browser.ui.theme.Accent
import com.musify.browser.ui.theme.Ink
import com.musify.browser.ui.theme.Surface
import com.musify.browser.ui.theme.TextPrimary
import com.musify.browser.ui.theme.TextSecondary

@Composable
fun EngineView(
    adapter: EngineAdapter,
    modifier: Modifier = Modifier
) {
    val processState by adapter.processState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (processState.hasCrashed) {
            // Safe, non-looping Renderer Crash / Process Loss Recovery Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Ink)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Web Process Terminated",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = processState.crashReason ?: "The engine render process became unresponsive.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { adapter.recoverRenderer() },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Text(text = "Recover Tab & Reload", color = Ink)
                    }
                }
            }
        } else {
            val rememberedView = remember(adapter) {
                { context: android.content.Context ->
                    adapter.createView(context)
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = rememberedView
            )
        }
    }
}
