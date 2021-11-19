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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RoutineEditorViewModel(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    routineId: Int,
) : ViewModel() {
    private val _routine =
        MutableStateFlow(runBlocking { routineRepository.getRoutine(routineId)!! })
    val editor = Editor()
    val presenter = Presenter()

    init {
        viewModelScope.launch {
            _routine.collectLatest {
                routineRepository.insert(it)
            }
        }
    }

    inner class Editor {
        fun setName(name: String) {
            _routine.value = _routine.value.copy(name = name)
        }

        // TODO reimplement editing sets with ExerciseSets instead of SetGroups
    }

    inner class Presenter {
        val routine = _routine.asStateFlow()

        fun getExercise(exerciseId: Int) = exerciseRepository.getExercise(exerciseId)
    }
}
