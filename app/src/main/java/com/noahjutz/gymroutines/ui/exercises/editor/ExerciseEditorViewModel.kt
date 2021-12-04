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

    private var _originalExercise = MutableStateFlow<Exercise?>(null)
    private val _currentExercise = MutableStateFlow<Exercise?>(Exercise(exerciseId = exerciseId))

    val isSavingEnabled = combine(
        _originalExercise,
        _currentExercise,
        name,
    ) { old, current, name ->
        val isExerciseEqual = current == old
        val isNameBlank = name.isBlank()
        !isExerciseEqual && !isNameBlank
    }

    init {
        viewModelScope.launch {
            _originalExercise.value = repository.getExercise(exerciseId)

            _originalExercise.value?.let {
                _name.value = it.name
                _notes.value = it.notes
                _logReps.value = it.logReps
                _logWeight.value = it.logWeight
                _logTime.value = it.logTime
                _logDistance.value = it.logDistance
            }

            launch {
                combine(logReps, logWeight, logTime, logDistance) { r, w, t, d ->
                    !(r || w || t || d)
                }.collect { isNoneLogged ->
                    if (isNoneLogged) _logReps.value = true
                }
            }

            launch {
                name.collect {
                    _currentExercise.value = _currentExercise.value?.copy(name = it)
                }
            }
            launch {
                notes.collect {
                    _currentExercise.value = _currentExercise.value?.copy(notes = it)
                }
            }
            launch {
                logReps.collect {
                    _currentExercise.value = _currentExercise.value?.copy(logReps = it)
                }
            }
            launch {
                logWeight.collect {
                    _currentExercise.value = _currentExercise.value?.copy(logWeight = it)
                }
            }
            launch {
                logTime.collect {
                    _currentExercise.value = _currentExercise.value?.copy(logTime = it)
                }
            }
            launch {
                logDistance.collect {
                    _currentExercise.value = _currentExercise.value?.copy(logDistance = it)
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
                repository.insert(exercise.copy(exerciseId = 0))
            } else {
                repository.update(exercise.copy(exerciseId = exerciseId))
            }
        }.invokeOnCompletion {
            onComplete()
        }
    }
}
