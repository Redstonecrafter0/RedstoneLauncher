package net.redstonecraft.redstonelauncher.credentials

import kotlinx.serialization.Serializable
import org.freedesktop.dbus.messages.MethodCall

abstract class CredentialsManager {

    companion object {

        val currentImpl = run {
            val os = System.getProperty("os.name").lowercase()
            when {
                "win" in os -> WindowsCredentialsManager()
                "linux" in os -> {
                    MethodCall.setDefaultTimeout(9000000)
                    when {
                        ProcessHandle.allProcesses().anyMatch { it.info().command().orElse(null)?.split(" ")?.first()?.split("/")?.last() == "kwalletd5" } -> KWalletCredentialsManager()
                        ProcessHandle.allProcesses().anyMatch { it.info().command().orElse(null)?.split(" ")?.first()?.split("/")?.last() == "gnome-keyring-daemon" } -> GnomeCredentialsManager()
                        else -> FileCredentialsManager()
                    }
                }
                else -> error("Unknown system")
            }
        }
    }

    abstract fun write(id: String, credentials: Credentials)
    abstract fun read(id: String): Credentials?
    abstract fun remove(id: String)
    abstract fun isSecure(): Boolean

    operator fun set(id: String, credentials: Credentials) = write(id, credentials)
    operator fun get(id: String) = read(id)
}

@Serializable
data class Credentials(val username: String, val password: String)
