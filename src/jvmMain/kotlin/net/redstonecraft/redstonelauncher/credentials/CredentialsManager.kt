package net.redstonecraft.redstonelauncher.credentials

import kotlinx.serialization.Serializable

abstract class CredentialsManager {

    companion object {

        val currentImpl = run {
            val os = System.getProperty("os.name").lowercase()
            when {
                "win" in os -> WindowsCredentialsManager()
                "linux" in os -> when {
                    ProcessHandle.allProcesses().anyMatch { it.info().command().orElse(null) == "kwalletd" } -> KWalletCredentialsManager()
                    ProcessHandle.allProcesses().anyMatch { it.info().command().orElse(null) == "gnome-keyring-daemon" } -> GnomeCredentialsManager()
                    else -> FileCredentialsManager()
                }
                else -> error("Unknown system")
            }
        }
    }

    abstract fun write(id: String, credentials: Credentials)
    abstract fun read(id: String): Credentials?
    abstract fun remove(id: String)

    operator fun set(id: String, credentials: Credentials) = write(id, credentials)
    operator fun get(id: String) = read(id)
}

@Serializable
data class Credentials(val username: String, val password: String)
