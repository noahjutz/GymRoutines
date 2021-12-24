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
        RoutineSetGroup::class,
        WorkoutSet::class,
        WorkoutSetGroup::class,
    ],
    version = 43,
    autoMigrations = [AutoMigration(from = 35, to = 36)],
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
        routinesCursor.close()

        db.execSQL("ALTER TABLE workout_table RENAME COLUMN setGroups TO sets")
        val workoutsCursor = db.query("SELECT workoutId, sets FROM workout_table")
        while (workoutsCursor.moveToNext()) {
            val workoutId = workoutsCursor.getInt(0)
            val setGroups = workoutsCursor.getString(1)

            val newSets = setGroupsToExerciseSets(Json.parseToJsonElement(setGroups).jsonArray)

            db.execSQL("UPDATE workout_table SET sets='$newSets' WHERE workoutId=$workoutId")
        }
        workoutsCursor.close()
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
        routinesCursor.close()

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
        workoutCursor.close()

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

/**
 * Adds workout_set_group_table and routine_set_group_table, grouping sets by exercise.
 */
val MIGRATION_38_39 = object : Migration(38, 39) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add routine_set_group_table

        db.execSQL(
            """
            CREATE TABLE routine_set_group_table (
                routineId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                id INTEGER PRIMARY KEY NOT NULL
            )
            """.trimIndent()
        )

        // Drop columns routineId, exerciseId; Add column groupId
        db.execSQL("ALTER TABLE routine_set_table RENAME TO routine_set_table_old")
        db.execSQL(
            """
            CREATE TABLE routine_set_table (
                groupId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                reps INTEGER,
                weight REAL,
                time INTEGER,
                distance REAL,
                routineSetId INTEGER PRIMARY KEY NOT NULL
            )
            """.trimIndent()
        )
        // Insert sets from routine_set_table_old to routine_set_table
        // Insert set groups to routine_set_group_table
        var routineSetGroupPosition = 0
        var routineSetGroupId = 0

        val routineSetCursor =
            db.query("SELECT routineId, exerciseId, position, reps, weight, time, distance, routineSetId FROM routine_set_table_old")
        while (routineSetCursor.moveToNext()) {
            val routineId = routineSetCursor.getInt(0)
            val exerciseId = routineSetCursor.getInt(1)
            val position = routineSetCursor.getInt(2)
            val reps = routineSetCursor.getInt(3)
            val weight = routineSetCursor.getInt(4)
            val time = routineSetCursor.getInt(5)
            val distance = routineSetCursor.getInt(6)
            val setId = routineSetCursor.getInt(7)

            val setGroupIds =
                db.query("SELECT id FROM routine_set_group_table WHERE routineId=$routineId AND exerciseId=$exerciseId")

            val groupId = if (setGroupIds.count == 0) {
                if (db.query("SELECT id FROM routine_set_group_table WHERE routineId=$routineId").count == 0) {
                    routineSetGroupPosition = 0
                }
                // Insert new routine set group
                routineSetGroupId++
                db.execSQL("INSERT INTO routine_set_group_table VALUES ($routineId, $exerciseId, $routineSetGroupPosition, $routineSetGroupId)")
                routineSetGroupPosition++
                routineSetGroupId
            } else {
                // Use existing routine set group
                setGroupIds.moveToFirst()
                val id = setGroupIds.getInt(0)
                id
            }
            setGroupIds.close()

            db.execSQL("INSERT INTO routine_set_table VALUES ($groupId, $position, $reps, $weight, $time, $distance, $setId)")
        }
        routineSetCursor.close()
        db.execSQL("DROP TABLE routine_set_table_old ")

        // Add workout_set_group_table

        db.execSQL(
            """
            CREATE TABLE workout_set_group_table (
                workoutId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                id INTEGER PRIMARY KEY NOT NULL
            )
            """.trimIndent()
        )

        // Drop columns routineId, exerciseId; Add column groupId
        db.execSQL("ALTER TABLE workout_set_table RENAME TO workout_set_table_old")
        db.execSQL(
            """
            CREATE TABLE workout_set_table (
                groupId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                reps INTEGER,
                weight REAL,
                time INTEGER,
                distance REAL,
                complete INTEGER NOT NULL DEFAULT 0,
                workoutSetId INTEGER PRIMARY KEY NOT NULL
            )
            """.trimIndent()
        )
        // Insert sets from routine_set_table_old to routine_set_table
        // Insert set groups to routine_set_group_table
        var workoutSetGroupPosition = 0
        var workoutSetGroupId = 0

        val workoutSetCursor =
            db.query("SELECT workoutId, exerciseId, position, reps, weight, time, distance, complete, workoutSetId FROM workout_set_table_old")
        while (workoutSetCursor.moveToNext()) {
            val workoutId = workoutSetCursor.getInt(0)
            val exerciseId = workoutSetCursor.getInt(1)
            val position = workoutSetCursor.getInt(2)
            val reps = workoutSetCursor.getInt(3)
            val weight = workoutSetCursor.getInt(4)
            val time = workoutSetCursor.getInt(5)
            val distance = workoutSetCursor.getInt(6)
            val complete = workoutSetCursor.getInt(7)
            val setId = workoutSetCursor.getInt(8)

            val setGroupIds =
                db.query("SELECT id FROM workout_set_group_table WHERE workoutId=$workoutId AND exerciseId=$exerciseId")

            val groupId = if (setGroupIds.count == 0) {
                if (
                    db.query("SELECT id FROM workout_set_group_table WHERE workoutId=$workoutId")
                        .use { it.count } == 0
                ) {
                    workoutSetGroupPosition = 0
                }
                // Insert new workout set group
                workoutSetGroupId++
                db.execSQL("INSERT INTO workout_set_group_table VALUES ($workoutId, $exerciseId, $workoutSetGroupPosition, $workoutSetGroupId)")
                workoutSetGroupPosition++
                workoutSetGroupId
            } else {
                // Use existing workout set group
                setGroupIds.moveToFirst()
                val id = setGroupIds.getInt(0)
                id
            }
            setGroupIds.close()

            db.execSQL("INSERT INTO workout_set_table VALUES ($groupId, $position, $reps, $weight, $time, $distance, $complete, $setId)")
        }
        workoutSetCursor.close()
        db.execSQL("DROP TABLE workout_set_table_old ")
    }
}

