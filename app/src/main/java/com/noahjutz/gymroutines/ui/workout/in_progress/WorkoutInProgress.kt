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

package com.noahjutz.gymroutines.ui.workout.in_progress

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noahjutz.gymroutines.data.domain.WorkoutWithSetGroups
import com.noahjutz.gymroutines.data.domain.duration
import com.noahjutz.gymroutines.ui.components.*
import com.noahjutz.gymroutines.util.RegexPatterns
import com.noahjutz.gymroutines.util.formatSimple
import com.noahjutz.gymroutines.util.pretty
import com.noahjutz.gymroutines.util.toStringOrBlank
import kotlin.time.ExperimentalTime
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun WorkoutInProgress(
    navToExercisePicker: () -> Unit,
    popBackStack: () -> Unit,
    workoutId: Int,
    exerciseIdsToAdd: List<Int>,
    viewModel: WorkoutInProgressViewModel = getViewModel { parametersOf(workoutId) },
) {
    LaunchedEffect(exerciseIdsToAdd) {
        viewModel.addExercises(exerciseIdsToAdd)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Workout",
                navigationIcon = {
                    IconButton(onClick = popBackStack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        },
    ) {
        val workout by viewModel.workout.collectAsState(initial = null)

        Crossfade(workout == null) { isNull ->
            if (isNull) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                workout?.let { workout ->
                    WorkoutInProgressContent(
                        workout = workout,
                        viewModel = viewModel,
                        popBackStack = popBackStack,
                        navToExercisePicker = navToExercisePicker
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalTime::class, ExperimentalFoundationApi::class)
@Composable
private fun WorkoutInProgressContent(
    workout: WorkoutWithSetGroups,
    viewModel: WorkoutInProgressViewModel,
    popBackStack: () -> Unit,
    navToExercisePicker: () -> Unit,
) {
    var showFinishWorkoutDialog by remember { mutableStateOf(false) }
    if (showFinishWorkoutDialog) FinishWorkoutDialog(
        onDismiss = { showFinishWorkoutDialog = false },
        finishWorkout = {
            viewModel.finishWorkout(popBackStack)
        }
    )

    var showCancelWorkoutDialog by remember { mutableStateOf(false) }
    if (showCancelWorkoutDialog) CancelWorkoutDialog(
        onDismiss = { showCancelWorkoutDialog = false },
        cancelWorkout = {
            viewModel.cancelWorkout(popBackStack)
        }
    )

    LazyColumn(Modifier.fillMaxHeight()) {
        item {
            Surface(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                color = colors.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(Modifier.padding(24.dp)) {
                    Text(
                        workout.workout.routineId.toString(),//TODO
                        style = typography.h3
                    )
                }
            }
            Text(
                workout.workout.duration.pretty(),
                Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                style = typography.h4.copy(textAlign = TextAlign.Center)
            )
        }

        items(workout.setGroups.sortedBy { it.group.position }, key = { it.group.id }) { setGroup ->
            val exercise by viewModel.getExercise(setGroup.group.exerciseId)
                .collectAsState(initial = null)
            Card(
                Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column {
                    Surface(Modifier.fillMaxWidth(), color = colors.primary) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                exercise?.name.toString(),
                                style = typography.h5,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .weight(1f)
                            )

                            Box {
                                var expanded by remember { mutableStateOf(false) }
                                IconButton(
                                    modifier = Modifier.padding(16.dp),
                                    onClick = { expanded = !expanded }
                                ) {
                                    Icon(Icons.Default.DragHandle, "More")
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            expanded = false
                                            val id = setGroup.group.id
                                            val toId = workout.setGroups
                                                .find { it.group.position == setGroup.group.position - 1 }
                                                ?.group
                                                ?.id
                                            if (toId != null) {
                                                viewModel.swapSetGroups(id, toId)
                                            }
                                        }
                                    ) {
                                        Text("Move Up")
                                    }
                                    DropdownMenuItem(
                                        onClick = {
                                            expanded = false
                                            val id = setGroup.group.id
                                            val toId = workout.setGroups
                                                .find { it.group.position == setGroup.group.position + 1 }
                                                ?.group
                                                ?.id
                                            if (toId != null) {
                                                viewModel.swapSetGroups(id, toId)
                                            }
                                        }
                                    ) {
                                        Text("Move Down")
                                    }
                                }
                            }
                        }
                    }
                    Column(Modifier.padding(vertical = 16.dp)) {
                        Row(Modifier.padding(horizontal = 4.dp)) {
                            val headerTextStyle = TextStyle(
                                color = colors.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            if (exercise?.logReps == true) Box(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Reps",
                                    style = headerTextStyle
                                )
                            }
                            if (exercise?.logWeight == true) Box(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Weight",
                                    style = headerTextStyle
                                )
                            }
                            if (exercise?.logTime == true) Box(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Time",
                                    style = headerTextStyle
                                )
                            }
                            if (exercise?.logDistance == true) Box(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Distance",
                                    style = headerTextStyle
                                )
                            }
                            Box(
                                Modifier
                                    .padding(4.dp)
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null)
                            }
                        }
                        for (set in setGroup.sets) {
                            key(set.workoutSetId) {
                                val dismissState = rememberDismissState()
                                LaunchedEffect(dismissState.currentValue) {
                                    if (dismissState.currentValue != DismissValue.Default) {
                                        viewModel.deleteSet(set)
                                        dismissState.snapTo(DismissValue.Default)
                                    }
                                }
                                SwipeToDismiss(
                                    state = dismissState,
                                    background = { SwipeToDeleteBackground(dismissState) },
                                ) {
                                    Surface {
                                        Row(
                                            Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            val textFieldStyle = typography.body1.copy(
                                                textAlign = TextAlign.Center,
                                                color = colors.onSurface
                                            )
                                            val decorationBox: @Composable (@Composable () -> Unit) -> Unit =
                                                { innerTextField ->
                                                    Surface(
                                                        color = colors.onSurface.copy(alpha = 0.1f),
                                                        shape = RoundedCornerShape(8.dp),
                                                    ) {
                                                        Box(
                                                            Modifier
                                                                .padding(horizontal = 4.dp)
                                                                .height(56.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            innerTextField()
                                                        }
                                                    }
                                                }
                                            if (exercise?.logReps == true) {
                                                val (reps, setReps) = remember { mutableStateOf(set.reps.toStringOrBlank()) }
                                                LaunchedEffect(reps) {
                                                    val repsInt = reps.toIntOrNull()
                                                    viewModel.updateReps(set, repsInt)
                                                }
                                                AutoSelectTextField(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(4.dp),
                                                    value = reps,
                                                    onValueChange = {
                                                        if (it.matches(RegexPatterns.integer))
                                                            setReps(it)
                                                    },
                                                    textStyle = textFieldStyle,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    cursorColor = colors.onSurface,
                                                    decorationBox = decorationBox,
                                                )
                                            }
                                            if (exercise?.logWeight == true) {
                                                val (weight, setWeight) = remember {
                                                    mutableStateOf(
                                                        set.weight.formatSimple()
                                                    )
                                                }
                                                LaunchedEffect(weight) {
                                                    val weightDouble = weight.toDoubleOrNull()
                                                    viewModel.updateWeight(set, weightDouble)
                                                }
                                                AutoSelectTextField(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(4.dp),
                                                    value = weight,
                                                    onValueChange = {
                                                        if (it.matches(RegexPatterns.float))
                                                            setWeight(it)
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    textStyle = textFieldStyle,
                                                    cursorColor = colors.onSurface,
                                                    decorationBox = decorationBox
                                                )
                                            }
                                            if (exercise?.logTime == true) {
                                                val (time, setTime) = remember { mutableStateOf(set.time.toStringOrBlank()) }
                                                LaunchedEffect(time) {
                                                    val timeInt = time.toIntOrNull()
                                                    viewModel.updateTime(set, timeInt)
                                                }
                                                AutoSelectTextField(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(4.dp),
                                                    value = time,
                                                    onValueChange = {
                                                        if (it.matches(RegexPatterns.duration))
                                                            setTime(it)
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    textStyle = textFieldStyle,
                                                    visualTransformation = durationVisualTransformation,
                                                    cursorColor = colors.onSurface,
                                                    decorationBox = decorationBox
                                                )
                                            }
                                            if (exercise?.logDistance == true) {
                                                val (distance, setDistance) = remember {
                                                    mutableStateOf(
                                                        set.distance.formatSimple()
                                                    )
                                                }
                                                LaunchedEffect(distance) {
                                                    val distanceDouble = distance.toDoubleOrNull()
                                                    viewModel.updateDistance(set, distanceDouble)
                                                }
                                                AutoSelectTextField(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(4.dp),
                                                    value = distance,
                                                    onValueChange = {
                                                        if (it.matches(RegexPatterns.float))
                                                            setDistance(it)
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    textStyle = textFieldStyle,
                                                    cursorColor = colors.onSurface,
                                                    decorationBox = decorationBox
                                                )
                                            }
                                            Box(
                                                Modifier
                                                    .padding(4.dp)
                                                    .size(56.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .toggleable(
                                                        value = set.complete,
                                                        onValueChange = {
                                                            viewModel.updateChecked(set, it)
                                                        },
                                                    )
                                                    .background(
                                                        animateColorAsState(
                                                            if (set.complete) colors.secondary else colors.onSurface.copy(
                                                                alpha = 0.1f
                                                            )
                                                        ).value
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                androidx.compose.animation.AnimatedVisibility(
                                                    visible = set.complete,
                                                    enter = fadeIn(),
                                                    exit = fadeOut()
                                                ) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        "Complete",
                                                        tint = colors.onSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    TextButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        onClick = { viewModel.addSet(setGroup) },
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(12.dp))
                        Text("Add Set")
                    }
                }
            }
        }

        item {
            Button(
                modifier = Modifier
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth()
                    .height(128.dp),
                shape = RoundedCornerShape(24.dp),
                onClick = navToExercisePicker
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(12.dp))
                Text("Add Exercise")
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                OutlinedButton(
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(percent = 100),
                    onClick = { showCancelWorkoutDialog = true },
                ) {
                    Text("Delete Workout")
                }
                Spacer(Modifier.width(16.dp))
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(percent = 100),
                    onClick = { showFinishWorkoutDialog = true }
                ) {
                    Text("Finish Workout")
                }
            }
        }
    }
}

@Composable
private fun CancelWorkoutDialog(
    onDismiss: () -> Unit,
    cancelWorkout: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Workout?") },
        text = { Text("Do you really want to delete this workout?") },
        confirmButton = { Button(onClick = cancelWorkout) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun FinishWorkoutDialog(
    onDismiss: () -> Unit,
    finishWorkout: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finish Workout?") },
        text = { Text("Do you want to finish the workout?") },
        confirmButton = { Button(onClick = finishWorkout) { Text("Finish") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
