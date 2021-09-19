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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.data.domain.Workout
import com.noahjutz.gymroutines.data.domain.duration
import com.noahjutz.gymroutines.ui.components.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.ExperimentalTime

@ExperimentalMaterialApi
@ExperimentalTime
@Composable
fun WorkoutInsights(
    viewModel: WorkoutInsightsViewModel = getViewModel(),
    navToWorkoutEditor: (Int) -> Unit,
) {
    val workouts by viewModel.presenter.workouts.collectAsState(initial = emptyList())

    if (workouts.isEmpty()) NothingHereYet("Insights will be available when you finish your first workout.")
    else WorkoutInsightsContent(viewModel, navToWorkoutEditor)
}

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
                WorkoutCharts(workouts)
                Text(
                    "History",
                    Modifier.padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                    style = typography.h5
                )
            }
            items(workouts) { workout ->
                val dismissState = rememberDismissState()

                SwipeToDismiss(
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
                                    text = workout.name.takeIf { it.isNotBlank() }
                                        ?: stringResource(R.string.unnamed_workout),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            secondaryText = {
                                val day = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(workout.startTime)
                                Text(day)
                            }
                        )
                    }
                }

                if (dismissState.targetValue != DismissValue.Default) {
                    DeleteConfirmation(
                        workout = workout,
                        onConfirm = {
                            viewModel.editor.delete(workout)
                            scope.launch { dismissState.snapTo(DismissValue.Default) }
                        },
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
    NormalDialog(
        title = {
            Text(
                stringResource(
                    R.string.confirm_delete,
                    workout.name.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.unnamed_workout)
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
    workouts: List<Workout>
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