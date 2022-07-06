package net.redstonecraft.redstonelauncher.credentials

import com.microsoft.alm.secret.Credential
import com.microsoft.alm.storage.InsecureFileBackedCredentialStore

class FileCredentialsManager: CredentialsManager() {

    private val back = InsecureFileBackedCredentialStore()

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

    override fun isSecure() = false
}
