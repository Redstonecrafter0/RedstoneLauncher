package net.redstonecraft.redstonelauncher.accounts

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.redstonecraft.redstonelauncher.Config
import net.redstonecraft.redstonelauncher.RedstoneLauncher
import net.redstonecraft.redstonelauncher.api.GitHubApi
import net.redstonecraft.redstonelauncher.credentials.Credentials
import net.redstonecraft.redstonelauncher.credentials.CredentialsManager
import net.redstonecraft.redstonelauncher.loadSvg
import java.awt.Desktop
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.Base64

@Composable
fun GitHubAccount(name: String, update: MutableState<Boolean>) {
    val interactionSource = MutableInteractionSource()
    val isHovered by interactionSource.collectIsHoveredAsState()
    var update by update
    Card(Modifier.fillMaxWidth(), elevation = 5.dp) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(loadSvg("icons/github.svg"), "", Modifier.padding(15.dp).size(50.dp))
                Text(name, fontSize = 20.sp)
            }
            Row {
                IconButton({
                    CredentialsManager.currentImpl.remove("redstonelauncher.github.${name}")
                    Config.save.githubAccounts -= name
                    Config.postModify()
                    update = true
                }, Modifier.padding(15.dp).size(50.dp), interactionSource = interactionSource) {
                    Icon(Icons.Outlined.Delete, "Delete", Modifier.size(30.dp), if (isHovered) Color.Red else Color.White)
                }
            }
        }
    }
}

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
        CredentialsManager.currentImpl.write("redstonelauncher.github.${account.first}", Credentials(account.first, account.second))
        Config.save.githubAccounts += account.first
        Config.postModify()
        adding = false
    }) {
        Text("Add Profile")
    }
}

fun randomString(size: Int): String {
    val bytes = ByteArray(size)
    SecureRandom.getInstanceStrong().nextBytes(bytes)
    return Base64.getUrlEncoder().encodeToString(bytes)
}

@Serializable
private data class GitHubAuth(val data: OAuth, val status: Int) {

    @Serializable
    data class OAuth(val access_token: String = "", val scope: String = "", val token_type: String = "")
}
