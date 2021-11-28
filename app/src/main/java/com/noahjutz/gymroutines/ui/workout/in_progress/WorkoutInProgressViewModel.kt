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

package com.noahjutz.gymroutines.ui.workout.in_progress

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.ExerciseRepository
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.WorkoutRepository
import com.noahjutz.gymroutines.data.domain.WorkoutWithSetGroups
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class WorkoutInProgressViewModel(
    private val preferences: DataStore<Preferences>,
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    workoutId: Int,
) : ViewModel() {
    val workout = workoutRepository.getWorkoutFlow(workoutId)
    private var _workout: WorkoutWithSetGroups? = null

    init {
        viewModelScope.launch {
            launch {
                workout.collect { workout ->
                    _workout = workout
                }
            }
            launch {
                while (true) {
                    setEndTime(Calendar.getInstance().time)
                    delay(60000)
                }
            }
        }
    }

    private fun setEndTime(endTime: Date) {
        _workout?.workout?.let { workout ->
            viewModelScope.launch {
                workoutRepository.insert(workout.copy(endTime = endTime))
            }
        }
    }

    fun cancelWorkout(onCompletion: () -> Unit) {
        _workout?.let { workout ->
            viewModelScope.launch {
                workoutRepository.delete(workout)
                preferences.edit { it[AppPrefs.CurrentWorkout.key] = -1 }
                onCompletion()
            }
        }
    }

    fun finishWorkout(onCompletion: () -> Unit) {
        viewModelScope.launch {
            preferences.edit { it[AppPrefs.CurrentWorkout.key] = -1 }
            onCompletion()
        }
    }
}
