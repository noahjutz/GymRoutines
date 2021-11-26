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

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.ExerciseRepository
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RoutineEditorViewModel(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    routineId: Int,
) : ViewModel() {
    private val routine = MutableStateFlow<Routine?>(null)
    private val setGroups = MutableStateFlow<List<RoutineSetGroup>>(emptyList())
    private val sets = MutableStateFlow<List<RoutineSet>>(emptyList())

    val routineWithSets = combine(routine, setGroups, sets) { routine, setGroups, sets ->
        if (routine != null) {
            RoutineWithSetGroups(
                routine = routine,
                setGroups = setGroups.map { group ->
                    RoutineSetGroupWithSets(
                        group = group,
                        sets = sets.filter { it.groupId == group.id }
                    )
                }
            )
        } else null
    }

    init {
        viewModelScope.launch {
            routine.value = routineRepository.getRoutine(routineId)
            setGroups.value = routineRepository.getSetGroups(routineId)
            sets.value = routineRepository.getSets(routineId)
            launch {
                sets.collect { sets ->
                    routineRepository.deleteSets(routineId)
                    routineRepository.insertSets(sets)
                }
            }
            launch {
                setGroups.collect { setGroups ->
                    routineRepository.deleteSetGroups(routineId)
                    routineRepository.insertSetGroups(setGroups)
                }
            }
            launch {
                routine.collect { routine ->
                    if (routine != null) {
                        routineRepository.insert(routine)
                    }
                }
            }
        }
    }

    fun getExercise(exerciseId: Int) = exerciseRepository.getExercise(exerciseId)

    fun updateName(name: String) {
        routine.value = routine.value?.copy(name = name)
    }

    fun deleteSet(set: RoutineSet) {
        sets.value = sets.value.filterNot { it.routineSetId == set.routineSetId }
    }
}
