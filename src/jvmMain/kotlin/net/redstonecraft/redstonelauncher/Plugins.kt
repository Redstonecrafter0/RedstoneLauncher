package net.redstonecraft.redstonelauncher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentComposer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.jar.JarFile
import kotlin.reflect.full.declaredFunctions

object Plugins {

    val plugins = Config.installationDir
        .resolve("plugins")
        .listFiles { _, it -> it.endsWith(".jar") }!!
        .map { JarFile(it) }
        .map { Json.decodeFromString<Plugin>(it.getInputStream(it.getEntry("plugin.json")).readAllBytes().decodeToString()) }
}

@Serializable
data class Plugin(val name: String, val addProfile: AddProfile) {

    @Serializable
    data class AddProfile(val clazz: String, val function: String) {

        private val iClazz by lazy { Class.forName(clazz).kotlin }
        private val iObject by lazy { iClazz.objectInstance }
        private val iFunction by lazy { iClazz.declaredFunctions.first { it.name == function } }
        val callFunction: @Composable (name: MutableState<String>) -> Unit by lazy { { iFunction.call(iObject, it, currentComposer, 0) } }
    }
}
