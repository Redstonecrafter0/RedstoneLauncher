package net.redstonecraft.redstonelauncher.accounts

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.redstonecraft.redstonelauncher.RedstoneLauncher
import net.redstonecraft.redstonelauncher.api.GitHubApi
import java.awt.Desktop
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.Base64

@Composable
fun AddGitHubAccount(addingState: MutableState<Boolean>) {
    var adding by addingState
    val state by remember { mutableStateOf(randomString(64)) }
    var account by remember { mutableStateOf("" to "") }
    val scope = rememberCoroutineScope()

    RedstoneLauncher.githubAuthCallback = {
        if ("code" in it && it["state"] == state) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val response = Json.decodeFromString<GitHubAuth>(URL("https://redstone-launcher-github-auth-api.vercel.app/api/auth?code=${URLEncoder.encode(it["code"], Charsets.UTF_8)}").readText())
                        if (response.status == 200) {
                            val token = response.data.access_token
                            val username = GitHubApi.getCurrentUser(token).login
                            account = username to token
                        }
                    } catch (_: Throwable) {
                    }
                }
            }
        }
    }

    OutlinedTextField(account.first, {}, readOnly = true, singleLine = true, label = { Text("Username") }, colors = TextFieldDefaults.textFieldColors(textColor = Color.White))
    Spacer(Modifier.height(20.dp))
    OutlinedButton({
        Desktop.getDesktop().browse(URI.create("https://github.com/login/oauth/authorize?client_id=c142b194f00f7aeee6ba&scope=repo&state=${state}"))
    }) {
        Text("Choose Account")
    }
    Spacer(Modifier.height(20.dp))
    OutlinedButton({
    }) {
        Text("Add Profile")
    }
}

private fun randomString(size: Int): String {
    val bytes = ByteArray(size)
    SecureRandom.getInstanceStrong().nextBytes(bytes)
    return Base64.getUrlEncoder().encodeToString(bytes)
}

@Serializable
private data class GitHubAuth(val data: OAuth, val status: Int) {

    @Serializable
    data class OAuth(val access_token: String = "", val scope: String = "", val token_type: String = "")
}
