package net.redstonecraft.redstonelauncher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentComposer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.reflect.full.declaredFunctions

object Plugins {

    val plugins = Config.installationDir
        .resolve("plugins")
        .listFiles { _, it -> it.endsWith(".jar") }!!
        .mapNotNull {
            try {
                val jar = JarFile(it)
                Json.decodeFromString<Plugin>(jar.getInputStream(jar.getEntry("plugin.json")).readAllBytes().decodeToString())
            } catch (_: Throwable) {
                null
            }
        }
}

@Serializable
data class Plugin(val name: String, val addProfile: AddProfile) {

    companion object {

        val classLoader = URLClassLoader(Config.installationDir.resolve("plugins").listFiles { _, it -> it.endsWith(".jar") }!!.map { it.toURI().toURL() }.toTypedArray(), ClassLoader.getSystemClassLoader())
    }

    @Serializable
    data class AddProfile(val clazz: String, val function: String) {

        private val iClazz by lazy { Class.forName(clazz, true, classLoader).kotlin }
        private val iObject by lazy { iClazz.objectInstance }
        private val iFunction by lazy { iClazz.declaredFunctions.first { it.name == function } }
        val callFunction: @Composable (name: MutableState<String>) -> Unit by lazy { { iFunction.call(iObject, it, currentComposer, 0) } }
    }
}
