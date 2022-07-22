package net.redstonecraft.redstonelauncher.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ObservedLinearProgressIndicator(provider: () -> Double?) {
    var value: Double? by remember { mutableStateOf(.0) }
    val scope = rememberCoroutineScope()
    scope.launch {
        while (true) {
            delay(50)
            value = provider()
        }
    }
    if (value == -1.0) {
        LinearProgressIndicator(Modifier.padding(10.dp, 15.dp).fillMaxWidth())
    } else {
        LinearProgressIndicator(value?.toFloat() ?: 0F, Modifier.padding(10.dp, 15.dp).fillMaxWidth())
    }
}
