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

package com.noahjutz.gymroutines.ui.exercises.picker

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.ui.components.SearchBar
import org.koin.androidx.compose.getViewModel

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun ExercisePickerSheet(
    viewModel: ExercisePickerViewModel = getViewModel(),
    onExercisesSelected: (List<Int>) -> Unit,
    navToExerciseEditor: () -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            val selectedExerciseIds by viewModel.selectedExerciseIds.collectAsState(initial = emptyList())
            if (selectedExerciseIds.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        onExercisesSelected(selectedExerciseIds)
                        viewModel.clearExercises()
                    }
                ) {
                    Icon(Icons.Default.Done, stringResource(R.string.pick_exercise))
                }
            }
        }
    ) { paddingValues ->
        val allExercises by viewModel.allExercises.collectAsState(emptyList())
        Column(Modifier.padding(paddingValues)) {
            val searchQuery by viewModel.nameFilter.collectAsState()
            SearchBar(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                value = searchQuery,
                onValueChange = viewModel::search
            )
            LazyColumn(Modifier.weight(1f)) {
                items(allExercises.filter { !it.hidden }) { exercise ->
                    val checked by viewModel.exercisesContains(exercise)
                        .collectAsState(initial = false)
                    ListItem(
                        Modifier.toggleable(
                            value = checked,
                            onValueChange = {
                                if (it) viewModel.addExercise(exercise)
                                else viewModel.removeExercise(exercise)
                            }
                        ),
                        icon = { Checkbox(checked = checked, onCheckedChange = null) },
                    ) {
                        Text(
                            exercise.name.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.unnamed_exercise)
                        )
                    }
                }

                item {
                    ListItem(
                        modifier = Modifier.clickable(onClick = navToExerciseEditor),
                        icon = { Icon(Icons.Default.Add, null, tint = colors.primary) },
                        text = {
                            Text(stringResource(R.string.new_exercise),
                                color = colors.primary)
                        },
                    )
                    Spacer(Modifier.height(70.dp))
                }
            }
        }
    }
}
