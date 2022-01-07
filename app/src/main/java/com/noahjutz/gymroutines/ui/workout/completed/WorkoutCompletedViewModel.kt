package com.noahjutz.gymroutines.ui.workout.completed

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.WorkoutRepository
import com.noahjutz.gymroutines.data.domain.RoutineSet
import com.noahjutz.gymroutines.data.domain.RoutineSetGroup
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WorkoutCompletedViewModel(
    private val workoutId: Int,
    private val routineId: Int,
    private val preferences: DataStore<Preferences>,
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {
    private val routineSetGroupsOld = viewModelScope.async {
        routineRepository.getSetGroupsInRoutine(routineId)
    }

    private val routineSetsOld = viewModelScope.async {
        routineRepository.getSetsInRoutine(routineId)
    }

    val isUpdateRoutineChecked = preferences.data.map {
        it[AppPrefs.UpdateRoutineAfterWorkout.key] ?: false
    }

    init {
        viewModelScope.launch {
            if (isUpdateRoutineChecked.first()) {
                updateRoutine()
            }
        }
    }

    fun startWorkout(onCompletion: () -> Unit) {
        viewModelScope.launch {
            preferences.edit {
                it[AppPrefs.CurrentWorkout.key] = workoutId
            }
            revertRoutine()
        }.invokeOnCompletion { onCompletion() }
    }

    fun setUpdateRoutine(updateRoutine: Boolean) {
        viewModelScope.launch {
            preferences.edit {
                it[AppPrefs.UpdateRoutineAfterWorkout.key] = updateRoutine
            }
            if (updateRoutine) updateRoutine() else revertRoutine()
        }
    }

    private suspend fun updateRoutine() {
        awaitAll(routineSetGroupsOld, routineSetsOld)

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
        val routineSetGroupsBackup = routineSetGroupsOld.await()
        val routineSetsBackup = routineSetsOld.await()

        val currentSetGroups = routineRepository.getSetGroupsInRoutine(routineId)
        for (setGroup in currentSetGroups) {
            routineRepository.delete(setGroup)
        }

        for (setGroup in routineSetGroupsBackup) {
            val groupId = routineRepository.insert(setGroup.copy(id = 0))
            for (set in routineSetsBackup.filter { it.groupId == setGroup.id }) {
                routineRepository.insert(set.copy(routineSetId = 0, groupId = groupId.toInt()))
            }
        }
    }
}
