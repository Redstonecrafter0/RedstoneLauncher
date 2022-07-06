package net.redstonecraft.redstonelauncher.credentials

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.kde.KWallet

class KWalletCredentialsManager: CredentialsManager() {

    companion object {
        private const val appId = "RedstoneLauncher"
        private const val folder = "accounts"
    }

    private val back = DBusConnectionBuilder.forSessionBus().withShared(false).build()
    private val service = back.getRemoteObject("org.kde.kwalletd5", "/modules/kwallet5d", KWallet::class.java)
    private val handle = service.open(appId, 0, appId)

    init {
        if (!service.hasFolder(handle, folder, appId)) {
            service.createFolder(handle, folder, appId)
        }
    }

    override fun write(id: String, credentials: Credentials) {
        service.writePassword(handle, folder, id, Json.encodeToString(credentials), appId)
    }

    override fun read(id: String): Credentials? {
        val password = service.readPassword(handle, folder, id, appId)
        return if (password == "") null else Json.decodeFromString<Credentials>(password)
    }

    override fun remove(id: String) {
        service.removeEntry(handle, folder, id, appId)
    }

    override fun isSecure() = true
}
