package net.redstonecraft.redstonelauncher.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import net.redstonecraft.redstonelauncher.Config
import net.redstonecraft.redstonelauncher.CustomIcons
import net.redstonecraft.redstonelauncher.Plugins
import net.redstonecraft.redstonelauncher.components.ExposedDropDownMenu
import net.redstonecraft.redstonelauncher.profiles.*
import org.jetbrains.skia.Image
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfilesPage() {
    val addingState = remember { mutableStateOf(Config.save.profiles.isEmpty()) }
    var adding by addingState
    if (adding) {
        AddProfilePage(addingState)
    } else {
        Scaffold(floatingActionButton = { FloatingActionButton(onClick = {
            adding = true
        }) { Icon(Icons.Filled.Add, "") } }) {
            LazyVerticalGrid(GridCells.Adaptive(110.dp), contentPadding = PaddingValues(5.dp)) {
                items(Config.save.profiles) {
                    Card(elevation = 3.dp, modifier = Modifier.padding(5.dp)) {
                        Column {
                            Icon(Image.makeFromEncoded(CustomIcons.fallbackProfile).toComposeImageBitmap(), it.id, modifier = Modifier.padding(10.dp).size(100.dp))
                            Text(it.id, modifier = Modifier.align(Alignment.CenterHorizontally))
                            Button(onClick = {}, modifier = Modifier.align(Alignment.CenterHorizontally), colors = LaunchColors) {
                                Text("Launch")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddProfilePage(addingState: MutableState<Boolean>) {
    var adding by addingState
    var nameS = remember { mutableStateOf("") }
    var name by nameS
    Row(Modifier.padding(10.dp)) {
        Button(onClick = {
            adding = false
        }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(red = 168, green = 199, blue = 250))) {
            Icon(Icons.Filled.KeyboardArrowLeft, "")
        }
        LazyColumn(Modifier.padding(10.dp, 0.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Adding a new profile", fontSize = 30.sp, modifier = Modifier.padding(10.dp))

                val platforms: List<Pair<String, @Composable (name: MutableState<String>) -> Unit>> = Profile.Platform.values().map { it.displayName to it.composeFunction } + Plugins.plugins.map { it.name to it.addProfile.callFunction }

                var index by remember { mutableStateOf(0) }

                ExposedDropDownMenu(platforms.map { it.first }, index, { index = it })

                OutlinedTextField(name, { name = it }, label = {
                    Text("Name")
                }, colors = TextFieldDefaults.textFieldColors(textColor = Color.White), singleLine = true)

                platforms[index].second(nameS)
            }
        }
    }
}

@Serializable
data class Profile(val id: String, val javaVersion: Int, val platform: Platform) {

    val path by lazy { File(File(Config.save.profilesPath), id) }

    enum class Platform(val displayName: String, val composeFunction: @Composable (name: MutableState<String>) -> Unit) {
        GITHUB("GitHub", { GitHubProfile(it) }),
        FABRIC("Fabric", { FabricProfile(it) }),
        FORGE("Forge", { ForgeProfile(it) }),
        VANILLA("Vanilla", { VanillaProfile(it) })
    }

}

object LaunchColors : ButtonColors {

    @Composable
    override fun backgroundColor(enabled: Boolean): State<Color> = mutableStateOf(if (enabled) Color(red = 55, green = 190, blue = 95) else Color(red = 109, green = 213, blue = 140))

    @Composable
    override fun contentColor(enabled: Boolean): State<Color> = mutableStateOf(Color(red = 255, green = 255, blue = 255))
}
