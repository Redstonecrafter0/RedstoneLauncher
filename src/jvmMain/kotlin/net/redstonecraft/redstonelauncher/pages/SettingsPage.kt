package net.redstonecraft.redstonelauncher.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.redstonecraft.redstonelauncher.Config
import kotlin.reflect.KMutableProperty

@Suppress("UNCHECKED_CAST")
@Composable
fun SettingsPage() {
    var updateSettings by remember { mutableStateOf(false) }
    var settings = getSettings()
    if (updateSettings) {
        settings = getSettings()
        updateSettings = false
    }
    LazyColumn(Modifier.padding(25.dp, 5.dp, 5.dp, 5.dp)) {
        items(settings) {
            when (it.type) {
                SettingData.Type.TEXT -> TextSetting(it.name, it.state as MutableState<String>)
                SettingData.Type.SWITCH -> SwitchSetting(it.name, it.state as MutableState<Boolean>)
            }
        }
        item {
            Row {
                Button(onClick = {
                    settings.forEach {
                        if (it.state.value != it.oldValue) {
                            it.property.setter.call(it.state.value)
                        }
                    }
                    Config.postModify()
                    updateSettings = true
                }, colors = LaunchColors) {
                    Text("Save")
                }
                if (settings.any { it.oldValue != it.state.value }) {
                    Card(Modifier.padding(7.dp), elevation = 5.dp, backgroundColor = Color(red = 228, green = 105, blue = 98)) {
                        Text("There are unsaved changes.", Modifier.padding(8.dp), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun getSettings() = listOf(
    textSetting("Profiles Path", Config.save::profilesPath),
    textSetting("Java Path", Config.save::javaPath),
    switchSetting("Close on Exit", Config.save::closeOnExit)
)

@Composable
fun textSetting(name: String, property: KMutableProperty<String>): SettingData<String> {
    val value = property.getter.call()
    return SettingData(name, property, value, remember { mutableStateOf(value) }, SettingData.Type.TEXT)
}

@Composable
fun switchSetting(name: String, property: KMutableProperty<Boolean>): SettingData<Boolean> {
    val value = property.getter.call()
    return SettingData(name, property, value, remember { mutableStateOf(value) }, SettingData.Type.SWITCH)
}

class SettingData<T>(val name: String, val property: KMutableProperty<T>, val oldValue: T, val state: MutableState<T>, val type: Type) {

    enum class Type {
        TEXT, SWITCH
    }
}

@Composable
fun TextSetting(label: String, state: MutableState<String>) {
    var value by state
    Row(Modifier.padding(5.dp)) {
        Text(label, modifier = Modifier.width(200.dp).padding(top = 20.dp), fontSize = 15.sp)
        OutlinedTextField(value, { value = it }, colors = TextFieldDefaults.textFieldColors(textColor = Color(255, 255, 255)), singleLine = true)
    }
}

@Composable
fun SwitchSetting(label: String, state: MutableState<Boolean>) {
    var value by state
    Row(Modifier.padding(5.dp)) {
        Text(label, modifier = Modifier.width(200.dp).padding(top = 15.dp), fontSize = 15.sp)
        Checkbox(value, { value = it })
    }
}

object Settings {

    val theme = darkColorScheme()
    val theme2 = darkColors()

}
