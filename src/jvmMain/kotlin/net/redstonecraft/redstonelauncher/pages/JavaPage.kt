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
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.redstonecraft.redstonelauncher.Config
import net.redstonecraft.redstonelauncher.CustomIcons
import net.redstonecraft.redstonelauncher.Plugins
import net.redstonecraft.redstonelauncher.RedstoneLauncher
import net.redstonecraft.redstonelauncher.components.ExposedDropDownMenu
import org.jetbrains.skia.Image
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun JavaPage() {
    val addingState = remember { mutableStateOf(Config.JavaInstall.javaInstalls.isEmpty()) }
    var adding by addingState
    if (adding) {
        AddJavaPage(addingState)
    } else {
        Scaffold(floatingActionButton = { FloatingActionButton(onClick = {
            adding = true
        }) { Icon(Icons.Filled.Add, "") } }) {
            LazyVerticalGrid(GridCells.Adaptive(110.dp), contentPadding = PaddingValues(5.dp)) {
                items(Config.JavaInstall.javaInstalls) {
                    Card(elevation = 3.dp, modifier = Modifier.padding(5.dp)) {
                        Column {
                            Icon(Image.makeFromEncoded(if (it.icon != null) it.icon.readBytes() else CustomIcons.fallbackProfile).toComposeImageBitmap(), it.name, modifier = Modifier.padding(10.dp).size(100.dp))
                            Text("Java ${if (it.jdk) "JDK" else "JRE"} ${it.version} - ${it.name}", modifier = Modifier.align(Alignment.CenterHorizontally))
                            if (it.installing) {
                                LinearProgressIndicator(RedstoneLauncher.runningDownloads[it.dir.absolutePath]!!.toFloat(), Modifier.padding(10.dp, 15.dp).fillMaxWidth())
                            } else {
                                Button(onClick = {}, enabled = TODO("IF NEW VERSION AVAILABLE"), modifier = Modifier.align(Alignment.CenterHorizontally), colors = LaunchColors) {
                                    Text("Update")
                                }
                            }
                            Button(onClick = {}, modifier = Modifier.align(Alignment.CenterHorizontally), colors = ButtonDefaults.buttonColors(containerColor = Color(red = 236, green = 146, blue = 142))) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddJavaPage(addingState: MutableState<Boolean>) {
    var adding by addingState
    val scope = rememberCoroutineScope()

    Row(Modifier.padding(10.dp)) {
        Button(onClick = {
            adding = false
        }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(red = 168, green = 199, blue = 250))) {
            Icon(Icons.Filled.KeyboardArrowLeft, "")
        }
        LazyColumn(Modifier.padding(10.dp, 0.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Download Java", fontSize = 30.sp, modifier = Modifier.padding(10.dp))

                var vendor by remember { mutableStateOf(0) }

                ExposedDropDownMenu(listOf("Temurin", "Zulu"), vendor, { vendor = it })

                var jdk by remember { mutableStateOf(false) }

                Row {
                    Text("JDK")
                    Checkbox(jdk, { jdk = it })
                }
            }
        }
    }
}
