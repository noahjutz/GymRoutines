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

package com.noahjutz.gymroutines.ui.exercises.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.ExerciseRepository
import com.noahjutz.gymroutines.data.domain.Exercise
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.log

class ExerciseEditorViewModel(
    private val repository: ExerciseRepository,
    private val exerciseId: Int,
) : ViewModel() {
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _logReps = MutableStateFlow(false)
    val logReps = _logReps.asStateFlow()

    private val _logWeight = MutableStateFlow(false)
    val logWeight = _logWeight.asStateFlow()

    private val _logTime = MutableStateFlow(false)
    val logTime = _logTime.asStateFlow()

    private val _logDistance = MutableStateFlow(false)
    val logDistance = _logDistance.asStateFlow()

    init {
        viewModelScope.launch {
            val exercise = repository.getExercise(exerciseId)

            if (exercise != null) {
                _name.value = exercise.name
                _notes.value = exercise.notes
                _logReps.value = exercise.logReps
                _logWeight.value = exercise.logWeight
                _logTime.value = exercise.logTime
                _logDistance.value = exercise.logDistance
            }

            launch {
                combine(logReps, logWeight, logTime, logDistance) { r, w, t, d ->
                    !(r || w || t || d)
                }.collect { isNoneLogged ->
                    if (isNoneLogged) _logReps.value = true
                }
            }
        }
    }

    fun setName(name: String) {
        _name.value = name
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }

    fun setLogReps(logReps: Boolean) {
        _logReps.value = logReps
    }

    fun setLogWeight(logWeight: Boolean) {
        _logWeight.value = logWeight
    }

    fun setLogTime(logTime: Boolean) {
        _logTime.value = logTime
    }

    fun setLogDistance(logDistance: Boolean) {
        _logDistance.value = logDistance
    }

    fun save(onComplete: () -> Unit) {
        viewModelScope.launch {
            val exercise = Exercise(
                name = name.value,
                notes = notes.value,
                logReps = logReps.value,
                logWeight = logWeight.value,
                logTime = logTime.value,
                logDistance = logDistance.value,
                hidden = false,
            )
            if (exerciseId < 0) {
                repository.insert(exercise)
            } else {
                repository.update(exercise.copy(exerciseId = exerciseId))
            }
        }.invokeOnCompletion {
            onComplete()
        }
    }
}
