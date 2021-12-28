package com.noahjutz.gymroutines.ui.main

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.ColorTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MainScreenViewModel(
    preferences: DataStore<Preferences>
) : ViewModel() {
    val colorTheme = preferences.data.map { preferences ->
        preferences[AppPrefs.AppTheme.key]?.let { key ->
            ColorTheme.valueOf(key)
        } ?: ColorTheme.FollowSystem
    }

    fun isDark(defaultValue: Boolean): Flow<Boolean> = colorTheme.map { colorTheme ->
        when (colorTheme) {
            ColorTheme.FollowSystem -> defaultValue
            ColorTheme.White -> false
            ColorTheme.Black -> true
        }
    }

    val currentWorkoutId: Flow<Int> = preferences.data.map { preferences ->
        preferences[AppPrefs.CurrentWorkout.key] ?: -1
    }

    val showBottomLabels = preferences.data.map { preferences ->
        preferences[AppPrefs.ShowBottomNavLabels.key] ?: true
    }
}
