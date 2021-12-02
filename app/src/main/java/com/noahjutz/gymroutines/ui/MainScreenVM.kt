package com.noahjutz.gymroutines.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import com.noahjutz.gymroutines.data.AppPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MainScreenVM(
    preferences: DataStore<Preferences>
) : ViewModel() {
    val currentWorkoutId: Flow<Int> = preferences.data.map { preferences ->
        preferences[AppPrefs.CurrentWorkout.key] ?: -1
    }

    val showBottomLabels = preferences.data.map { preferences ->
        preferences[AppPrefs.ShowBottomNavLabels.key] ?: true
    }
}