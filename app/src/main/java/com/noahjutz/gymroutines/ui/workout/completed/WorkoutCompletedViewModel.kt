package com.noahjutz.gymroutines.ui.workout.completed

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.WorkoutRepository
import com.noahjutz.gymroutines.data.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map

class WorkoutCompletedViewModel(
    private val workoutId: Int,
    private val routineId: Int,
    private val preferences: DataStore<Preferences>,
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {

    val isUpdateRoutineChecked = preferences.data.map {
        it[AppPrefs.UpdateRoutineAfterWorkout.key] ?: false
    }

    fun startWorkout() {
        viewModelScope.launch {
            preferences.edit {
                it[AppPrefs.CurrentWorkout.key] = workoutId
            }
        }
    }

    fun setUpdateRoutine(updateRoutine: Boolean) {
        viewModelScope.launch {
            preferences.edit {
                it[AppPrefs.UpdateRoutineAfterWorkout.key] = updateRoutine
            }
            // TODO doesn't automatically run if preference is true initially
            if (updateRoutine) {
                updateRoutine()
            } else {
                // TODO
            }
        }
    }

    private suspend fun updateRoutine() {
        val routineSetGroups = routineRepository.getSetGroupsInRoutine(routineId)
        for (setGroup in routineSetGroups) {
            routineRepository.delete(setGroup)
        }

        val workoutSetGroups = workoutRepository.getSetGroupsInWorkout(workoutId)
        val workoutSets = workoutRepository.getSetsInWorkout(workoutId)
        for (setGroup in workoutSetGroups) {
            val routineSetGroup = RoutineSetGroup(
                routineId = routineId,
                exerciseId = setGroup.exerciseId,
                position = setGroup.position,
            )
            val groupId = routineRepository.insert(routineSetGroup)
            for (set in workoutSets.filter { it.groupId == setGroup.id }) {
                val routineSet = RoutineSet(
                    groupId = groupId.toInt(),
                    reps = set.reps,
                    weight = set.weight,
                    time = set.time,
                    distance = set.distance,
                )
                routineRepository.insert(routineSet)
            }
        }

    }

    private suspend fun revertRoutine() {
        val currentSetGroups = routineRepository.getSetGroupsInRoutine(routineId)
    }
}
