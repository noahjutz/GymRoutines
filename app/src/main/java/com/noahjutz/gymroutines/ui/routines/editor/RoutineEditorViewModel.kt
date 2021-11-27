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
import com.noahjutz.gymroutines.data.domain.Routine
import com.noahjutz.gymroutines.data.domain.RoutineSet
import com.noahjutz.gymroutines.data.domain.RoutineSetGroup
import com.noahjutz.gymroutines.data.domain.RoutineSetGroupWithSets
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
}
