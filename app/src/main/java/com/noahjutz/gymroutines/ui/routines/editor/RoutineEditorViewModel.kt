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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.ExerciseRepository
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.domain.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RoutineEditorViewModel(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    routineId: Int,
) : ViewModel() {
    val routine = routineRepository.getRoutineWithSetGroups(routineId)
    private val _routineFlow = routineRepository.getRoutineFlow(routineId)
    private var _routine: Routine? = null
    private val _setsFlow = routineRepository.getSetsFlow(routineId)
    private var _sets = emptyList<RoutineSet>()
    private val _setGroupsFlow = routineRepository.getSetGroupsFlow(routineId)
    private var _setGroups = emptyList<RoutineSetGroup>()

    init {
        viewModelScope.launch {
            launch {
                _routineFlow.collect { _routine = it }
            }
            launch {
                _setsFlow.collect { _sets = it }
            }
            launch {
                _setGroupsFlow.collect { _setGroups = it }
            }
        }
    }

    fun getExercise(exerciseId: Int) = exerciseRepository.getExercise(exerciseId)

    fun updateName(name: String) {
        _routine?.let {
            viewModelScope.launch {
                routineRepository.insert(it.copy(name = name))
            }
        }
    }

    fun deleteSet(set: RoutineSet) {
        viewModelScope.launch {
            routineRepository.delete(set)
        }
    }

    fun addSet(setGroup: RoutineSetGroupWithSets) {
        viewModelScope.launch {
            val lastSet = setGroup.sets.lastOrNull()
            routineRepository.insert(
                RoutineSet(
                    groupId = setGroup.group.id,
                    position = setGroup.sets.size,
                    reps = lastSet?.reps,
                    weight = lastSet?.weight,
                    time = lastSet?.time,
                    distance = lastSet?.distance,
                )
            )
        }
    }

    fun addExercises(exercises: List<Exercise>) {
        _routine?.let { routine ->
            viewModelScope.launch {
                for (exercise in exercises) {
                    val setGroup = RoutineSetGroup(
                        exerciseId = exercise.exerciseId,
                        routineId = routine.routineId,
                        position = _setGroups.size,
                    )
                    val groupId = routineRepository.insert(setGroup)
                    val set = RoutineSet(
                        groupId = groupId.toInt(),
                        position = 0
                    )
                    routineRepository.insert(set)
                }
            }
        }
    }

    fun swapSetGroups(id1: Int, id2: Int) {
        viewModelScope.launch {
            val g1 = routineRepository.getSetGroup(id1)
            val g2 = routineRepository.getSetGroup(id2)
            if (g1 != null && g2 != null) {
                val newG1 = g1.copy(position = g2.position)
                val newG2 = g2.copy(position = g1.position)
                routineRepository.insert(newG1)
                routineRepository.insert(newG2)
            }
        }
    }

    fun updateReps(set: RoutineSet, reps: Int?) {
        viewModelScope.launch {
            routineRepository.insert(set.copy(reps = reps))
        }
    }
}
