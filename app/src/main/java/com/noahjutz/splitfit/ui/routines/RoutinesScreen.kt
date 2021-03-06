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

package com.noahjutz.splitfit.ui.routines

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun RoutinesScreen(
    addEditRoutine: (Int) -> Unit,
    viewModel: RoutinesViewModel
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { addEditRoutine(-1) },
                content = { Icon(Icons.Default.Add) }
            )
        }
    ) {
        val routines by viewModel.routines.observeAsState()
        LazyColumnFor(items = routines ?: emptyList()) { routine ->
            val dismissState = rememberDismissState()

            SwipeToDismiss(
                state = dismissState,
                background = {
                    val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                    val alignment = when (direction) {
                        DismissDirection.StartToEnd -> Alignment.CenterStart
                        DismissDirection.EndToStart -> Alignment.CenterEnd
                    }
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Color.Red)
                            .padding(horizontal = 20.dp),
                        contentAlignment = alignment
                    ) {
                        Icon(Icons.Default.Delete)
                    }
                },
                dismissContent = {
                    Card(
                        elevation = animate(if (dismissState.dismissDirection != null) 4.dp else 0.dp)
                    ) {
                        ListItem(
                            text = {
                                Text(
                                    text = routine.name.takeIf { it.isNotBlank() } ?: "Unnamed",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            modifier = Modifier.clickable {
                                addEditRoutine(routine.routineId)
                            }
                        )
                    }
                }
            )

            if (dismissState.value != DismissValue.Default) {
                AlertDialog(
                    title = { Text("Delete ${routine.name.takeIf { it.isNotBlank() } ?: "Unnamed"}?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteRoutine(routine.routineId)
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
    }
}
