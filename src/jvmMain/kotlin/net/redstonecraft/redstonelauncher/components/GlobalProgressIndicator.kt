package net.redstonecraft.redstonelauncher.components

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.redstonecraft.redstonelauncher.DownloadManager

@Composable
fun GlobalProgressIndicator() {
    var value by remember { mutableStateOf(0F) }
    val scope = rememberCoroutineScope()
    scope.launch {
        while (true) {
            delay(50)
            value = DownloadManager.runningDownloads.values.filter { it != -1.0 }.average().toFloat()
        }
    }
    if (!value.isNaN()) {
        CircularProgressIndicator(value)
    }
}
