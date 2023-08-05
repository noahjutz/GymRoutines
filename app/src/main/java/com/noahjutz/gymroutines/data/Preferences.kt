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

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.noahjutz.gymroutines.R

val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ColorTheme(
    @StringRes val themeName: Int
) {
    FollowSystem(R.string.app_theme_follow_system),
    White(R.string.app_theme_light),
    Black(R.string.app_theme_dark)
}

sealed class AppPrefs<T>(val key: Preferences.Key<T>, val defaultValue: T) {
    data object IsFirstRun : AppPrefs<Boolean>(
        key = booleanPreferencesKey("isFirstRun"),
        defaultValue = false
    )

    data object CurrentWorkout : AppPrefs<Int>(
        key = intPreferencesKey("currentWorkout"),
        defaultValue = -1
    )

    data object ShowBottomNavLabels : AppPrefs<Boolean>(
        key = booleanPreferencesKey("showBottomNavLabels"),
        defaultValue = true
    )

    data object AppTheme : AppPrefs<String>(
        key = stringPreferencesKey("appTheme"),
        defaultValue = ColorTheme.FollowSystem.name
    )

    data object UpdateRoutineAfterWorkout : AppPrefs<Boolean>(
        key = booleanPreferencesKey("updateRoutineAfterWorkout"),
        defaultValue = false
    )
}

suspend fun DataStore<Preferences>.resetAppSettings() {
    edit {
        it[AppPrefs.ShowBottomNavLabels.key] = AppPrefs.ShowBottomNavLabels.defaultValue
        it[AppPrefs.IsFirstRun.key] = AppPrefs.IsFirstRun.defaultValue
        it[AppPrefs.AppTheme.key] = AppPrefs.AppTheme.defaultValue
        it[AppPrefs.UpdateRoutineAfterWorkout.key] = AppPrefs.UpdateRoutineAfterWorkout.defaultValue
    }
}
