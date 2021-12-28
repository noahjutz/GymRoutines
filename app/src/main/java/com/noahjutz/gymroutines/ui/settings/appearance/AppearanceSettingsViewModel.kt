package com.noahjutz.gymroutines.ui.settings.appearance

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.ColorTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppearanceSettingsViewModel(
    private val preferences: DataStore<Preferences>
) : ViewModel() {
    val appTheme = preferences.data.map {
        it[AppPrefs.AppTheme.key]?.let { colorThemeString ->
            ColorTheme.valueOf(colorThemeString)
        } ?: ColorTheme.FollowSystem
    }

    fun setAppTheme(theme: ColorTheme) {
        viewModelScope.launch {
            preferences.edit {
                it[AppPrefs.AppTheme.key] = theme.name
            }
        }
    }
}
