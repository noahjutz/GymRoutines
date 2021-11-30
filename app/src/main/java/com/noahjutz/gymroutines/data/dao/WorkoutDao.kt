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

package com.noahjutz.gymroutines.data.dao

import androidx.room.*
import com.noahjutz.gymroutines.data.domain.Workout
import com.noahjutz.gymroutines.data.domain.WorkoutSet
import com.noahjutz.gymroutines.data.domain.WorkoutSetGroup
import com.noahjutz.gymroutines.data.domain.WorkoutWithSetGroups
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insert(workout: Workout): Long

    @Update
    suspend fun update(workout: Workout)

    @Insert
    suspend fun insert(workoutSet: WorkoutSet): Long

    @Update
    suspend fun update(workoutSet: WorkoutSet)

    @Insert
    suspend fun insert(workoutSetGroup: WorkoutSetGroup): Long

    @Update
    suspend fun update(workoutSetGroup: WorkoutSetGroup)

    @Transaction
    @Query("SELECT * FROM workout_table WHERE workoutId == :id")
    suspend fun getWorkoutWithSetGroups(id: Int): WorkoutWithSetGroups?

    @Transaction
    @Query("SELECT * FROM workout_table WHERE workoutId == :id")
    fun getWorkoutWithSetGroupsFlow(id: Int): Flow<WorkoutWithSetGroups?>

    @Transaction
    @Query("SELECT * FROM workout_table ORDER BY startTime DESC")
    fun getWorkouts(): Flow<List<WorkoutWithSetGroups>>

    @Delete
    suspend fun delete(workout: Workout)

    @Delete
    suspend fun delete(workoutSet: WorkoutSet)

    @Delete
    suspend fun delete(workoutSetGroup: WorkoutSetGroup)

    @Query("SELECT * FROM workout_set_group_table WHERE id == :id")
    suspend fun getSetGroup(id: Int): WorkoutSetGroup?

    @Query("SELECT * FROM workout_set_group_table WHERE workoutId == :workoutId")
    suspend fun getSetGroups(workoutId: Int): List<WorkoutSetGroup>

    @Query("SELECT * FROM workout_set_table WHERE groupId == :groupId")
    suspend fun getSetsInGroup(groupId: Int): List<WorkoutSet>
}
