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

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class ExerciseSetLegacy(
    val exerciseId: Int,
    val reps: Int? = null,
    val weight: Double? = null,
    val time: Int? = null,
    val distance: Double? = null,
    val complete: Boolean = false,
)

@Entity(tableName = "routine_set_table")
data class RoutineSet(
    val routineId: Int,
    val exerciseId: Int,
    val position: Int,
    val reps: Int? = null,
    val weight: Double? = null,
    val time: Int? = null,
    val distance: Double? = null,

    @PrimaryKey(autoGenerate = true)
    val routineSetId: Int = 0
)

@Entity(tableName = "workout_set_table")
data class WorkoutSet(
    val workoutId: Int,
    val exerciseId: Int,
    val position: Int,
    val reps: Int? = null,
    val weight: Double? = null,
    val time: Int? = null,
    val distance: Double? = null,
    val complete: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    val workoutSetId: Int = 0
)