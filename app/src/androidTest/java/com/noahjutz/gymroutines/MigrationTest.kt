package com.noahjutz.gymroutines

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.noahjutz.gymroutines.data.AppDatabase
import com.noahjutz.gymroutines.data.MIGRATION_36_37
import com.noahjutz.gymroutines.data.MIGRATION_37_38
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException

private const val TEST_DB = "migration-test"

@RunWith(JUnit4::class)
class MigrationTest {
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create earliest version of the database.
        helper.createDatabase(TEST_DB, 35).apply {
            close()
        }

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room
            .databaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                AppDatabase::class.java,
                TEST_DB
            )
            .addMigrations(MIGRATION_36_37, MIGRATION_37_38)
            .build()
            .apply {
                openHelper.writableDatabase
                close()
            }
    }

    @Test
    @Throws(IOException::class)
    fun migrate35To36() {
        var db = helper.createDatabase(TEST_DB, 35).use {
            it.execSQL("INSERT INTO exercise_table VALUES ('Squat', 'true', 'true', 'false', 'false', 'false', 0)")
            it
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 36, true)
    }

    @Test
    @Throws(IOException::class)
    fun migrate36To37() {
        var db = helper.createDatabase(TEST_DB, 36).use {
            it.execSQL("INSERT INTO exercise_table VALUES ('Squat', '', 'true', 'true', 'false', 'false', 'false', 0)")
            it.execSQL("INSERT INTO routine_table VALUES ('Legs', '[{\"exerciseId\":0,\"sets\":[{\"reps\":6}]}]', 0)")
            it
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 37, true, MIGRATION_36_37)
    }

    @Test
    @Throws(IOException::class)
    fun migrate37to38() {
        var db = helper.createDatabase(TEST_DB, 37).use {
            it.execSQL("INSERT INTO exercise_table VALUES ('Squat', '', 'true', 'true', 'false', 'false', 'false', 0)")
            it.execSQL("INSERT INTO routine_table VALUES ('Legs', '[{\"exerciseId\":0,\"reps\":6}]', 0)")
            it
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 38, true, MIGRATION_37_38)
    }
}
