package com.noahjutz.gymroutines.ui.workout.completed

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.WorkoutRepository
import kotlinx.coroutines.launch

class WorkoutCompletedViewModel(
    private val workoutId: Int,
    private val preferences: DataStore<Preferences>,
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {
    fun startWorkout() {
        viewModelScope.launch {
            preferences.edit {
                it[AppPrefs.CurrentWorkout.key] = workoutId
            }
        }
    }

    fun updateRoutine() {
        // TODO
    }

    fun resetRoutine() {
        // TODO
    }
}
