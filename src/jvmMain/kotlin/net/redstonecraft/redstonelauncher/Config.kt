package net.redstonecraft.redstonelauncher

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.redstonecraft.redstonelauncher.pages.Profile
import java.io.File

@Serializable
data class Config(
    var profilesPath: String,
    val profiles: MutableList<Profile> = mutableListOf(),
    val githubAccounts: MutableList<String> = mutableListOf(),
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

        private val defaultConfig = Config(File(System.getProperty("user.home")).resolve("RedstoneLauncher/Profiles").absolutePath)

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

}
