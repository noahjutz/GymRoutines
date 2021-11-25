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

package com.noahjutz.gymroutines.data.domain

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "routine_table")
data class Routine(
    val name: String = "",

    @PrimaryKey(autoGenerate = true)
    var routineId: Int = 0,
)

// temporary, TODO remove
data class RoutineWithSets(
    val routine: Routine,
    val sets: List<RoutineSet> = emptyList(),
)

data class RoutineWithSetGroups(
    @Embedded val routine: Routine,
    @Relation(
        entity = RoutineSetGroup::class,
        parentColumn = "routineId",
        entityColumn = "routineId"
    ) val setGroups: List<RoutineSetGroupWithSets>
)