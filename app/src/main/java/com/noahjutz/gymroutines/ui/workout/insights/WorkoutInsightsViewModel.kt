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

package com.noahjutz.gymroutines.ui.workout.insights

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.WorkoutRepository
import com.noahjutz.gymroutines.data.domain.Workout
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WorkoutInsightsViewModel(
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
    preferences: DataStore<Preferences>,
) : ViewModel() {
    val workouts = workoutRepository.workouts.combine(preferences.data) { workouts, prefs ->
        workouts.filter {
            prefs[AppPrefs.CurrentWorkout.key] != it.workoutId
        }
    }
    val routineNames = workouts.map { workouts ->
        workouts.associate {
            Pair(it.workoutId, getRoutineName(it.routineId))
        }
    }

    fun delete(workout: Workout) = viewModelScope.launch {
        workoutRepository.delete(workout)
    }

    private suspend fun getRoutineName(routineId: Int): String {
        val routine = routineRepository.getRoutine(routineId)
        return routine?.name ?: ""
    }
}
