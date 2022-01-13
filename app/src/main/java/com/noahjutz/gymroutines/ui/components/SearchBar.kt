package com.noahjutz.gymroutines.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.noahjutz.gymroutines.R

@ExperimentalAnimationApi
@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.h6.copy(color = MaterialTheme.colors.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
        decorationBox = { innerTextField ->
            Surface(
                modifier = Modifier.height(60.dp),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(30.dp)
            ) {
                Row(
                    Modifier.padding(start = 24.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, null)
                    Spacer(Modifier.width(12.dp))
                    Box(
                        Modifier
                            .padding(vertical = 16.dp)
                            .weight(1f)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                stringResource(R.string.hint_search),
                                style = MaterialTheme.typography.h6.copy(
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                                )
                            )
                        }
                        innerTextField()
                    }
                    AnimatedVisibility(
                        value.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(Icons.Default.Clear, stringResource(R.string.btn_clear_text))
                        }
                    }
                }
            }
        }
    )
}
