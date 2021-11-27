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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.data.domain.Routine
import com.noahjutz.gymroutines.data.domain.RoutineSetGroupWithSets
import com.noahjutz.gymroutines.ui.components.TopBar
import com.noahjutz.gymroutines.ui.exercises.picker.ExercisePickerSheet
import com.noahjutz.gymroutines.util.toStringOrBlank
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun CreateRoutineScreen(
    startWorkout: (Int) -> Unit,
    popBackStack: () -> Unit,
    routineId: Int,
    viewModel: RoutineEditorViewModel = getViewModel { parametersOf(routineId) },
    preferences: DataStore<Preferences> = get(),
    navToExerciseEditor: () -> Unit,
) {
    val preferencesData by preferences.data.collectAsState(null)
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    BackHandler(enabled = sheetState.isVisible) {
        scope.launch {
            sheetState.hide()
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        sheetContent = {
            ExercisePickerSheet(
                onExercisesSelected = {
                    viewModel.addExercises(it)
                    scope.launch { sheetState.hide() }
                },
                navToExerciseEditor = navToExerciseEditor
            )
        },
        sheetElevation = 0.dp,
    ) {
        Scaffold(
            scaffoldState = scaffoldState,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        // TODO start workout
                        //val currentWorkout =
                        //    preferencesData?.get(AppPrefs.CurrentWorkout.key)
                        //if (currentWorkout == null || currentWorkout < 0) {
                        //    startWorkout(viewModel.routine.value.routine.routineId)
                        //} else {
                        //    scope.launch {
                        //        scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                        //        val snackbarResult =
                        //            scaffoldState.snackbarHostState.showSnackbar(
                        //                "A workout is already in progress.",
                        //                "Stop current"
                        //            )
                        //        if (snackbarResult == SnackbarResult.ActionPerformed) {
                        //            preferences.edit {
                        //                it[AppPrefs.CurrentWorkout.key] = -1
                        //            }
                        //            scaffoldState.snackbarHostState.showSnackbar("Current workout finished.")
                        //        }
                        //    }
                        //}
                    },
                    icon = { Icon(Icons.Default.PlayArrow, null) },
                    text = { Text("Start Workout") },
                )
            },
            topBar = {
                TopBar(
                    navigationIcon = {
                        IconButton(onClick = popBackStack) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.pop_back))
                        }
                    },
                    title = "Edit Routine",
                )
            }
        ) {
            val routine by viewModel.routine.collectAsState(initial = null)
            Crossfade(routine == null) { isNotReady ->
                if (isNotReady) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    RoutineEditorContent(
                        routine = routine!!.routine,
                        setGroups = routine!!.setGroups,
                        viewModel = viewModel,
                        sheetState = sheetState
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
    sheetState: ModalBottomSheetState
) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        Modifier.fillMaxHeight(),
        contentPadding = PaddingValues(bottom = 70.dp)
    ) {

        item {
            val (name, setName) = remember { mutableStateOf(routine.name) }
            LaunchedEffect(name) {
                viewModel.updateName(name)
            }
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                value = name,
                onValueChange = setName,
                textStyle = typography.h3.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.onSurface),
                decorationBox = { innerTextField ->
                    Surface(
                        color = colors.onSurface.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(Modifier.padding(24.dp)) {
                            if (routine.name.isEmpty()) {
                                Text(
                                    "Unnamed",
                                    style = typography.h3.copy(
                                        color = colors.onSurface.copy(alpha = 0.12f)
                                    )
                                )
                            }
                            innerTextField()
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
                                    Icon(Icons.Default.DragHandle, "More")
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(onClick = {
                                        expanded = false
                                        val id = setGroup.group.id
                                        val toId = setGroups
                                            .find { it.group.position == setGroup.group.position - 1 }
                                            ?.group
                                            ?.id
                                        if (toId != null) {
                                            viewModel.swapSetGroups(id, toId)
                                        }
                                    }) {
                                        Text("Move Up")
                                    }
                                    DropdownMenuItem(onClick = {
                                        expanded = false
                                        val id = setGroup.group.id
                                        val toId = setGroups
                                            .find { it.group.position == setGroup.group.position + 1 }
                                            ?.group
                                            ?.id
                                        if (toId != null) {
                                            viewModel.swapSetGroups(id, toId)
                                        }
                                    }) {
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
                            if (exercise.logReps) Surface(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f),
                                color = colors.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Reps",
                                    Modifier.padding(vertical = 16.dp),
                                    style = headerTextStyle
                                )
                            }
                            if (exercise.logWeight) Surface(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f),
                                color = colors.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Weight",
                                    Modifier.padding(vertical = 16.dp),
                                    style = headerTextStyle
                                )
                            }
                            if (exercise.logTime) Surface(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f),
                                color = colors.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Time",
                                    Modifier.padding(vertical = 16.dp),
                                    style = headerTextStyle
                                )
                            }
                            if (exercise.logDistance) Surface(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f),
                                color = colors.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Distance",
                                    Modifier.padding(vertical = 16.dp),
                                    style = headerTextStyle
                                )
                            }
                        }
                        for (set in setGroup.sets) {
                            val dismissState = rememberDismissState()
                            LaunchedEffect(dismissState.currentValue) {
                                if (dismissState.currentValue != DismissValue.Default) {
                                    viewModel.deleteSet(set)
                                    dismissState.snapTo(DismissValue.Default)
                                }
                            }
                            SwipeToDismiss(
                                state = dismissState,
                                background = {
                                    val alignment = when (dismissState.dismissDirection) {
                                        DismissDirection.StartToEnd -> Alignment.CenterStart
                                        DismissDirection.EndToStart -> Alignment.CenterEnd
                                        else -> Alignment.Center
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(colors.secondary)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = alignment
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            null,
                                            tint = colors.onSecondary
                                        )
                                    }
                                },
                            ) {
                                Row(
                                    Modifier
                                        .background(colors.surface)
                                        .padding(horizontal = 4.dp)
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
                                    if (exercise.logReps) BasicTextField(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp),
                                        value = set.reps.toStringOrBlank(),
                                        onValueChange = { /* TODO */ },
                                        textStyle = textFieldStyle,
                                        cursorBrush = SolidColor(colors.onSurface),
                                        decorationBox = decorationBox,
                                    )
                                    if (exercise.logWeight) BasicTextField(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp),
                                        value = set.weight.toStringOrBlank(),
                                        onValueChange = { /* TODO */ },
                                        textStyle = textFieldStyle,
                                        cursorBrush = SolidColor(colors.onSurface),
                                        decorationBox = decorationBox
                                    )
                                    if (exercise.logTime) BasicTextField(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp),
                                        value = set.time.toStringOrBlank(),
                                        onValueChange = { /* TODO */ },
                                        textStyle = textFieldStyle,
                                        cursorBrush = SolidColor(colors.onSurface),
                                        decorationBox = decorationBox
                                    )
                                    if (exercise.logDistance) BasicTextField(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp),
                                        value = set.distance.toStringOrBlank(),
                                        onValueChange = { /* TODO */ },
                                        textStyle = textFieldStyle,
                                        cursorBrush = SolidColor(colors.onSurface),
                                        decorationBox = decorationBox
                                    )
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
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(120.dp),
                shape = RoundedCornerShape(24.dp),
                onClick = {
                    scope.launch {
                        sheetState.show()
                    }
                }
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(12.dp))
                Text("Add Exercise")
            }
        }
    }
}