/**
 * Adds foreign keys to:
 * routine_set_group_table,
 * routine_set_table,
 * workout_set_group_table,
 * workout_set_table.
 *
 * The foreign keys have an ON DELETE CASCADE action, meaning that children are deleted when their
 * parents are deleted.
 */
val MIGRATION_39_40 = object : Migration(39, 40) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add foreign key to routine_set_group_table
        db.execSQL("ALTER TABLE routine_set_group_table RENAME TO routine_set_group_table_old")
        db.execSQL(
            """
            CREATE TABLE routine_set_group_table (
                routineId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                id INTEGER PRIMARY KEY NOT NULL,
                FOREIGN KEY (routineId) REFERENCES routine_table(routineId) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("INSERT INTO routine_set_group_table SELECT routineId, exerciseId, position, id FROM routine_set_group_table_old")
        db.execSQL("DROP TABLE routine_set_group_table_old")

        // Add foreign key to routine_set_table
        db.execSQL("ALTER TABLE routine_set_table RENAME TO routine_set_table_old")
        db.execSQL(
            """
            CREATE TABLE routine_set_table (
                groupId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                reps INTEGER,
                weight REAL,
                time INTEGER,
                distance REAL,
                routineSetId INTEGER PRIMARY KEY NOT NULL,
                FOREIGN KEY (groupId) REFERENCES routine_set_group_table(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("INSERT INTO routine_set_table SELECT groupId, position, reps, weight, time, distance, routineSetId FROM routine_set_table_old")
        db.execSQL("DROP TABLE routine_set_table_old")

        // Add foreign key to workout_set_group_table
        db.execSQL("ALTER TABLE workout_set_group_table RENAME TO workout_set_group_table_old")
        db.execSQL(
            """
            CREATE TABLE workout_set_group_table (
                workoutId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                id INTEGER PRIMARY KEY NOT NULL,
                FOREIGN KEY (workoutId) REFERENCES workout_table(workoutId) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("INSERT INTO workout_set_group_table SELECT workoutId, exerciseId, position, id FROM workout_set_group_table_old")
        db.execSQL("DROP TABLE workout_set_group_table_old")

        // Add foreign key to workout_set_table
        db.execSQL("ALTER TABLE workout_set_table RENAME TO workout_set_table_old")
        db.execSQL(
            """
            CREATE TABLE workout_set_table (
                groupId INTEGER NOT NULL,
                position INTEGER NOT NULL,
                reps INTEGER,
                weight REAL,
                time INTEGER,
                distance REAL,
                complete INTEGER NOT NULL DEFAULT 0,
                workoutSetId INTEGER PRIMARY KEY NOT NULL,
                FOREIGN KEY (groupId) REFERENCES workout_set_group_table(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("INSERT INTO workout_set_table SELECT groupId, position, reps, weight, time, distance, complete, workoutSetId FROM workout_set_table_old")
        db.execSQL("DROP TABLE workout_set_table_old")
    }
}

/**
 * Adds Indices to:
 * routine_set_group_table,
 * routine_set_table,
 * workout_set_group_table,
 * workout_set_table.
 */
val MIGRATION_40_41 = object : Migration(40, 41) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX index_routine_set_group_table_routineId ON routine_set_group_table(routineId)")
        db.execSQL("CREATE INDEX index_routine_set_table_groupId ON routine_set_table(groupId)")
        db.execSQL("CREATE INDEX index_workout_set_group_table_workoutId ON workout_set_group_table(workoutId)")
        db.execSQL("CREATE INDEX index_workout_set_table_groupId ON workout_set_table(groupId)")
    }
}

/**
 * Removes WorkoutSet.position and RoutineSet.position columns. The order can be derived from the id.
 */
val MIGRATION_41_42 = object : Migration(41, 42) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX index_routine_set_table_groupId")
        db.execSQL("ALTER TABLE routine_set_table RENAME TO routine_set_table_old")
        db.execSQL(
            """
            CREATE TABLE routine_set_table (
                groupId INTEGER NOT NULL,
                reps INTEGER,
                weight REAL,
                time INTEGER,
                distance REAL,
                routineSetId INTEGER PRIMARY KEY NOT NULL,
                FOREIGN KEY (groupId) REFERENCES routine_set_group_table(id) ON DELETE CASCADE
                
            )
            """.trimIndent()
        )
        db.execSQL("INSERT INTO routine_set_table SELECT groupId, reps, weight, time, distance, routineSetId FROM routine_set_table_old")
        db.execSQL("CREATE INDEX index_routine_set_table_groupId ON routine_set_table(groupId)")
        db.execSQL("DROP TABLE routine_set_table_old")

        db.execSQL("DROP INDEX index_workout_set_table_groupId")
        db.execSQL("ALTER TABLE workout_set_table RENAME TO workout_set_table_old")
        db.execSQL(
            """
            CREATE TABLE workout_set_table (
                groupId INTEGER NOT NULL,
                reps INTEGER,
                weight REAL,
                time INTEGER,
                distance REAL,
                complete INTEGER NOT NULL DEFAULT 0,
                workoutSetId INTEGER PRIMARY KEY NOT NULL,
                FOREIGN KEY (groupId) REFERENCES workout_set_group_table(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("INSERT INTO workout_set_table SELECT groupId, reps, weight, time, distance, complete, workoutSetId FROM workout_set_table_old")
        db.execSQL("CREATE INDEX index_workout_set_table_groupId ON workout_set_table(groupId)")
        db.execSQL("DROP TABLE workout_set_table_old")
    }
}

/**
 * Adds `routineId` column to workout_table, removes `name` column. This introduces a one-to-many
 * relationship routine_table (1-n) workout_table.
 *
 * Existing workouts are connected to routines with matching names. Workouts without matching
 * routine are connected to new empty hidden routines (New `hidden` column in routine_table is added
 * for this).
 */
val MIGRATION_42_43 = object : Migration(42, 43) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE routine_table ADD COLUMN hidden INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE workout_table RENAME TO workout_table_old")
        db.execSQL(
            """
            CREATE TABLE workout_table (
                routineId INTEGER NOT NULL,
                startTime INTEGER NOT NULL,
                endTime INTEGER NOT NULL,
                workoutId INTEGER PRIMARY KEY NOT NULL
            )
            """.trimIndent()
        )
        db.query("SELECT * FROM workout_table_old").use { workouts ->
            while (workouts.moveToNext()) {
                val name = workouts.getString(0)
                val startTime = workouts.getInt(1)
                val endTime = workouts.getInt(2)
                val workoutId = workouts.getInt(3)
                db.query("SELECT * FROM routine_table WHERE name='$name' LIMIT 1").use { routine ->
                    val nextRoutineId =
                        if (routine.moveToFirst()) {
                            routine.getInt(1)
                        } else {
                            db.query("SELECT MAX(routineId) FROM routine_table")
                                .use { maxRoutineId ->
                                    val id = if (maxRoutineId.moveToFirst()) {
                                        maxRoutineId.getInt(0) + 1
                                    } else {
                                        1
                                    }
                                    db.execSQL("INSERT INTO routine_table VALUES ('$name', true, $id)")
                                    id
                                }
                        }
                    db.execSQL("INSERT INTO workout_table VALUES ($nextRoutineId, $startTime, $endTime, $workoutId)")
                }
            }
        }
        db.execSQL("DROP TABLE workout_table_old")
    }
}