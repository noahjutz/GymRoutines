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
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.data.domain.Workout
import com.noahjutz.gymroutines.data.domain.duration
import com.noahjutz.gymroutines.ui.components.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@ExperimentalMaterialApi
@ExperimentalTime
@Composable
fun WorkoutInsights(
    viewModel: WorkoutInsightsViewModel = getViewModel(),
    navToWorkoutEditor: (Int) -> Unit,
) {
    val workouts by viewModel.presenter.workouts.collectAsState(initial = emptyList())

    if (workouts.size <= 1) NothingHereYet("No data yet.")
    else WorkoutInsightsContent(viewModel, navToWorkoutEditor)
}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalTime
@ExperimentalMaterialApi
@Composable
fun WorkoutInsightsContent(
    viewModel: WorkoutInsightsViewModel = getViewModel(),
    navToWorkoutEditor: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val workouts by viewModel.presenter.workouts.collectAsState(emptyList())
    Scaffold(topBar = { TopBar(title = stringResource(R.string.tab_insights)) }) {
        LazyColumn {
            item {
                WorkoutCharts(workouts.map { it.workout })
                Text(
                    "History",
                    Modifier.padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                    style = typography.h5
                )
            }
            items(workouts, { it.workout.workoutId }) { workout ->
                val dismissState = rememberDismissState()

                SwipeToDismiss(
                    modifier = Modifier
                        .animateItemPlacement()
                        .zIndex(if (dismissState.offset.value == 0f) 0f else 1f),
                    state = dismissState,
                    background = { SwipeToDeleteBackground(dismissState) }
                ) {
                    Card(
                        onClick = { navToWorkoutEditor(workout.workout.workoutId) },
                        elevation = animateDpAsState(
                            if (dismissState.dismissDirection != null) 4.dp else 0.dp
                        ).value,
                    ) {
                        ListItem(
                            text = {
                                Text(
                                    text = workout.workout.routineId.toString(),//TODO
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            secondaryText = {
                                val day = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(workout.workout.startTime)
                                Text(day)
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
                        workout = workout.workout,
                        onConfirm = { viewModel.editor.delete(workout) },
                        onDismiss = { scope.launch { dismissState.reset() } }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmation(
    workout: Workout,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(
                stringResource(
                    R.string.confirm_delete,
                    workout.workoutId.toString()//TODO
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
    workouts: List<Workout>,
) {
    Box(
        Modifier.padding(16.dp),
    ) {
        ChartCard(title = "Average duration") {
            if (workouts.isNotEmpty()) {
                SimpleLineChart(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp),
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
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(title, style = typography.body1.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(8.dp))
            chart()
        }
    }
}
