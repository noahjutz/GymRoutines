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

package com.noahjutz.gymroutines.data

import com.noahjutz.gymroutines.data.dao.ExerciseDao
import com.noahjutz.gymroutines.data.dao.RoutineDao
import com.noahjutz.gymroutines.data.dao.WorkoutDao
import com.noahjutz.gymroutines.data.domain.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ExerciseRepository(private val exerciseDao: ExerciseDao) {
    val exercises = exerciseDao.getExercises()

    fun insert(exercise: Exercise) = runBlocking {
        withContext(IO) {
            exerciseDao.insert(exercise)
        }
    }

    fun getExerciseFlow(exerciseId: Int): Flow<Exercise?> {
        return exerciseDao.getExerciseFlow(exerciseId)
    }

    fun getExercise(id: Int): Exercise? = runBlocking {
        withContext(IO) {
            exerciseDao.getExercise(id)
        }
    }
}

class RoutineRepository(private val routineDao: RoutineDao) {
    val routines = routineDao.getRoutines()

    suspend fun getRoutine(routineId: Int): Routine? {
        return routineDao.getRoutineOld(routineId)
    }

    fun getRoutineFlow(routineId: Int): Flow<Routine?> {
        return routineDao.getRoutineFlow(routineId)
    }

    fun getSetGroupsFlow(routineId: Int): Flow<List<RoutineSetGroup>> {
        return routineDao.getSetGroupsFlow(routineId)
    }

    fun getSetsFlow(routineId: Int): Flow<List<RoutineSet>> {
        return routineDao.getSetsFlow(routineId)
    }

    suspend fun getRoutineWithSetGroupsOld(routineId: Int): RoutineWithSetGroups? {
        return routineDao.getRoutineWithSetGroupsOld(routineId)
    }

    fun getRoutineWithSetGroups(routineId: Int): Flow<RoutineWithSetGroups?> {
        return routineDao.getRoutineWithSetGroups(routineId)
    }

    suspend fun insert(routine: Routine): Long {
        return routineDao.insert(routine)
    }

    suspend fun update(routine: Routine) {
        routineDao.update(routine)
    }

    suspend fun update(set: RoutineSet) {
        routineDao.update(set)
    }

    suspend fun update(setGroup: RoutineSetGroup) {
        routineDao.update(setGroup)
    }

    suspend fun insert(set: RoutineSet): Long {
        return routineDao.insert(set)
    }

    suspend fun insert(setGroup: RoutineSetGroup): Long {
        return routineDao.insert(setGroup)
    }

    suspend fun insertWorkoutAsRoutine(workoutWithSetGroups: WorkoutWithSetGroups): Long {
        // TODO
        return -1L
    }

    // TODO replace with delete(routine: RoutineWithSetGroups)
    suspend fun delete(routine: Routine) {
        routineDao.delete(routine)
    }

    suspend fun delete(set: RoutineSet) {
        routineDao.delete(set)

        if (routineDao.getSetsInGroup(set.groupId).isEmpty()) {
            routineDao.getSetGroup(set.groupId)?.let { setGroup ->
                delete(setGroup)
            }
        }
    }

    suspend fun delete(setGroup: RoutineSetGroup) {
        routineDao.delete(setGroup)

        val nextSetGroups = routineDao.getSetGroups(setGroup.routineId)
            .filter { it.position > setGroup.position }

        for (group in nextSetGroups) {
            routineDao.update(group.copy(position = group.position - 1))
        }
    }

    suspend fun getSetGroup(id: Int): RoutineSetGroup? {
        return routineDao.getSetGroup(id)
    }
}

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    val workouts = workoutDao.getWorkouts()

    suspend fun insert(workout: Workout): Long {
        return workoutDao.insert(workout)
    }

    suspend fun insert(setGroup: WorkoutSetGroup): Long {
        return workoutDao.insert(setGroup)
    }

    suspend fun insert(set: WorkoutSet): Long {
        return workoutDao.insert(set)
    }

    suspend fun insert(workout: WorkoutWithSetGroups): Long {
        for (setGroup in workout.setGroups) {
            workoutDao.insert(setGroup.group)
            for (set in setGroup.sets) {
                workoutDao.insert(set)
            }
        }
        return workoutDao.insert(workout.workout)
    }

    suspend fun getWorkout(workoutId: Int): WorkoutWithSetGroups? {
        return workoutDao.getWorkoutWithSetGroups(workoutId)
    }

    fun getWorkoutFlow(workoutId: Int): Flow<WorkoutWithSetGroups?> {
        return workoutDao.getWorkoutWithSetGroupsFlow(workoutId)
    }

    suspend fun delete(workout: WorkoutWithSetGroups) {
        workoutDao.delete(workout.workout)
        for (setGroup in workout.setGroups) {
            workoutDao.delete(setGroup.group)
            for (set in setGroup.sets) {
                workoutDao.delete(set)
            }
        }
    }

    suspend fun delete(setGroup: WorkoutSetGroup) {
        workoutDao.delete(setGroup)

        val nextSetGroups = workoutDao.getSetGroups(setGroup.workoutId)
            .filter { it.position > setGroup.position }

        for (group in nextSetGroups) {
            workoutDao.insert(group.copy(position = group.position - 1))
        }
    }

    suspend fun getSetGroup(id: Int): WorkoutSetGroup? {
        return workoutDao.getSetGroup(id)
    }

    suspend fun delete(set: WorkoutSet) {
        workoutDao.delete(set)
        if (workoutDao.getSetsInGroup(set.groupId).isEmpty()) {
            workoutDao.getSetGroup(set.groupId)?.let { setGroup ->
                delete(setGroup)
            }
        }
    }
}
