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
import com.noahjutz.gymroutines.data.domain.Routine
import com.noahjutz.gymroutines.data.domain.RoutineSet
import com.noahjutz.gymroutines.data.domain.RoutineSetGroup
import com.noahjutz.gymroutines.data.domain.RoutineWithSetGroups
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routine: Routine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routineSet: RoutineSet): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routineSetGroup: RoutineSetGroup): Long

    @Delete
    suspend fun delete(routine: Routine)

    @Query("SELECT * FROM routine_table")
    fun getRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routine_table WHERE routineId == :routineId")
    suspend fun getRoutineOld(routineId: Int): Routine?

    @Query("SELECT * FROM routine_set_group_table WHERE routineId == :routineId")
    fun getSetGroupsFlow(routineId: Int): Flow<List<RoutineSetGroup>>

    @Query("SELECT * FROM routine_set_table WHERE groupId IN (SELECT id FROM routine_set_group_table WHERE routineId == :routineId)")
    fun getSetsFlow(routineId: Int): Flow<List<RoutineSet>>

    @Transaction
    @Query("SELECT * FROM routine_table WHERE routineId == :routineId")
    suspend fun getRoutineWithSetGroupsOld(routineId: Int): RoutineWithSetGroups?

    @Transaction
    @Query("SELECT * FROM routine_table WHERE routineId == :routineId")
    fun getRoutineWithSetGroups(routineId: Int): Flow<RoutineWithSetGroups?>

    @Query("SELECT * FROM routine_table WHERE routineId == :routineId")
    fun getRoutineFlow(routineId: Int): Flow<Routine?>

    @Delete
    suspend fun delete(set: RoutineSet)

    @Query("SELECT * FROM routine_set_table WHERE groupId == :groupId")
    suspend fun getSetsInGroup(groupId: Int): List<RoutineSet>

    @Delete
    suspend fun delete(setGroup: RoutineSetGroup)

    @Query("SELECT * FROM routine_set_group_table WHERE id == :id")
    suspend fun getSetGroup(id: Int): RoutineSetGroup?
}
