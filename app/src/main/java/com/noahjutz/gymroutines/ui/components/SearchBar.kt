package com.noahjutz.gymroutines.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@ExperimentalAnimationApi
@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onClear = { onValueChange("") }
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        leadingIcon = { Icon(Icons.Default.Search, null) },
        label = { Text("Search") },
        trailingIcon = (
            @Composable {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Clear,
                        null
                    )
                }
            }
            ).takeIf { value.isNotEmpty() }
    )
}
