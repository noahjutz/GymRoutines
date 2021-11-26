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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
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

    suspend fun getRoutine(routineId: Int): Routine? = routineDao.getRoutine(routineId)
    // TODO remove
    suspend fun getRoutineWithSets(routineId: Int): RoutineWithSets? {
        return routineDao.getRoutine(routineId)?.let {
            RoutineWithSets(it, emptyList())
        }
    }

    suspend fun getRoutineWithSetGroups(routineId: Int): RoutineWithSetGroups? {
        return routineDao.getRoutineWithSetGroups(routineId)
    }

    suspend fun insert(routine: RoutineWithSetGroups): Long {
        // TODO insert setGroups as well
        return routineDao.insert(routine.routine)
    }

    suspend fun insert(routine: Routine): Long {
        return routineDao.insert(routine)
    }

    suspend fun insertWorkoutAsRoutine(workoutWithSets: WorkoutWithSets): Long {
        // TODO
        return -1L
    }

    suspend fun insert(routine: RoutineWithSets): Long = withContext(IO) {
        for (set in routine.sets) {
            routineDao.insert(set)
        }
        routineDao.insert(routine.routine)
    }

    fun delete(routine: Routine) {
        CoroutineScope(IO).launch {
            routineDao.delete(routine)
        }
    }
}

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    suspend fun insert(workout: Workout) = workoutDao.insert(workout)
    suspend fun insert(workout: WorkoutWithSets): Long {
        for (set in workout.sets) workoutDao.insert(set)
        return workoutDao.insert(workout.workout)
    }

    suspend fun getWorkout(workoutId: Int) = workoutDao.getWorkout(workoutId)
    suspend fun getWorkoutWithSets(workoutId: Int) = workoutDao.getWorkoutWithSets(workoutId)
    suspend fun delete(workout: Workout) = workoutDao.delete(workout)
    fun getWorkouts() = workoutDao.getWorkouts()
    suspend fun insertRoutineAsWorkout(routine: RoutineWithSets): Long {
        val workout = Workout(name = routine.routine.name)
        val id = insert(workout)
        //TODO adapt this code for new workout_set_table structure
        //val sets = routine.sets.map {
        //    WorkoutSet(
        //        id.toInt(),
        //        it.exerciseId,
        //        it.position,
        //        it.reps,
        //        it.weight,
        //        it.time,
        //        it.distance,
        //        false
        //    )
        //}
        //for (set in sets) workoutDao.insert(set)
        return id
    }
}
