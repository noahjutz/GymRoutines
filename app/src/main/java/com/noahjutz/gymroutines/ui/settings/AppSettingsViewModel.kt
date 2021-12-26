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

package com.noahjutz.gymroutines.ui.settings

import android.app.Application
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakewharton.processphoenix.ProcessPhoenix
import com.noahjutz.gymroutines.data.AppDatabase
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.ColorTheme
import com.noahjutz.gymroutines.data.resetAppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppSettingsViewModel(
    private val preferences: DataStore<androidx.datastore.preferences.core.Preferences>,
) : ViewModel() {
    private val _showBottomNavLabels = MutableStateFlow(false)
    val showBottomNavLabels = _showBottomNavLabels.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.data.collectLatest {
                _showBottomNavLabels.value = it[AppPrefs.ShowBottomNavLabels.key] == true
            }
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            preferences.resetAppSettings()
        }
    }
}
