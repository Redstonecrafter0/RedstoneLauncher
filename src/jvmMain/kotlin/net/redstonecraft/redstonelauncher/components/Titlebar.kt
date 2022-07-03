package net.redstonecraft.redstonelauncher.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun Titlebar(title: String, icon: Painter) {
    Surface() {
        Row {
            Icon(icon, "")
            Text(title)
            // TODO: titlebar
        }
    }
}
