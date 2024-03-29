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

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.ExerciseRepository
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.WorkoutRepository
import com.noahjutz.gymroutines.data.domain.Exercise
import com.noahjutz.gymroutines.data.domain.WorkoutSet
import com.noahjutz.gymroutines.data.domain.WorkoutSetGroup
import com.noahjutz.gymroutines.data.domain.WorkoutSetGroupWithSets
import com.noahjutz.gymroutines.data.domain.WorkoutWithSetGroups
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class WorkoutInProgressViewModel(
    private val preferences: DataStore<Preferences>,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    private val application: Application,
    workoutId: Int,
) : ViewModel() {
    val workout = workoutRepository.getWorkoutFlow(workoutId)
    private var _workout: WorkoutWithSetGroups? = null

    val routineName = workout.map {
        it?.workout?.routineId?.let { routineId ->
            routineRepository.getRoutine(routineId)?.name
        } ?: application.resources.getString(R.string.unnamed_routine)
    }

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
                    delay(1000)
                }
            }
        }
    }

    fun getExercise(exerciseId: Int): Flow<Exercise?> {
        return exerciseRepository.getExerciseFlow(exerciseId)
    }

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            workoutRepository.delete(set)
        }
    }

    fun addSet(setGroup: WorkoutSetGroupWithSets) {
        viewModelScope.launch {
            val lastSet = setGroup.sets.lastOrNull()
            workoutRepository.insert(
                WorkoutSet(
                    groupId = setGroup.group.id,
                    reps = lastSet?.reps,
                    weight = lastSet?.weight,
                    time = lastSet?.time,
                    distance = lastSet?.distance,
                )
            )
        }
    }

    fun addExercises(exerciseIds: List<Int>) {
        _workout?.let { workout ->
            viewModelScope.launch {
                for (exerciseId in exerciseIds) {
                    val setGroup = WorkoutSetGroup(
                        exerciseId = exerciseId,
                        workoutId = workout.workout.workoutId,
                        position = workout.setGroups.size,
                    )
                    val groupId = workoutRepository.insert(setGroup)
                    val set = WorkoutSet(
                        groupId = groupId.toInt(),
                    )
                    workoutRepository.insert(set)
                }
            }
        }
    }

    fun swapSetGroups(id1: Int, id2: Int) {
        viewModelScope.launch {
            val g1 = workoutRepository.getSetGroup(id1)
            val g2 = workoutRepository.getSetGroup(id2)
            if (g1 != null && g2 != null) {
                val newG1 = g1.copy(position = g2.position)
                val newG2 = g2.copy(position = g1.position)
                workoutRepository.update(newG1)
                workoutRepository.update(newG2)
            }
        }
    }

    fun updateReps(set: WorkoutSet, reps: Int?) {
        viewModelScope.launch {
            workoutRepository.update(set.copy(reps = reps))
        }
    }

    fun updateWeight(set: WorkoutSet, weight: Double?) {
        viewModelScope.launch {
            workoutRepository.update(set.copy(weight = weight))
        }
    }

    fun updateTime(set: WorkoutSet, time: Int?) {
        viewModelScope.launch {
            workoutRepository.update(set.copy(time = time))
        }
    }

    fun updateDistance(set: WorkoutSet, distance: Double?) {
        viewModelScope.launch {
            workoutRepository.update(set.copy(distance = distance))
        }
    }

    fun updateChecked(set: WorkoutSet, checked: Boolean) {
        viewModelScope.launch {
            workoutRepository.update(set.copy(complete = checked))
        }
    }

    private fun setEndTime(endTime: Date) {
        _workout?.workout?.let { workout ->
            viewModelScope.launch {
                workoutRepository.update(workout.copy(endTime = endTime))
            }
        }
    }

    fun cancelWorkout(onCompletion: () -> Unit) {
        _workout?.let { workout ->
            viewModelScope.launch {
                workoutRepository.delete(workout.workout)
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
