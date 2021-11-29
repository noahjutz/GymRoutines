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

import androidx.room.*
import kotlinx.serialization.Serializable

// Temporary class for SetGroupCard. TODO remove and use RoutineSet/WorkoutSet
@Serializable
data class ExerciseSetLegacy(
    val exerciseId: Int,
    val reps: Int? = null,
    val weight: Double? = null,
    val time: Int? = null,
    val distance: Double? = null,
    val complete: Boolean = false,
    val position: Int,
    val setId: Int,
)

@Entity(
    tableName = "routine_set_table",
    foreignKeys = [
        ForeignKey(
            entity = RoutineSetGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RoutineSet(
    val groupId: Int,
    val position: Int,
    val reps: Int? = null,
    val weight: Double? = null,
    val time: Int? = null,
    val distance: Double? = null,

    @PrimaryKey(autoGenerate = true)
    val routineSetId: Int = 0
)

@Entity(
    tableName = "routine_set_group_table",
    foreignKeys = [
        ForeignKey(
            entity = Routine::class,
            parentColumns = ["routineId"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RoutineSetGroup(
    val routineId: Int,
    val exerciseId: Int,
    val position: Int,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

data class RoutineSetGroupWithSets(
    @Embedded val group: RoutineSetGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    ) val sets: List<RoutineSet>
)

@Entity(
    tableName = "workout_set_table",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSetGroup::class,
            childColumns = ["groupId"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutSet(
    val groupId: Int,
    val position: Int,
    val reps: Int? = null,
    val weight: Double? = null,
    val time: Int? = null,
    val distance: Double? = null,
    val complete: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    val workoutSetId: Int = 0
)

@Entity(
    tableName = "workout_set_group_table",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            childColumns = ["workoutId"],
            parentColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutSetGroup(
    val workoutId: Int,
    val exerciseId: Int,
    val position: Int,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

data class WorkoutSetGroupWithSets(
    @Embedded val group: WorkoutSetGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    ) val sets: List<WorkoutSet>
)
