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

package com.noahjutz.gymroutines.di

import androidx.room.Room
import com.noahjutz.gymroutines.data.*
import com.noahjutz.gymroutines.ui.exercises.editor.ExerciseEditorViewModel
import com.noahjutz.gymroutines.ui.exercises.list.ExerciseListViewModel
import com.noahjutz.gymroutines.ui.exercises.picker.ExercisePickerViewModel
import com.noahjutz.gymroutines.ui.main.MainScreenViewModel
import com.noahjutz.gymroutines.ui.routines.editor.RoutineEditorViewModel
import com.noahjutz.gymroutines.ui.routines.list.RoutineListViewModel
import com.noahjutz.gymroutines.ui.settings.appearance.AppearanceSettingsViewModel
import com.noahjutz.gymroutines.ui.settings.data.DataSettingsViewModel
import com.noahjutz.gymroutines.ui.settings.general.GeneralSettingsViewModel
import com.noahjutz.gymroutines.ui.workout.completed.WorkoutCompletedViewModel
import com.noahjutz.gymroutines.ui.workout.in_progress.WorkoutInProgressViewModel
import com.noahjutz.gymroutines.ui.workout.insights.WorkoutInsightsViewModel
import com.noahjutz.gymroutines.ui.workout.viewer.WorkoutViewerViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val koinModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "workout_routines_database")
            .addMigrations(
                MIGRATION_36_37,
                MIGRATION_37_38,
                MIGRATION_38_39,
                MIGRATION_39_40,
                MIGRATION_40_41,
                MIGRATION_41_42,
                MIGRATION_42_43,
            )
            .build()
    }

    factory {
        androidContext().datastore
    }

    factory {
        get<AppDatabase>().exerciseDao
    }

    factory {
        get<AppDatabase>().routineDao
    }

    factory {
        get<AppDatabase>().workoutDao
    }

    factory {
        WorkoutRepository(workoutDao = get())
    }

    factory {
        RoutineRepository(routineDao = get())
    }

    factory {
        ExerciseRepository(exerciseDao = get())
    }

    viewModel {
        RoutineListViewModel(get())
    }

    viewModel {
        ExerciseListViewModel(get())
    }

    viewModel {
        ExercisePickerViewModel(exerciseRepository = get())
    }

    viewModel { params ->
        ExerciseEditorViewModel(repository = get(), exerciseId = params.get())
    }

    viewModel { params ->
        RoutineEditorViewModel(
            preferences = get(),
            exerciseRepository = get(),
            routineRepository = get(),
            workoutRepository = get(),
            routineId = params.get()
        )
    }

    viewModel { params ->
        WorkoutInProgressViewModel(
            preferences = get(),
            workoutRepository = get(),
            exerciseRepository = get(),
            routineRepository = get(),
            application = androidApplication(),
            workoutId = params.get(),
        )
    }

    viewModel {
        WorkoutInsightsViewModel(
            workoutRepository = get(),
            routineRepository = get(),
            preferences = get(),
        )
    }

    viewModel { params ->
        WorkoutViewerViewModel(
            workoutId = params.get(),
            workoutRepository = get(),
            exerciseRepository = get(),
            routineRepository = get(),
        )
    }

    viewModel {
        MainScreenViewModel(
            preferences = get()
        )
    }

    viewModel {
        AppearanceSettingsViewModel(
            preferences = get()
        )
    }

    viewModel {
        DataSettingsViewModel(
            database = get(),
            application = androidApplication(),
            preferences = get(),
        )
    }

    viewModel {
        GeneralSettingsViewModel(
            preferences = get()
        )
    }

    viewModel { params ->
        WorkoutCompletedViewModel(
            workoutId = params.get(),
            routineRepository = get(),
            workoutRepository = get(),
        )
    }
}
