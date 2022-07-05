package net.redstonecraft.redstonelauncher.credentials

import com.microsoft.alm.secret.Credential
import com.microsoft.alm.storage.SecretStore
import com.microsoft.alm.storage.StorageProvider

class GnomeCredentialsManager: CredentialsManager() {

    private val back: SecretStore<Credential> = StorageProvider.getCredentialStorage(true, StorageProvider.SecureOption.PREFER)

    override fun write(id: String, credentials: Credentials) {
        back.add(id, Credential(credentials.username, credentials.password))
    }

    override fun read(id: String): Credentials? {
        val credentials = back.get(id)
        return if (credentials == null) null else Credentials(credentials.Username, credentials.Password)
    }

    override fun remove(id: String) {
        back.delete(id)
    }
}
