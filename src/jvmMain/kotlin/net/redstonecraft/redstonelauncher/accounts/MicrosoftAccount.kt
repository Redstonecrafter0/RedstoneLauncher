package net.redstonecraft.redstonelauncher.accounts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun AddMicrosoftAccount(addingState: MutableState<Boolean>) {
    var adding by addingState
}
