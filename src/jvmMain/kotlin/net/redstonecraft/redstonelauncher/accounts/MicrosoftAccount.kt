package net.redstonecraft.redstonelauncher.accounts

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.redstonecraft.redstonelauncher.Config
import net.redstonecraft.redstonelauncher.CustomIcons
import net.redstonecraft.redstonelauncher.api.MinecraftAPI
import net.redstonecraft.redstonelauncher.api.XBoxApi
import net.redstonecraft.redstonelauncher.credentials.Credentials
import net.redstonecraft.redstonelauncher.credentials.CredentialsManager
import net.redstonecraft.redstonelauncher.utils.toLongUUID
import net.redstonecraft.redstonelauncher.web.MSALogin
import org.jetbrains.skia.Image
import kotlin.concurrent.thread

@Composable
fun MicrosoftAccount(name: String, update: MutableState<Boolean>) {
    val info by remember { mutableStateOf(name.toLongUUID() to CredentialsManager.currentImpl.read("redstonelauncher.msa.$name")?.username) }
    val (uuid, username) = info
    if (username == null) {
        return
    }
    val interactionSource = MutableInteractionSource()
    val isHovered by interactionSource.collectIsHoveredAsState()
    var update by update
    Card(Modifier.fillMaxWidth(), elevation = 5.dp) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Image.makeFromEncoded(CustomIcons.minecraft).toComposeImageBitmap(), "", Modifier.padding(15.dp).size(50.dp), Color.Unspecified)
                Column {
                    Text(username, fontSize = 20.sp)
                    Spacer(Modifier.height(7.5.dp))
                    Text(uuid, fontSize = 15.sp)
                }
            }
            Row {
                IconButton({
                    CredentialsManager.currentImpl.remove("redstonelauncher.msa.$name")
                    Config.save.microsoftAccounts -= name
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
fun AddMicrosoftAccount(addingState: MutableState<Boolean>) {
    var adding by addingState
    var user by remember { mutableStateOf("" to "") }
    var msCred: MicrosoftCredentials? by remember { mutableStateOf(null) }
    var valid by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    OutlinedTextField(user.first, {}, readOnly = true, singleLine = true, label = { Text("Username") }, colors = TextFieldDefaults.textFieldColors(textColor = Color.White))
    OutlinedTextField(user.second.toLongUUID(), {}, readOnly = true, singleLine = true, label = { Text("UUID") }, colors = TextFieldDefaults.textFieldColors(textColor = Color.White))

    Spacer(Modifier.height(5.dp))

    OutlinedButton({
        thread {
            MSALogin { accessToken, refreshToken, validUntil ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        user = "" to ""
                        valid = false
                        loading = true
                        var creds = MicrosoftCredentials(accessToken, refreshToken, validUntil, "", 0)
                        val profile = try {
                            creds = XBoxApi.login(creds)
                            MinecraftAPI(creds.minecraftBearer).getProfile()!!
                        } catch (_: Throwable) {
                            loading = false
                            return@withContext
                        }
                        msCred = creds
                        user = profile.name to profile.id
                        valid = true
                        loading = false
                    }
                }
            }
        }
    }) {
        Text("Login")
    }
    OutlinedButton({
        CredentialsManager.currentImpl.write("redstonelauncher.msa.${user.second}", Credentials(user.first, Json.encodeToString(msCred)))
        Config.save.microsoftAccounts += user.second
        Config.postModify()
        adding = false
    }, enabled = valid && user.first != "") {
        Text("Add Profile")
    }

    if (!valid && !loading) {
        Card(Modifier.padding(5.dp), backgroundColor = Color(red = 228, green = 105, blue = 98)) {
            Text("This account does not own Minecraft.", Modifier.padding(20.dp))
        }
    }
    if (loading) {
        Card(Modifier.padding(5.dp), backgroundColor = Color(red = 228, green = 105, blue = 98)) {
            Text("Loading...", Modifier.padding(20.dp))
        }
    }
}

@Serializable
data class MicrosoftCredentials(val accessToken: String, val refreshToken: String, val validUntil: Long, val minecraftBearer: String, val minecraftBearerValidUntil: Long)
