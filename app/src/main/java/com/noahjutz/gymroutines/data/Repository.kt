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

    suspend fun insert(set: RoutineSet): Long {
        return routineDao.insert(set)
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
                routineDao.delete(setGroup)
            }
        }
    }
}

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    val workouts = workoutDao.getWorkouts()

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

    suspend fun delete(workout: WorkoutWithSetGroups) {
        workoutDao.delete(workout.workout)
        for (setGroup in workout.setGroups) {
            workoutDao.delete(setGroup.group)
            for (set in setGroup.sets) {
                workoutDao.delete(set)
            }
        }
    }

    suspend fun insertRoutineAsWorkout(routine: RoutineWithSetGroups): Long {
        // TODO
        return -1L
    }
}
