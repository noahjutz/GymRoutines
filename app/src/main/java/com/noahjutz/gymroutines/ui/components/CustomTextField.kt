/*
 * Splitfit
 * Copyright (C) 2020  Noah Jutz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.noahjutz.gymroutines.ui.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun AutoSelectTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textStyle: TextStyle = LocalTextStyle.current,
    cursorColor: Color = LocalContentColor.current,
    maxLines: Int = Int.MAX_VALUE,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit = { it() },
    singleLine: Boolean = false,
) {
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }

    fun selectText() {
        textFieldValue = textFieldValue.copy(selection = TextRange(0, textFieldValue.text.length))
    }

    fun deselectText() {
        textFieldValue = textFieldValue.copy(selection = TextRange(textFieldValue.text.length))
    }

    // onValueChange is called after onFocusChanged, overriding the selection in onFocusChanged.
    // see https://stackoverflow.com/questions/66262168
    var isValueChangeLocked = false

    BasicTextField(
        modifier = modifier.onFocusChanged {
            if (it.isFocused) {
                if (textFieldValue.text.isNotEmpty()) {
                    selectText()
                    isValueChangeLocked = true
                }
            }
        },
        value = textFieldValue,
        onValueChange = {
            if (!isValueChangeLocked) {
                onValueChange(it.text)
                deselectText()
            } else {
                isValueChangeLocked = false
            }
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        textStyle = textStyle,
        cursorBrush = SolidColor(cursorColor),
        maxLines = maxLines,
        decorationBox = decorationBox,
        singleLine = singleLine,
    )
}

/** Turns string of 0-4 digits to MM:SS format */
val durationVisualTransformation = object : VisualTransformation {
    val offsetMap = object : OffsetMapping {
        override fun originalToTransformed(offset: Int) = if (offset == 0) 0 else 5
        override fun transformedToOriginal(offset: Int) = if (offset == 0) 0 else 5 - offset
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val withZeroes = "0".repeat((4 - text.text.length).takeIf { it > 0 } ?: 0) + text.text
        val withColon = withZeroes.let { it.substring(0, 2) + ":" + it.substring(2, 4) }
        return TransformedText(
            AnnotatedString(if (text.text.isEmpty()) "" else withColon),
            offsetMap
        )
    }
}
