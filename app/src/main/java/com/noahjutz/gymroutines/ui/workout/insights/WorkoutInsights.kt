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

package com.noahjutz.gymroutines.ui.workout.insights

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.placeholder.material.placeholder
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.data.domain.Workout
import com.noahjutz.gymroutines.data.domain.duration
import com.noahjutz.gymroutines.ui.components.*
import java.util.*
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalTime
@Composable
fun WorkoutInsights(
    viewModel: WorkoutInsightsViewModel = getViewModel(),
    navToSettings: () -> Unit,
    navToWorkoutEditor: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.tab_insights),
                actions = {
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Default.MoreVert, "More")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(onClick = navToSettings) {
                                Text("Settings")
                            }
                        }
                    }
                }
            )
        }
    ) {
        val scope = rememberCoroutineScope()
        val workouts by viewModel.workouts.collectAsState(initial = null)
        val routineNames by viewModel.routineNames.collectAsState(initial = null)

        LazyColumn {
            stickyHeader {
                Surface(Modifier.fillMaxWidth()) {
                    Text(
                        "Charts",
                        Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        style = typography.h4
                    )
                }
            }

            item {
                Box(Modifier.padding(16.dp)) {
                    WorkoutCharts(workouts)
                }
            }

            stickyHeader {
                Surface(Modifier.fillMaxWidth()) {
                    Text(
                        "History",
                        Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                        style = typography.h4
                    )
                }
            }

            if (workouts != null && routineNames != null) {
                items(workouts ?: emptyList(), { it.workoutId }) { workout ->
                    val dismissState = rememberDismissState()
                    val routineName = routineNames?.get(workout.workoutId) ?: ""

                    SwipeToDismiss(
                        modifier = Modifier
                            .zIndex(if (dismissState.offset.value == 0f) 0f else 1f),
                        state = dismissState,
                        background = { SwipeToDeleteBackground(dismissState) }
                    ) {
                        Card(
                            onClick = { navToWorkoutEditor(workout.workoutId) },
                            elevation = animateDpAsState(
                                if (dismissState.dismissDirection != null) 4.dp else 0.dp
                            ).value,
                        ) {
                            ListItem(
                                text = {
                                    Text(
                                        text = routineName,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                trailing = {
                                    var expanded by remember { mutableStateOf(false) }

                                    Box {
                                        IconButton(onClick = { expanded = !expanded }) {
                                            Icon(Icons.Default.MoreVert, null)
                                        }

                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                onClick = {
                                                    expanded = false
                                                    scope.launch {
                                                        dismissState.dismiss(DismissDirection.StartToEnd)
                                                    }
                                                }
                                            ) {
                                                Text("Delete")
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (dismissState.targetValue != DismissValue.Default) {
                        DeleteConfirmation(
                            name = routineName,
                            onConfirm = { viewModel.delete(workout) },
                            onDismiss = { scope.launch { dismissState.reset() } }
                        )
                    }
                }
            } else {
                items(5) {
                    ListItem {
                        Text(
                            "A".repeat((5..15).random()),
                            Modifier.placeholder(visible = true)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmation(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(
                stringResource(
                    R.string.confirm_delete,
                    name
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                content = { Text(stringResource(R.string.yes)) }
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = { Text(stringResource(R.string.cancel)) }
            )
        },
        onDismissRequest = onDismiss
    )
}

@ExperimentalTime
@Composable
private fun WorkoutCharts(
    workouts: List<Workout>?,
) {
    ChartCard(title = "Workout duration") {
        when {
            workouts == null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .placeholder(visible = true)
                )
            }
            workouts.size < 3 -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Not enough data.", color = colors.onSurface.copy(alpha = 0.6f))
                }
            }
            else -> {
                SimpleLineChart(
                    Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    data = workouts
                        .reversed()
                        .mapIndexed { i, workout ->
                            Pair(i.toFloat(), workout.duration.inWholeSeconds.toFloat())
                        }
                        .chunked(3) {
                            val avg = it.map { it.second }.average()
                            Pair(it.first().first, avg.toFloat())
                        },
                    secondaryData = workouts
                        .reversed()
                        .mapIndexed { i, workout ->
                            Pair(i.toFloat(), workout.duration.inWholeSeconds.toFloat())
                        }
                )
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    chart: @Composable () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(30.dp),
        elevation = 2.dp,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Surface(
                Modifier.fillMaxWidth(),
                color = colors.primary
            ) {
                Text(
                    title,
                    Modifier.padding(20.dp),
                    style = typography.h6
                )
            }
            chart()
        }
    }
}
