package com.noahjutz.gymroutines.ui.settings.general

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.resetAppSettings
import kotlinx.coroutines.launch

class GeneralSettingsViewModel(
    private val preferences: DataStore<Preferences>
) : ViewModel() {
    fun resetSettings() {
        viewModelScope.launch {
            preferences.resetAppSettings()
        }
    }
}
