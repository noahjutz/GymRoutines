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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.data.domain.SetLegacy
import com.noahjutz.gymroutines.util.RegexPatterns
import com.noahjutz.gymroutines.util.formatSimple
import com.noahjutz.gymroutines.util.toStringOrBlank
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun SetGroupCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    border: BorderStroke? = null,
    name: String,
    sets: List<SetLegacy>,
    onMoveDown: () -> Unit,
    onMoveUp: () -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (Int) -> Unit,
    logReps: Boolean,
    onRepsChange: (Int, String) -> Unit = { _, _ -> },
    logWeight: Boolean,
    onWeightChange: (Int, String) -> Unit = { _, _ -> },
    logTime: Boolean,
    onTimeChange: (Int, String) -> Unit = { _, _ -> },
    logDistance: Boolean,
    onDistanceChange: (Int, String) -> Unit = { _, _ -> },
    showCheckbox: Boolean,
    onCheckboxChange: (Int, Boolean) -> Unit = { _, _ -> },
) {
    Card(
        modifier,
        elevation = elevation,
        shape = shape,
        border = border,
    ) {
        Column(Modifier.fillMaxWidth()) {
            SetGroupTitle(
                name = name,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
            )
            SetTable(
                Modifier.padding(horizontal = 16.dp),
                sets = sets,
                logReps = logReps,
                onRepsChange = onRepsChange,
                logWeight = logWeight,
                onWeightChange = onWeightChange,
                logTime = logTime,
                onTimeChange = onTimeChange,
                logDistance = logDistance,
                onDistanceChange = onDistanceChange,
                showCheckbox = showCheckbox,
                onCheckboxChange = onCheckboxChange,
                onAddSet = onAddSet,
                onDeleteSet = onDeleteSet,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SetGroupTitle(
    modifier: Modifier = Modifier,
    name: String,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Box(Modifier.clickable {}) {
        Row(
            modifier
                .height(70.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProvideTextStyle(typography.h5) {
                Text(
                    name.takeIf { it.isNotBlank() } ?: stringResource(R.string.unnamed_exercise),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            // Temporary replacement for drag & drop.
            // See https://stackoverflow.com/questions/64913067
            Box {
                var showMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "More") }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(onClick = onMoveUp) {
                        Text("Move up")
                    }
                    DropdownMenuItem(onClick = onMoveDown) {
                        Text("Move down")
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
private fun SetTable(
    modifier: Modifier = Modifier,
    sets: List<SetLegacy>,
    logReps: Boolean,
    onRepsChange: (Int, String) -> Unit,
    logWeight: Boolean,
    onWeightChange: (Int, String) -> Unit,
    logTime: Boolean,
    onTimeChange: (Int, String) -> Unit,
    logDistance: Boolean,
    onDistanceChange: (Int, String) -> Unit,
    showCheckbox: Boolean,
    onCheckboxChange: (Int, Boolean) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (Int) -> Unit,
) {
    Table(modifier) {
        SetTableHeader(
            logReps = logReps,
            logWeight = logWeight,
            logTime = logTime,
            logDistance = logDistance,
            showCheckbox = showCheckbox,
        )
        Divider()

        sets.forEachIndexed { i, set ->
            var reps by remember { mutableStateOf(set.reps.toStringOrBlank()) }
            var weight by remember { mutableStateOf(set.weight.formatSimple()) }
            var time by remember { mutableStateOf(set.time.toStringOrBlank()) }
            var distance by remember { mutableStateOf(set.distance.formatSimple()) }

            TableSetRow(
                logReps = logReps,
                reps = reps,
                onRepsChange = {
                    reps = it
                    onRepsChange(i, it)
                },
                logWeight = logWeight,
                weight = weight,
                onWeightChange = {
                    weight = it
                    onWeightChange(i, it)
                },
                logDuration = logTime,
                duration = time,
                onDurationChange = {
                    time = it
                    onTimeChange(i, it)
                },
                logDistance = logDistance,
                distance = distance,
                onDistanceChange = {
                    distance = it
                    onDistanceChange(i, it)
                },
                showCheckbox = showCheckbox,
                checkboxChecked = set.complete,
                onCheckboxChange = { onCheckboxChange(i, it) },
                onDeleteSet = { onDeleteSet(i) }
            )
            Divider()
        }

        TableRow {
            TextButton(
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth(),
                shape = RectangleShape,
                onClick = onAddSet,
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.height(8.dp))
                Text("Add Set")
            }
        }
    }
}

@Composable
private fun ColumnScope.SetTableHeader(
    logReps: Boolean,
    logWeight: Boolean,
    logTime: Boolean,
    logDistance: Boolean,
    showCheckbox: Boolean,
) {
    TableHeaderRow(Modifier.padding(horizontal = 16.dp)) {
        if (logReps) TableCell(Modifier.weight(1f)) {
            Text(stringResource(R.string.reps))
        }
        if (logWeight) TableCell(Modifier.weight(1f)) {
            Text(stringResource(R.string.weight))
        }
        if (logTime) TableCell(Modifier.weight(1f)) {
            Text(stringResource(R.string.time))
        }
        if (logDistance) TableCell(Modifier.weight(1f)) {
            Text(stringResource(R.string.distance))
        }
        if (showCheckbox) Spacer(Modifier.width(40.dp))
    }
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
private fun ColumnScope.TableSetRow(
    modifier: Modifier = Modifier,
    logReps: Boolean,
    reps: String = "",
    onRepsChange: ((String) -> Unit) = {},
    logWeight: Boolean,
    weight: String = "",
    onWeightChange: ((String) -> Unit) = {},
    logDuration: Boolean,
    duration: String = "",
    onDurationChange: ((String) -> Unit) = {},
    logDistance: Boolean,
    distance: String = "",
    onDistanceChange: ((String) -> Unit) = {},
    showCheckbox: Boolean,
    checkboxChecked: Boolean,
    onCheckboxChange: (Boolean) -> Unit,
    onDeleteSet: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberDismissState()

    val isDismissed = dismissState.targetValue != DismissValue.Default

    if (isDismissed) ConfirmDeleteSetDialog(
        onDismiss = { scope.launch { dismissState.reset() } },
        onConfirm = {
            scope.launch { dismissState.snapTo(DismissValue.Default) }
            onDeleteSet()
        }
    )

    val focusManager = LocalFocusManager.current
    LaunchedEffect(isDismissed) {
        if (isDismissed) focusManager.clearFocus()
    }

    DismissibleTableRow(
        modifier.padding(start = 16.dp, end = if (showCheckbox) 8.dp else 16.dp),
        state = dismissState,
    ) {
        if (logReps) TableCell(Modifier.weight(1f)) {
            IntegerTextField(value = reps, onValueChange = onRepsChange)
        }
        if (logWeight) TableCell(Modifier.weight(1f)) {
            FloatTextField(value = weight, onValueChange = onWeightChange)
        }
        if (logDuration) TableCell(Modifier.weight(1f)) {
            DurationTextField(value = duration, onValueChange = onDurationChange)
        }
        if (logDistance) TableCell(Modifier.weight(1f)) {
            FloatTextField(value = distance, onValueChange = onDistanceChange)
        }
        if (showCheckbox) TableCell {
            Checkbox(
                modifier = Modifier.size(48.dp),
                checked = checkboxChecked,
                onCheckedChange = onCheckboxChange,
            )
        }
    }
}

@Composable
private fun IntegerTextField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    TableCellTextField(
        value = value,
        onValueChange = { if (it.matches(RegexPatterns.integer)) onValueChange(it) },
        hint = "0",
    )
}

@Composable
private fun FloatTextField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    TableCellTextField(
        value = value,
        onValueChange = { if (it.matches(RegexPatterns.float)) onValueChange(it) },
        hint = "0.0",
    )
}

@Composable
private fun DurationTextField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    TableCellTextField(
        value = value,
        onValueChange = { if (it.matches(RegexPatterns.duration)) onValueChange(it) },
        hint = "00:00",
        visualTransformation = durationVisualTransformation,
    )
}

@Composable
private fun ConfirmDeleteSetDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    NormalDialog(
        onDismissRequest = onDismiss,
        title = { Text("Do you want to delete this set?") },
        confirmButton = { Button(onClick = onConfirm) { Text(stringResource(R.string.yes)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
