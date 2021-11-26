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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RoutineEditorViewModel(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    routineId: Int,
) : ViewModel() {
    private val _routine = MutableStateFlow<Routine?>(null)
    val routine = _routine.asStateFlow()
    private val _setGroups = MutableStateFlow<List<RoutineSetGroup>>(emptyList())
    val setGroups = _setGroups.asStateFlow()
    private val _sets = MutableStateFlow<List<RoutineSet>>(emptyList())
    val sets = _sets.asStateFlow()

    init {
        viewModelScope.launch {
            _routine.value = routineRepository.getRoutine(routineId)
            _setGroups.value = routineRepository.getSetGroups(routineId)
            _sets.value = routineRepository.getSets(routineId)

            routine.collect { routine ->
                if (routine != null) {
                    routineRepository.insert(routine)
                }
            }
            setGroups.collect { setGroups ->
                routineRepository.insertSetGroups(setGroups)
            }
            sets.collect { sets ->
                routineRepository.insertSets(sets)
            }
        }
    }

    fun getExercise(exerciseId: Int) = exerciseRepository.getExercise(exerciseId)

    fun updateName(name: String) {
        viewModelScope.launch {
            _routine.value = _routine.value?.copy(name = name)
        }
    }
}
