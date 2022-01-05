package com.noahjutz.gymroutines.ui.workout.completed

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.WorkoutRepository
import com.noahjutz.gymroutines.data.domain.Routine
import com.noahjutz.gymroutines.data.domain.Workout
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WorkoutCompletedViewModel(
    private val workoutId: Int,
    private val preferences: DataStore<Preferences>,
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {
    val isUpdateRoutineChecked = preferences.data.map {
        it[AppPrefs.UpdateRoutineAfterWorkout.key] ?: false
    }

    private var _workout: Workout? = null
    private var _routine: Routine? = null

    init {
        viewModelScope.launch {
            _workout = workoutRepository.getWorkout(workoutId)
            _workout?.let { workout ->
                _routine = routineRepository.getRoutine(workout.routineId)
            }
        }
    }

    fun startWorkout() {
        viewModelScope.launch {
            preferences.edit {
                it[AppPrefs.CurrentWorkout.key] = workoutId
            }
        }
    }

    fun updateRoutine() {
        viewModelScope.launch {
            preferences.edit {
                it[AppPrefs.UpdateRoutineAfterWorkout.key] = true
                // TODO update routine in db
            }
        }
    }

    fun resetRoutine() {
        viewModelScope.launch {
            preferences.edit {
                it[AppPrefs.UpdateRoutineAfterWorkout.key] = false
                // TODO reset routine in db
            }
        }
    }
}
