package net.redstonecraft.redstonelauncher

import androidx.compose.runtime.Composer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.redstonecraft.redstonelauncher.api.OS
import net.redstonecraft.redstonelauncher.pages.Profile
import java.io.File

@Serializable
data class Config(
    var profilesPath: String = userHome.resolve("RedstoneLauncher/Profiles").absolutePath,
    var javaPath: String = userHome.resolve("RedstoneLauncher/Java").absolutePath,
    val profiles: MutableList<Profile> = mutableListOf(),
    val githubAccounts: MutableList<String> = mutableListOf(),
    val microsoftAccounts: MutableList<String> = mutableListOf(),
    var closeOnExit: Boolean = false
) {

    companion object {

        @OptIn(ExperimentalSerializationApi::class)
        private val json = Json {
            prettyPrint = true
            prettyPrintIndent = "  "
        }

        fun postModify(config: Config = save) {
            if (!configFile.exists() || !configFile.isFile) {
                configFile.parentFile.mkdirs()
                configFile.createNewFile()
            }
            configFile.writeText(json.encodeToString(config))
        }

        private val userHome = File(System.getProperty("user.home"))

        private val defaultConfig = Config()

        val configFile = File(System.getProperty("user.home")).resolve("RedstoneLauncher/config.json")

        val save = (if (configFile.exists() && configFile.isFile) try {
            Json.decodeFromString(configFile.readText())
        } catch (_: Throwable) {
            defaultConfig
        } else defaultConfig.also { postModify(it) })

        val installationDir = System.getenv("REDSTONELAUNCHER_TESTING_INSTALLATION_DIR")?.let { File(it) } ?: File(System.getProperty("java.home")).parentFile

        init {
            listOf(
                File(save.profilesPath),
                installationDir.resolve("plugins")
            ).forEach {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
        }
    }

    class JavaInstall(
        val javaProgram: File,
        val dir: File,
        val icon: File?,
        val name: String,
        val version: List<Int>,
        val jdk: Boolean,
        var installing: Boolean = false
    ) {

        companion object {
            val javaInstalls = File(save.javaPath)
                .listFiles()
                .let { File(save.javaPath).listFiles() }!!
                .filter { it.isDirectory }
                .mapNotNull { create(it) }
                .toMutableList()

            fun create(dir: File): JavaInstall? {
                try {
                    val javaProgram = when (OS.current) {
                        OS.WINDOWS -> dir.resolve("bin/java.exe")
                        OS.LINUX -> dir.resolve("bin/java")
                    }
                    val icon = dir.resolve("icon.png")
                        .let { if (!it.exists() || !it.isFile) null else it }
                    val name = dir.resolve("name.txt")
                        .let { if (it.exists() && it.isFile) it else null }?.readText()
                    val version = dir.resolve("version.txt")
                        .let { if (it.exists() && it.isFile) it else null }
                        ?.readText()
                        ?.split(".")
                        ?.mapNotNull { it.toIntOrNull() }
                        ?.let { if (it.size != 4) null else it }
                    val jdk = dir.resolve("jdk.txt").exists()
                    return if (javaProgram.exists()
                        && javaProgram.isFile
                        && name != null
                        && version != null) {
                        JavaInstall(javaProgram, dir, icon, name, version, jdk)
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    return null
                }
            }
        }
    }

}
