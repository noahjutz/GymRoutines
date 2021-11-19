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
import com.noahjutz.gymroutines.data.domain.*
import com.noahjutz.gymroutines.util.minus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class WorkoutInProgressViewModel(
    private val preferences: DataStore<Preferences>,
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    workoutId: Int,
    routineId: Int,
) : ViewModel() {
    private val _workout = MutableStateFlow(
        runBlocking {
            workoutRepository.getWorkout(workoutId)
                ?: routineRepository.getRoutine(routineId)?.let {
                    workoutRepository.getWorkout(workoutRepository.insert(it.toWorkout()).toInt())
                }
                ?: workoutRepository.insert(Workout()).let {
                    workoutRepository.getWorkout(it.toInt())
                }!!
        }
    )
    val presenter = Presenter()
    val editor = Editor()

    init {
        viewModelScope.launch {
            preferences.edit { // TODO move to CreateRoutine when "start workout" is tapped
                it[AppPrefs.CurrentWorkout.key] = _workout.value.workoutId
            }
            _workout.collectLatest {
                workoutRepository.insert(_workout.value)
            }
        }
    }

    inner class Editor {
        fun setEndTime(endTime: Date) {
            _workout.value = _workout.value.copy(endTime = endTime)
        }

        // TODO reimplement SetGroup update functions with ExerciseSet

        fun deleteWorkout() {
            viewModelScope.launch {
                workoutRepository.delete(_workout.value)
            }
        }

        suspend fun finishWorkout() {
            setEndTime(Calendar.getInstance().time)
            preferences.edit { it[AppPrefs.CurrentWorkout.key] = -1 }
        }

        suspend fun cancelWorkout() {
            deleteWorkout()
            preferences.edit { it[AppPrefs.CurrentWorkout.key] = -1 }
        }
    }

    inner class Presenter {
        val workout = _workout.asStateFlow()
        private val currentTime = flow {
            while (true) {
                emit(Calendar.getInstance().time)
                delay(1000)
            }
        }

        @OptIn(ExperimentalTime::class)
        val duration =
            currentTime.map { Duration.seconds((it - workout.value.startTime).inWholeSeconds) }

        @OptIn(ExperimentalTime::class)
        val durationString = duration.map {
            it.toComponents { hours, minutes, seconds, _ ->
                val hoursString = if (hours > 9) hours.toString() else "0$hours"
                val minutesString = if (minutes > 9) minutes.toString() else "0$minutes"
                val secondsString = if (seconds > 9) seconds.toString() else "0$seconds"
                "$hoursString:$minutesString:$secondsString"
            }
        }

        fun getExercise(exerciseId: Int) = exerciseRepository.getExercise(exerciseId)
    }
}
