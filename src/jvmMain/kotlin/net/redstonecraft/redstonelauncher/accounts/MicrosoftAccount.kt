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
import net.redstonecraft.redstonelauncher.credentials.CredentialsManager
import net.redstonecraft.redstonelauncher.web.MSALogin
import org.jetbrains.skia.Image
import java.net.URL
import kotlin.concurrent.thread

@Composable
fun MicrosoftAccount(name: String, update: MutableState<Boolean>) {
    val interactionSource = MutableInteractionSource()
    val isHovered by interactionSource.collectIsHoveredAsState()
    var update by update
    Card(Modifier.fillMaxWidth(), elevation = 5.dp) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Image.makeFromEncoded(CustomIcons.minecraft).toComposeImageBitmap(), "", Modifier.padding(15.dp).size(50.dp))
                Text(name, fontSize = 20.sp)
            }
            Row {
                IconButton({
                    CredentialsManager.currentImpl.remove("redstonelauncher.microsoft.${name}")
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
    val state by remember { mutableStateOf(randomString(64)) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    OutlinedTextField(username, {}, readOnly = true, singleLine = true, label = { Text("Username") }, colors = TextFieldDefaults.textFieldColors(textColor = Color.White))
    OutlinedButton({
        MSALogin { accessToken, refreshToken, userId, validUntil ->
//            scope.launch {
//                withContext(Dispatchers.IO) {
//                    val conn = URL("https://user.auth.xboxlive.com/user/authenticate").openConnection()
//                    conn.setRequestProperty("Content-Type", "application/json")
//                    conn.setRequestProperty("Accept", "application/json")
//                    conn.doInput = true
//                    conn.doOutput = true
//                    conn.outputStream.write(Json.encodeToString(
//                        XBoxLiveAuthRequest(
//                            XBoxLiveAuthRequest.PropertiesData("RPS", "user.auth.xboxlive.com", accessToken),
//                            "http://auth.xboxlive.com",
//                            "JWT"
//                        )
//                    ).encodeToByteArray())
//                    conn.outputStream.close()
//                }
//            }
        }
    }) {
        Text("Login")
    }
}

@Serializable
data class XBoxLiveAuthRequest(val Properties: PropertiesData, val RelyingParty: String, val TokenType: String){

    @Serializable
    data class PropertiesData(val AuthMethod: String, val SiteName: String, val RpsTicket: String)
}
