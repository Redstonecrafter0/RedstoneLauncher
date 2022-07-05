package net.redstonecraft.redstonelauncher.accounts

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.net.URI

@Composable
fun AddGitHubAccount(addingState: MutableState<Boolean>) {
    var adding by addingState
    var username by remember { mutableStateOf("") }

    OutlinedTextField(username, {}, readOnly = true, singleLine = true, label = { Text("Username") })
    Spacer(Modifier.height(20.dp))
    OutlinedButton({
        Desktop.getDesktop().browse(URI.create("https://github.com/login/oauth/authorize?client_id=c142b194f00f7aeee6ba&scope=repo"))
    }) {
        Text("Choose Account")
    }
    Spacer(Modifier.height(20.dp))
    OutlinedButton({
    }) {
        Text("Add Profile")
    }
}
