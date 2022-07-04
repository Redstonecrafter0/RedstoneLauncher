package net.redstonecraft.redstonelauncher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import net.redstonecraft.redstonelauncher.loadSvg
import java.awt.GraphicsEnvironment
import java.awt.Toolkit

@Composable
fun WindowScope.TitleBar(title: String, icon: Painter, windowState: WindowState, onExit: () -> Unit) {
    var size by remember { mutableStateOf(windowState.size) }
    var position by remember { mutableStateOf(windowState.position) }
    val maxDimensions = getEffectiveScreenSize()
    if (windowState.placement != WindowPlacement.Fullscreen) {
        WindowDraggableArea(Modifier.height(30.dp).fillMaxWidth().background(Color(0xFF121212))) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Row {
                    Icon(icon, "", Modifier.padding(5.dp), Color.Unspecified)
                    Text(title, Modifier.padding(5.dp, 8.dp, 5.dp, 5.dp), fontSize = 14.sp, color = Color.White)
                }
                Row {
                    TitleBarButton("icons/close.svg", "icons/closeHighlight.svg") {
                        windowState.isMinimized = true
                    }
                    if (windowState.size to windowState.position == maxDimensions) {
                        TitleBarButton("icons/min.svg", "icons/minHighlight.svg") {
                            windowState.size = size
                            windowState.position = position
                        }
                    } else {
                        TitleBarButton("icons/max.svg", "icons/maxHighlight.svg") {
                            size = windowState.size
                            position = windowState.position
                            val (maxSize, maxPosition) = maxDimensions
                            windowState.size = maxSize
                            windowState.position = maxPosition
                        }
                    }
                    TitleBarButton("icons/exit.svg", "icons/exitHighlight.svg", onExit)
                }
            }
        }
    }
}

@Composable
fun TitleBarButton(icon: String, iconHovered: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val iconL = remember { loadSvg(icon) }
    val iconHoveredL = remember { loadSvg(iconHovered) }
    IconButton(onClick, interactionSource = interactionSource) {
        Icon(if (isHovered) iconHoveredL else iconL, "Exit", tint = Color.Unspecified)
    }
}

private fun getEffectiveScreenSize(): Pair<DpSize, WindowPosition> {
    val gc = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
    val bounds = gc.bounds
    val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc)
    return DpSize((bounds.width - screenInsets.left - screenInsets.right).dp, (bounds.height - screenInsets.top - screenInsets.bottom).dp) to WindowPosition((bounds.x + screenInsets.left).dp, (bounds.y + screenInsets.top).dp)
}
