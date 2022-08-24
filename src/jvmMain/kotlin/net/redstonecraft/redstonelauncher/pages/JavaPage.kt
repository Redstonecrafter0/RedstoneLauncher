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
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.redstonecraft.redstonelauncher.Config
import net.redstonecraft.redstonelauncher.CustomIcons
import net.redstonecraft.redstonelauncher.DownloadManager
import net.redstonecraft.redstonelauncher.api.AdoptiumAPI
import net.redstonecraft.redstonelauncher.api.JavaPackage
import net.redstonecraft.redstonelauncher.api.OS
import net.redstonecraft.redstonelauncher.api.ZuluAPI
import net.redstonecraft.redstonelauncher.components.ExposedDropDownMenu
import net.redstonecraft.redstonelauncher.components.ObservedLinearProgressIndicator
import net.redstonecraft.redstonelauncher.unpackToTempDir
import org.apache.commons.io.FileUtils
import org.jetbrains.skia.Image
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun JavaPage() {
    val addingState = remember { mutableStateOf(Config.JavaInstall.javaInstalls.isEmpty()) }
    val scope = rememberCoroutineScope()
    @Suppress("RemoveExplicitTypeArguments")
    var javaVersions by remember { mutableStateOf(mapOf(
        "Temurin" to (emptyMap<Int, JavaPackage>() to emptyMap<Int, JavaPackage>()),
        "Zulu" to (emptyMap<Int, JavaPackage>() to emptyMap<Int, JavaPackage>())
    )) }
    scope.launch {
        withContext(Dispatchers.IO) {
            javaVersions = mapOf(
                "Temurin" to AdoptiumAPI.getAllVersions(),
                "Zulu" to ZuluAPI.getAllVersions()
            )
        }
    }
    var adding by addingState
    if (adding) {
        AddJavaPage(addingState, javaVersions)
    } else {
        var updateState = remember { mutableStateOf(false) }
        Scaffold(floatingActionButton = { FloatingActionButton(onClick = {
            adding = true
        }) { Icon(Icons.Filled.Add, "") } }) {
            if (updateState.value) {
                updateState.value = false
            } else {
                LazyVerticalGrid(GridCells.Adaptive(150.dp), contentPadding = PaddingValues(5.dp)) {
                    items(Config.JavaInstall.javaInstalls) {
                        JavaCard(it,
                            it.version.isOlderThan(
                                javaVersions[it.name]
                                    ?.let { i -> if (it.jdk) i.second else i.first }
                                    ?.get(it.version[0])?.version
                            ),
                            updateState
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JavaCard(javaInstall: Config.JavaInstall, hasUpdate: Boolean, updateState: MutableState<Boolean>) {
    Card(elevation = 3.dp, modifier = Modifier.padding(5.dp)) {
        Column(Modifier.padding(10.dp), Arrangement.spacedBy(10.dp), Alignment.CenterHorizontally) {
            Icon(Image.makeFromEncoded(if (javaInstall.icon != null) javaInstall.icon.readBytes() else CustomIcons.fallbackProfile).toComposeImageBitmap(), javaInstall.name, modifier = Modifier.size(100.dp))
            Text("${if (javaInstall.jdk) "JDK" else "JRE"} ${javaInstall.version[0]}\n${javaInstall.name}", textAlign = TextAlign.Center)
            val scope = rememberCoroutineScope()
            var installing by remember { mutableStateOf(javaInstall.installing) }
            scope.launch {
                while (true) {
                    delay(50)
                    installing = javaInstall.installing
                }
            }
            if (installing) {
                ObservedLinearProgressIndicator { DownloadManager.runningDownloads[javaInstall.dir.absolutePath] }
            } else if (hasUpdate) {
                Button(onClick = {}, modifier = Modifier.align(Alignment.CenterHorizontally), colors = LaunchColors) {
                    Text("Update")
                }
            }
            Button(onClick = {
                Config.JavaInstall.javaInstalls -= javaInstall
                javaInstall.dir.deleteRecursively()
                updateState.value = true
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(red = 236, green = 146, blue = 142))) {
                Text("Remove")
            }
        }
    }
}

@Composable
fun AddJavaPage(addingState: MutableState<Boolean>, javaVersions: Map<String, Pair<Map<Int, JavaPackage>, Map<Int, JavaPackage>>>) {
    var adding by addingState
    val vendors = javaVersions.keys.toList()

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

                ExposedDropDownMenu(vendors, vendor, { vendor = it })

                Spacer(Modifier.height(5.dp))

                var jdk by remember { mutableStateOf(false) }

                var major by remember { mutableStateOf(0) }

                Row {
                    Text("JDK", Modifier.padding(12.5.dp))
                    Checkbox(jdk, { jdk = it })
                }

                Spacer(Modifier.height(5.dp))

                val javaMajorVersionMap = javaVersions[vendors[vendor]]!!.let { i -> if (jdk) i.second else i.first }

                ExposedDropDownMenu(javaMajorVersionMap.keys.sorted().map { it.toString() }, major, { major = it })

                Spacer(Modifier.height(10.dp))

                OutlinedButton({
                    val jdk = jdk
                    val vendors = vendors
                    val vendor = vendor
                    val major = major
                    val version = javaMajorVersionMap[javaMajorVersionMap.keys.sorted().toList()[major]]!!
                    val filename = version.filename.removeSuffix(".zip").removeSuffix(".tar.gz")
                    val rootDir = File(Config.save.javaPath, filename)
                    val javaInstall = Config.JavaInstall(
                        File(Config.save.javaPath).resolve(filename).let {
                            when (OS.current) {
                                OS.WINDOWS -> it.resolve("bin/java.exe")
                                OS.LINUX -> it.resolve("bin/java")
                            }
                        },
                        rootDir,
                        null,
                        vendors[vendor],
                        version.version,
                        jdk,
                        true
                    )
                    Config.JavaInstall.javaInstalls += javaInstall
                    DownloadManager.start(version.url, rootDir.absolutePath, onError = {
                        Config.JavaInstall.javaInstalls -= javaInstall
                    }) {
                        val dir = it.unpackToTempDir()
                        var rootBin: File? = dir.resolve("bin")
                        if (!rootBin!!.exists() || !rootBin.isFile) {
                            rootBin = dir.listFiles()?.find { i ->
                                val file = i.resolve("bin")
                                file.exists() && file.isDirectory
                            }
                        }
                        if (rootBin != null) {
                            FileUtils.moveDirectory(rootBin, rootDir)
                            rootDir.resolve("name.txt").writeText(vendors[vendor])
                            rootDir.resolve("version.txt").writeText(version.version.joinToString("."))
                            if (jdk) {
                                rootDir.resolve("jdk.txt").createNewFile()
                            }
                            javaInstall.installing = false
                        } else {
                            Config.JavaInstall.javaInstalls -= javaInstall
                        }
                        dir.deleteRecursively()
                    }
                    adding = false
                }) {
                    Text("Install")
                }
            }
        }
    }
}

private fun List<Int>.isOlderThan(version: List<Int>?): Boolean {
    if (version == null) {
        return false
    }
    for (i in 1..3) {
        if (this[i] < version[i]) {
            return true
        } else if (this[i] > version[i]) {
            return false
        }
    }
    return false
}
