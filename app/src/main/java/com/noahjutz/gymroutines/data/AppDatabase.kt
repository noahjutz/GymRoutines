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
import com.noahjutz.gymroutines.data.domain.*
import kotlinx.serialization.json.*

@Database(
    entities = [
        Exercise::class,
        Routine::class,
        Workout::class,
        RoutineSet::class,
        WorkoutSet::class,
    ],
    version = 38,
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
                        val complete =
                            set.jsonObject["complete"]?.jsonPrimitive?.booleanOrNull ?: false

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

/**
 * Instead of saving routine and workout sets as a json string in the sets column of routine_table
 * and workout_table, routine and workout sets now have their own table. They are associated with
 * their routine/workout by one-to-many relationship.
 */
val MIGRATION_37_38 = object : Migration(37, 38) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE routine_set_table (
                routineId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                reps INTEGER DEFAULT NULL,
                weight REAL DEFAULT NULL,
                time INTEGER DEFAULT NULL,
                distance REAL DEFAULT NULL,
                routineSetId INTEGER PRIMARY KEY NOT NULL
            )
            """.trimIndent()
        )

        // Transfer sets from routine_table to routine_set_table
        val routinesCursor = db.query("SELECT routineId, sets FROM routine_table")
        while (routinesCursor.moveToNext()) {
            val routineId = routinesCursor.getInt(0)
            val sets = routinesCursor.getString(1)

            Json.parseToJsonElement(sets).jsonArray.forEachIndexed { i, set ->
                val exerciseId = set.jsonObject["exerciseId"]?.jsonPrimitive?.int
                val reps = set.jsonObject["reps"]?.jsonPrimitive?.intOrNull
                val weight = set.jsonObject["weight"]?.jsonPrimitive?.doubleOrNull
                val time = set.jsonObject["time"]?.jsonPrimitive?.intOrNull
                val distance = set.jsonObject["distance"]?.jsonPrimitive?.doubleOrNull

                db.execSQL(
                    """
                    INSERT INTO routine_set_table VALUES (
                        $routineId,
                        $exerciseId,
                        $i,
                        $reps,
                        $weight,
                        $time,
                        $distance,
                        NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // This is the same as "ALTER TABLE routine_table DROP COLUMN sets" (not supported)
        db.execSQL("ALTER TABLE routine_table RENAME TO routine_table_old")
        db.execSQL(
            """
            CREATE TABLE routine_table (
                name TEXT NOT NULL,
                routineId INTEGER PRIMARY KEY NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("INSERT INTO routine_table SELECT name, routineId FROM routine_table_old")
        db.execSQL("DROP TABLE routine_table_old")

        db.execSQL(
            """
            CREATE TABLE workout_set_table (
                workoutId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                reps INTEGER DEFAULT NULL,
                weight REAL DEFAULT NULL,
                time INTEGER DEFAULT NULL,
                distance REAL DEFAULT NULL,
                complete INTEGER NOT NULL DEFAULT 0,
                workoutSetId INTEGER PRIMARY KEY NOT NULL
            )
            """.trimIndent()
        )

        // Transfer sets from workout_table to workout_set_table
        val workoutCursor = db.query("SELECT workoutId, sets FROM workout_table")
        while (workoutCursor.moveToNext()) {
            val workoutId = workoutCursor.getInt(0)
            val sets = workoutCursor.getString(1)

            Json.parseToJsonElement(sets).jsonArray.forEachIndexed { i, set ->
                val exerciseId = set.jsonObject["exerciseId"]?.jsonPrimitive?.int
                val reps = set.jsonObject["reps"]?.jsonPrimitive?.intOrNull
                val weight = set.jsonObject["weight"]?.jsonPrimitive?.doubleOrNull
                val time = set.jsonObject["time"]?.jsonPrimitive?.intOrNull
                val distance = set.jsonObject["distance"]?.jsonPrimitive?.doubleOrNull
                val complete = set.jsonObject["complete"]?.jsonPrimitive?.booleanOrNull ?: false

                db.execSQL(
                    """
                    INSERT INTO workout_set_table VALUES (
                        $workoutId,
                        $exerciseId,
                        $i,
                        $reps,
                        $weight,
                        $time,
                        $distance,
                        $complete,
                        NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // This is the same as "ALTER TABLE workout_table DROP COLUMN sets" (not supported)
        db.execSQL("ALTER TABLE workout_table RENAME TO workout_table_old")
        db.execSQL(
            """
            CREATE TABLE workout_table (
                name TEXT NOT NULL,
                startTime INTEGER NOT NULL,
                endTime INTEGER NOT NULL,
                workoutId INTEGER PRIMARY KEY NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("INSERT INTO workout_table SELECT name, startTime, endTime, workoutId FROM workout_table_old")
        db.execSQL("DROP TABLE workout_table_old")
    }
}