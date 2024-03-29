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

package com.noahjutz.gymroutines.ui.routines.editor

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.data.domain.Routine
import com.noahjutz.gymroutines.data.domain.RoutineSetGroupWithSets
import com.noahjutz.gymroutines.ui.components.AutoSelectTextField
import com.noahjutz.gymroutines.ui.components.SwipeToDeleteBackground
import com.noahjutz.gymroutines.ui.components.TopBar
import com.noahjutz.gymroutines.ui.components.durationVisualTransformation
import com.noahjutz.gymroutines.util.RegexPatterns
import com.noahjutz.gymroutines.util.formatSimple
import com.noahjutz.gymroutines.util.toStringOrBlank
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun RoutineEditor(
    navToExercisePicker: () -> Unit,
    navToWorkout: (Long) -> Unit,
    popBackStack: () -> Unit,
    routineId: Int,
    exerciseIdsToAdd: List<Int>,
    viewModel: RoutineEditorViewModel = getViewModel { parametersOf(routineId) },
) {
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(exerciseIdsToAdd) {
        viewModel.addExercises(exerciseIdsToAdd)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            val isWorkoutRunning by viewModel.isWorkoutInProgress.collectAsState(initial = false)
            if (!isWorkoutRunning) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.startWorkout { id ->
                            navToWorkout(id)
                        }
                    },
                    icon = { Icon(Icons.Default.PlayArrow, null) },
                    text = { Text(stringResource(R.string.btn_start_workout)) },
                )
            }
        },
        topBar = {
            TopBar(
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_pop_back))
                    }
                },
                title = stringResource(R.string.screen_edit_routine),
            )
        }
    ) { paddingValues ->
        val routine by viewModel.routine.collectAsState(initial = null)
        Crossfade(routine != null, Modifier.padding(paddingValues)) { isReady ->
            if (!isReady) {
                RoutineEditorPlaceholder()
            } else {
                routine?.let { routine ->
                    RoutineEditorContent(
                        routine = routine.routine,
                        setGroups = routine.setGroups,
                        viewModel = viewModel,
                        navToExercisePicker = navToExercisePicker
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun RoutineEditorContent(
    routine: Routine,
    setGroups: List<RoutineSetGroupWithSets>,
    viewModel: RoutineEditorViewModel,
    navToExercisePicker: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxHeight(),
        contentPadding = PaddingValues(bottom = 70.dp)
    ) {

        item {
            val (name, setName) = remember { mutableStateOf(routine.name) }
            LaunchedEffect(name) {
                viewModel.updateName(name)
            }
            val (nameLineCount, setNameLineCount) = remember { mutableStateOf(0) }
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 30.dp, end = 30.dp),
                value = name,
                onValueChange = setName,
                onTextLayout = { setNameLineCount(it.lineCount) },
                textStyle = typography.h6.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.onSurface),
                decorationBox = { innerTextField ->
                    Surface(
                        modifier = if (nameLineCount <= 1) Modifier.height(60.dp) else Modifier,
                        color = colors.onSurface.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(30.dp)
                    ) {
                        Row(
                            Modifier.padding(start = 30.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .padding(vertical = 16.dp)
                                    .weight(1f)
                            ) {
                                if (routine.name.isEmpty()) {
                                    Text(
                                        stringResource(R.string.unnamed_routine),
                                        style = typography.h6.copy(
                                            color = colors.onSurface.copy(alpha = 0.12f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                            AnimatedVisibility(
                                name.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { setName("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        stringResource(R.string.btn_clear_text)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        items(setGroups.sortedBy { it.group.position }, key = { it.group.id }) { setGroup ->
            val exercise = viewModel.getExercise(setGroup.group.exerciseId)!!
            Card(
                Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
                    .padding(top = 30.dp),
                shape = RoundedCornerShape(30.dp),
            ) {
                Column {
                    Surface(Modifier.fillMaxWidth(), color = colors.primary) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                exercise.name,
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
                                    Icon(
                                        Icons.Default.DragHandle,
                                        stringResource(R.string.btn_more)
                                    )
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            expanded = false
                                            val id = setGroup.group.id
                                            val toId = setGroups
                                                .find { it.group.position == setGroup.group.position - 1 }
                                                ?.group
                                                ?.id
                                            if (toId != null) {
                                                viewModel.swapSetGroups(id, toId)
                                            }
                                        }
                                    ) {
                                        Text(stringResource(R.string.btn_move_up))
                                    }
                                    DropdownMenuItem(
                                        onClick = {
                                            expanded = false
                                            val id = setGroup.group.id
                                            val toId = setGroups
                                                .find { it.group.position == setGroup.group.position + 1 }
                                                ?.group
                                                ?.id
                                            if (toId != null) {
                                                viewModel.swapSetGroups(id, toId)
                                            }
                                        }
                                    ) {
                                        Text(stringResource(R.string.btn_move_down))
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
                            if (exercise.logReps) Surface(
                                Modifier
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .weight(1f),
                            ) {
                                Text(
                                    stringResource(R.string.column_reps),
                                    style = headerTextStyle
                                )
                            }
                            if (exercise.logWeight) Surface(
                                Modifier
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .weight(1f),
                            ) {
                                Text(
                                    stringResource(R.string.column_weight),
                                    style = headerTextStyle
                                )
                            }
                            if (exercise.logTime) Surface(
                                Modifier
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .weight(1f),
                            ) {
                                Text(
                                    stringResource(R.string.column_time),
                                    style = headerTextStyle
                                )
                            }
                            if (exercise.logDistance) Surface(
                                Modifier
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .weight(1f),
                            ) {
                                Text(
                                    stringResource(R.string.column_distance),
                                    style = headerTextStyle
                                )
                            }
                        }
                        for (set in setGroup.sets) {
                            key(set.routineSetId) {
                                val dismissState = rememberDismissState()
                                LaunchedEffect(dismissState.currentValue) {
                                    if (dismissState.currentValue != DismissValue.Default) {
                                        viewModel.deleteSet(set)
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
                                                            Modifier.padding(
                                                                vertical = 16.dp,
                                                                horizontal = 4.dp
                                                            ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            innerTextField()
                                                        }
                                                    }
                                                }
                                            if (exercise.logReps) {
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
                                            if (exercise.logWeight) {
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
                                            if (exercise.logTime) {
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
                                            if (exercise.logDistance) {
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
                        Text(stringResource(R.string.btn_add_set))
                    }
                }
            }
        }

        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp)
                    .height(120.dp),
                shape = RoundedCornerShape(30.dp),
                onClick = navToExercisePicker
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.btn_add_exercise))
            }
        }
    }
}
