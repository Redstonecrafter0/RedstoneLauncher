package net.redstonecraft.redstonelauncher.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

@Composable
fun ExposedDropDownMenu(
    values: List<String>,
    selectedIndex: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier.width(IntrinsicSize.Min),
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    shape: Shape = androidx.compose.material.MaterialTheme.shapes.medium
) {
    var expanded by remember { mutableStateOf(false) }
    var textfieldSize by remember { mutableStateOf(Size.Zero) }

    val indicatorColor =
        if (expanded) androidx.compose.material.MaterialTheme.colors.primary.copy(alpha = ContentAlpha.high)
        else androidx.compose.material.MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.UnfocusedIndicatorLineOpacity)
    val indicatorWidth = (if (expanded) 2 else 1).dp
    val trailingIconColor = androidx.compose.material.MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity)

    val rotation: Float by animateFloatAsState(if (expanded) 180f else 0f)

    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        Box(
            Modifier.border(indicatorWidth, indicatorColor, androidx.compose.material.MaterialTheme.shapes.medium)
        ) {
            Box(Modifier.fillMaxWidth().background(color = backgroundColor, shape = shape)
                .onGloballyPositioned { textfieldSize = it.size.toSize() }.clip(shape).clickable {
                    expanded = !expanded
                    focusManager.clearFocus()
                }.padding(start = 16.dp, end = 12.dp, top = 7.dp, bottom = 10.dp)) {
                Column(Modifier.padding(end = 32.dp)) {
                    Text(
                        text = values.getOrElse(selectedIndex) { "Loading" }, modifier = Modifier.padding(top = 7.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = "Change",
                    tint = trailingIconColor,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(top = 4.dp).rotate(rotation)
                )

            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(LocalDensity.current) { textfieldSize.width.toDp() })
        ) {
            values.forEachIndexed { i, v ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onChange(i)
                }) {
                    Text(v)
                }
            }
        }
    }
}