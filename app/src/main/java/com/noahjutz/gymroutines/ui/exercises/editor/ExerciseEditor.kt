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

package com.noahjutz.gymroutines.ui.exercises.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.ui.components.TopBar
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@ExperimentalMaterialApi
@Composable
fun ExerciseEditor(
    popBackStack: () -> Unit,
    exerciseId: Int,
    viewModel: ExerciseEditorViewModel = getViewModel { parametersOf(exerciseId) },
) {
    var showDiscardAlert by remember { mutableStateOf(false) }
    if (showDiscardAlert) {
        AlertDialog(
            onDismissRequest = { showDiscardAlert = false },
            title = { Text(stringResource(R.string.dialog_discard_title)) },
            confirmButton = {
                Button(onClick = popBackStack) {
                    Text(stringResource(R.string.dialog_discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardAlert = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    val isSavingEnabled by viewModel.isSavingEnabled.collectAsState(initial = false)

    BackHandler(enabled = isSavingEnabled) {
        showDiscardAlert = true
    }

    Scaffold(
        topBar = {
            TopBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSavingEnabled) {
                                showDiscardAlert = true
                            } else {
                                popBackStack()
                            }
                        },
                        content = { Icon(Icons.Default.Close, stringResource(R.string.btn_cancel)) },
                    )
                },
                title = stringResource(R.string.screen_edit_exercise),
                actions = {
                    TextButton(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            viewModel.save {
                                popBackStack()
                            }
                        },
                        enabled = isSavingEnabled
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            )
        },
        content = {
            LazyColumn {
                item {
                    val name by viewModel.name.collectAsState()
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        value = name,
                        onValueChange = viewModel::setName,
                        label = { Text("Exercise name") },
                        singleLine = true,
                    )
                    val notes by viewModel.notes.collectAsState()
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        value = notes,
                        onValueChange = viewModel::setNotes,
                        label = { Text("Notes") },
                    )
                    val logReps by viewModel.logReps.collectAsState()
                    ListItem(
                        Modifier.toggleable(
                            value = logReps,
                            onValueChange = viewModel::setLogReps
                        ),
                        text = { Text(stringResource(R.string.checkbox_log_reps)) },
                        icon = { Checkbox(checked = logReps, null) },
                    )
                    val logWeight by viewModel.logWeight.collectAsState()
                    ListItem(
                        Modifier.toggleable(
                            value = logWeight,
                            onValueChange = viewModel::setLogWeight
                        ),
                        text = { Text(stringResource(R.string.checkbox_log_weight)) },
                        icon = { Checkbox(checked = logWeight, null) },
                    )
                    val logTime by viewModel.logTime.collectAsState()
                    ListItem(
                        Modifier.toggleable(
                            value = logTime,
                            onValueChange = viewModel::setLogTime
                        ),
                        text = { Text(stringResource(R.string.checkbox_log_time)) },
                        icon = { Checkbox(checked = logTime, null) },
                    )
                    val logDistance by viewModel.logDistance.collectAsState()
                    ListItem(
                        Modifier.toggleable(
                            value = logDistance,
                            onValueChange = viewModel::setLogDistance
                        ),
                        text = { Text(stringResource(R.string.checkbox_log_distance)) },
                        icon = { Checkbox(checked = logDistance, null) },
                    )
                }
            }
        }
    )
}
