package net.redstonecraft.redstonelauncher.profiles

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.redstonecraft.redstonelauncher.components.ExposedDropDownMenu
import java.net.URL
import kotlin.math.min

@Composable
fun FabricProfile(name: MutableState<String>) {
    val scope = rememberCoroutineScope()
    var name by name
    var snapshots by remember { mutableStateOf(false) }
    var unstableLoader by remember { mutableStateOf(false) }
    var gameVersions by remember { mutableStateOf(emptyList<FabricGameVersions.GameVersion>()) }
    var gameVersion by remember { mutableStateOf(0) }
    var loaderVersions by remember { mutableStateOf(emptyList<FabricLoaderVersions.LoaderVersion>()) }
    var loaderVersion by remember { mutableStateOf(0) }

    scope.launch {
        withContext(Dispatchers.IO) {
            gameVersions = getGameVersions()
        }
    }
    scope.launch {
        withContext(Dispatchers.IO) {
            loaderVersions = getLoaderVersions()
        }
    }

    Row {
        Checkbox(snapshots, {
            snapshots = it
            if (!it) {
                gameVersion = min(gameVersion, gameVersions.filter { i -> i.stable }.lastIndex)
            }
        })
        Text("Snapshots", Modifier.padding(top = 15.dp))
    }
    Row {
        ExposedDropDownMenu(gameVersions.let { if (!snapshots) it.filter { i -> i.stable } else it }.map { it.version }, gameVersion, { gameVersion = it })
        Text("Game Version", Modifier.padding(top = 15.dp, start = 10.dp))
    }
    Row {
        Checkbox(unstableLoader, {
            unstableLoader = it
            if (!it) {
                loaderVersion = min(loaderVersion, loaderVersions.filter { i -> i.stable }.lastIndex)
            }
        })
        Text("Unstable Loaders", Modifier.padding(top = 15.dp))
    }
    Row {
        ExposedDropDownMenu(loaderVersions.let { if (!unstableLoader) it.filter { i -> i.stable } else it }.map { it.version }, loaderVersion, { loaderVersion = it })
        Text("Loader Version", Modifier.padding(top = 15.dp, start = 10.dp))
    }
    Spacer(Modifier.height(20.dp))
    OutlinedButton({
    }) {
        Text("Add Profile")
    }
}

private suspend fun getGameVersions(): List<FabricGameVersions.GameVersion> {
    return try {
        val text = "{\"data\":${URL("https://meta.fabricmc.net/v2/versions/game").readText()}}"
        Json.decodeFromString<FabricGameVersions>(text).data
    } catch (_: Exception) {
        emptyList()
    }
}

private suspend fun getLoaderVersions(): List<FabricLoaderVersions.LoaderVersion> {
    return try {
        val text = "{\"data\":${URL("https://meta.fabricmc.net/v2/versions/loader").readText()}}"
        Json.decodeFromString<FabricLoaderVersions>(text).data
    } catch (_: Exception) {
        emptyList()
    }
}

@Serializable
data class FabricGameVersions(val data: List<GameVersion>) {

    @Serializable
    data class GameVersion(val version: String, val stable: Boolean = false)
}

@Serializable
data class FabricLoaderVersions(val data: List<LoaderVersion>) {

    @Serializable
    data class LoaderVersion(val separator: String, val build: Int, val maven: String, val version: String, val stable: Boolean = false)
}
