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
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(private val exerciseDao: ExerciseDao) {
    val exercises = exerciseDao.getExercises()

    suspend fun insert(exercise: Exercise): Long {
        return exerciseDao.insert(exercise)
    }

    suspend fun update(exercise: Exercise) {
        exerciseDao.update(exercise)
    }

    fun getExerciseFlow(exerciseId: Int): Flow<Exercise?> {
        return exerciseDao.getExerciseFlow(exerciseId)
    }

    suspend fun getExercise(id: Int): Exercise? {
        return exerciseDao.getExercise(id)
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

    suspend fun getSetGroupsInRoutine(routineId: Int): List<RoutineSetGroup> {
        return routineDao.getSetGroupsInRoutine(routineId)
    }

    fun getSetsFlow(routineId: Int): Flow<List<RoutineSet>> {
        return routineDao.getSetsFlow(routineId)
    }

    suspend fun getSetsInRoutine(routineId: Int): List<RoutineSet> {
        return routineDao.getSetsInRoutine(routineId)
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

        val nextSetGroups = routineDao.getSetGroupsInRoutine(setGroup.routineId)
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

    suspend fun update(workout: Workout) {
        workoutDao.update(workout)
    }

    suspend fun insert(setGroup: WorkoutSetGroup): Long {
        return workoutDao.insert(setGroup)
    }

    suspend fun update(setGroup: WorkoutSetGroup) {
        workoutDao.update(setGroup)
    }

    suspend fun insert(set: WorkoutSet): Long {
        return workoutDao.insert(set)
    }

    suspend fun update(set: WorkoutSet) {
        workoutDao.update(set)
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

    suspend fun getWorkoutWithSetGroups(workoutId: Int): WorkoutWithSetGroups? {
        return workoutDao.getWorkoutWithSetGroups(workoutId)
    }

    suspend fun getSetsInWorkout(workoutId: Int): List<WorkoutSet> {
        return workoutDao.getSetsInWorkout(workoutId)
    }

    suspend fun getSetGroupsInWorkout(workoutId: Int): List<WorkoutSetGroup> {
        return workoutDao.getSetGroupsInWorkout(workoutId)
    }

    fun getWorkoutFlow(workoutId: Int): Flow<WorkoutWithSetGroups?> {
        return workoutDao.getWorkoutWithSetGroupsFlow(workoutId)
    }

    suspend fun delete(workout: Workout) {
        workoutDao.delete(workout)
    }

    suspend fun delete(setGroup: WorkoutSetGroup) {
        workoutDao.delete(setGroup)

        val nextSetGroups = workoutDao.getSetGroups(setGroup.workoutId)
            .filter { it.position > setGroup.position }

        for (group in nextSetGroups) {
            workoutDao.update(group.copy(position = group.position - 1))
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
