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

package com.noahjutz.splitfit.ui.exercises

import androidx.compose.animation.animate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.noahjutz.splitfit.util.SwipeToDeleteBackground

@ExperimentalMaterialApi
@Composable
fun ExercisesScreen(
    addEditExercise: (Int) -> Unit,
    viewModel: ExercisesViewModel
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { addEditExercise(-1) },
                content = { Icon(Icons.Default.Add) }
            )
        },
        bodyContent = {
            val exercises by viewModel.exercises.observeAsState()
            LazyColumn(Modifier.fillMaxHeight()) {
                items(exercises?.filter { !it.hidden } ?: emptyList()) { exercise ->
                    val dismissState = rememberDismissState()
                    var hidden by remember { mutableStateOf(false) }

                    if (!hidden) {
                        SwipeToDismiss(
                            state = dismissState,
                            background = { SwipeToDeleteBackground(dismissState) }
                        ) {
                            Card(elevation = animate(if (dismissState.dismissDirection != null) 4.dp else 0.dp)) {
                                ListItem(Modifier.clickable { addEditExercise(exercise.exerciseId) }) {
                                    Text(
                                        text = exercise.name.takeIf { it.isNotBlank() }
                                            ?: "Unnamed",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }

                    if (dismissState.value != DismissValue.Default) {
                        AlertDialog(
                            title = { Text("Delete ${exercise.name.takeIf { it.isNotBlank() } ?: "Unnamed"}?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.hide(exercise, true)
                                        hidden = true
                                        dismissState.snapTo(DismissValue.Default)
                                    },
                                    content = { Text("Yes") }
                                )
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        dismissState.snapTo(DismissValue.Default)
                                    },
                                    content = { Text("Cancel") }
                                )
                            },
                            onDismissRequest = {
                                dismissState.snapTo(DismissValue.Default)
                            }
                        )
                    }
                }
                item {
                    // Fix FAB overlap
                    Box(Modifier.height(72.dp)) {}
                }
            }
        }
    )
}
