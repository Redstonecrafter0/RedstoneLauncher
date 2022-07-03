package net.redstonecraft.redstonelauncher.profiles

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GitHubProfile(name: MutableState<String>) {
    var url by remember { mutableStateOf("") }
    OutlinedTextField(url, { url = it }, label = {
        Text("Repository")
    }, colors = TextFieldDefaults.textFieldColors(textColor = Color.White), singleLine = true)
    Spacer(Modifier.height(20.dp))
    OutlinedButton({
    }) {
        Text("Add Profile")
    }
}
