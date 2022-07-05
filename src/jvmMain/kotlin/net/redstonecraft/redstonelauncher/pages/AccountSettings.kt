package net.redstonecraft.redstonelauncher.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.redstonecraft.redstonelauncher.Config
import net.redstonecraft.redstonelauncher.accounts.AddGitHubAccount
import net.redstonecraft.redstonelauncher.accounts.AddMicrosoftAccount
import net.redstonecraft.redstonelauncher.accounts.GitHubAccount
import net.redstonecraft.redstonelauncher.components.ExposedDropDownMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsPage() {
    val addingState = remember { mutableStateOf(Config.save.githubAccounts.isEmpty()) }
    val updateState = remember { mutableStateOf(false) }
    var update by updateState
    var adding by addingState
    if (adding) {
        AddAccountPage(addingState)
    } else {
        if (update) {
            update = false
        } else {
            Scaffold(floatingActionButton = { FloatingActionButton(onClick = {
                adding = true
            }) { Icon(Icons.Filled.Add, "") } }) {
                LazyColumn(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(Config.save.githubAccounts.map { it to "gh" }) { (name, type) ->
                        when (type) {
                            "gh" -> GitHubAccount(name, updateState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddAccountPage(addingState: MutableState<Boolean>) {
    var adding by addingState
    Row(Modifier.padding(10.dp)) {
        Button(onClick = {
            adding = false
        }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(red = 168, green = 199, blue = 250))) {
            Icon(Icons.Filled.KeyboardArrowLeft, "")
        }
        LazyColumn(Modifier.padding(10.dp, 0.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                var index by remember { mutableStateOf(0) }
                ExposedDropDownMenu(listOf("Microsoft", "GitHub"), index, { index = it }, Modifier.width(200.dp))
                when (index) {
                    0 -> AddMicrosoftAccount(addingState)
                    1 -> AddGitHubAccount(addingState)
                }
            }
        }
    }
}
