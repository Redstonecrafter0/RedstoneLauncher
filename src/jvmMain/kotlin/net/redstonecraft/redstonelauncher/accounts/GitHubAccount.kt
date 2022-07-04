package net.redstonecraft.redstonelauncher.accounts

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddGitHubAccount(addingState: MutableState<Boolean>) {
    var adding by addingState
    var username by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    Spacer(Modifier.height(20.dp))
    OutlinedButton({
    }) {
        Text("Add Profile")
    }
}
