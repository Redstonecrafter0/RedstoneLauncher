package net.redstonecraft.redstonelauncher

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.ui.zIndex
import net.redstonecraft.redstonelauncher.components.TitleBar
import net.redstonecraft.redstonelauncher.pages.*
import androidx.compose.material.MaterialTheme as MaterialTheme2

@Composable
fun App() {
    var page by remember { mutableStateOf(0) }

    MaterialTheme(Settings.theme) {
        MaterialTheme2(Settings.theme2) {
            Surface(Modifier.fillMaxSize()) {
                Row {
                    NavigationRail(modifier = Modifier.shadow(20.dp).zIndex(1000F)) {
                        listOf(
                            "Profiles" to Icons.Outlined.SportsEsports, "Java" to Icons.Outlined.Code,
                            "Settings" to Icons.Outlined.Settings, "Accounts" to Icons.Outlined.ManageAccounts
                        ).forEachIndexed { index, (label, icon) ->
                            NavigationRailItem(index == page, onClick = { page = index }, icon = { Icon(icon, label) },
                                label = { Text(label) })
                        }
                    }
                    when (page) {
                        0 -> ProfilesPage()
                        1 -> JavaPage()
                        2 -> SettingsPage()
                        3 -> AccountSettingsPage()
                    }
                }
            }
        }
    }
}

fun main() = application {
    var isOpen by remember { mutableStateOf(true) }
    val trayState = rememberTrayState()

    Tray(
        icon = loadSvg("icon.svg"),
        state = trayState,
        tooltip = "RedstoneLauncher",
        onAction = {
            isOpen = true
        }
    ) {
        if (!isOpen) {
            Item("Show") {
                isOpen = true
            }
        }
        Item("Exit", onClick = ::exitApplication)
    }

    val windowState = WindowState(position = WindowPosition.Aligned(Alignment.Center))

    if (isOpen) {
        Window(
            state = windowState,
            onCloseRequest = { if (Config.save.closeOnExit) exitApplication() else isOpen = false },
            title = "RedstoneLauncher",
            icon = loadSvg("icon.svg"),
            undecorated = true
        ) {
          Column {
                TitleBar("RedstoneLauncher", loadSvg("icon.svg"), windowState) {
                    if (Config.save.closeOnExit) exitApplication() else isOpen = false
                }
                App()
            }
        }
    }
}

fun loadSvg(resource: String) = loadSvgPainter(Config::class.java.getResourceAsStream("/${resource}")!!, Density(1F))
