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

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.noahjutz.gymroutines.data.dao.ExerciseDao
import com.noahjutz.gymroutines.data.dao.RoutineDao
import com.noahjutz.gymroutines.data.dao.WorkoutDao
import com.noahjutz.gymroutines.data.domain.Exercise
import com.noahjutz.gymroutines.data.domain.Routine
import com.noahjutz.gymroutines.data.domain.Workout
import kotlinx.serialization.json.*

@Database(
    entities = [
        Exercise::class,
        Routine::class,
        Workout::class,
    ],
    version = 37,
    autoMigrations = [
        AutoMigration(from = 35, to = 36)
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val exerciseDao: ExerciseDao
    abstract val routineDao: RoutineDao
    abstract val workoutDao: WorkoutDao
}

/**
 * Removes SetGroup, flattening sets within routines.
 */
val MIGRATION_36_37 = object : Migration(36, 37) {
    override fun migrate(db: SupportSQLiteDatabase) {
        fun setGroupsToExerciseSets(setGroups: JsonArray): JsonArray {
            return buildJsonArray {
                for (setGroup in setGroups) {
                    val exerciseId = setGroup.jsonObject["exerciseId"]!!.jsonPrimitive.int
                    for (set in setGroup.jsonObject["sets"]!!.jsonArray) {
                        val reps = set.jsonObject["reps"]?.jsonPrimitive?.intOrNull
                        val weight = set.jsonObject["weight"]?.jsonPrimitive?.doubleOrNull
                        val time = set.jsonObject["time"]?.jsonPrimitive?.intOrNull
                        val distance = set.jsonObject["distance"]?.jsonPrimitive?.doubleOrNull
                        val complete = set.jsonObject["complete"]?.jsonPrimitive?.booleanOrNull ?: false

                        val newSet = buildJsonObject {
                            put("exerciseId", exerciseId)
                            put("reps", reps)
                            put("weight", weight)
                            put("time", time)
                            put("distance", distance)
                            put("complete", complete)
                        }
                        add(newSet)
                    }
                }
            }
        }

        db.execSQL("ALTER TABLE routine_table RENAME COLUMN setGroups TO sets")
        val routinesCursor = db.query("SELECT routineId, sets FROM routine_table")
        while (routinesCursor.moveToNext()) {
            val routineId = routinesCursor.getInt(0)
            val setGroups = routinesCursor.getString(1)

            val newSets = setGroupsToExerciseSets(Json.parseToJsonElement(setGroups).jsonArray)

            db.execSQL("UPDATE routine_table SET sets='$newSets' WHERE routineId=$routineId")
        }

        db.execSQL("ALTER TABLE workout_table RENAME COLUMN setGroups TO sets")
        val workoutsCursor = db.query("SELECT workoutId, sets FROM workout_table")
        while (workoutsCursor.moveToNext()) {
            val workoutId = workoutsCursor.getInt(0)
            val setGroups = workoutsCursor.getString(1)

            val newSets = setGroupsToExerciseSets(Json.parseToJsonElement(setGroups).jsonArray)

            db.execSQL("UPDATE workout_table SET sets='$newSets' WHERE workoutId=$workoutId")
        }
    }
}
