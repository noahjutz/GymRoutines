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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.ui.components.TopBar

@ExperimentalMaterialApi
@Composable
fun AppSettings(
    popBackStack: () -> Unit,
    navToAbout: () -> Unit,
    navToAppearanceSettings: () -> Unit,
    navToDataSettings: () -> Unit,
    navToGeneralSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.tab_settings),
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(Icons.Default.ArrowBack, "back")
                    }
                }
            )
        }
    ) {
        Column(
            Modifier.scrollable(
                orientation = Orientation.Vertical,
                state = rememberScrollState()
            )
        ) {
            ListItem(
                modifier = Modifier.clickable(onClick = navToGeneralSettings),
                text = { Text("General") },
                icon = { Icon(Icons.Default.Construction, null) }
            )
            ListItem(
                modifier = Modifier.clickable(onClick = navToAppearanceSettings),
                text = { Text("Appearance") },
                icon = { Icon(Icons.Default.DarkMode, null) }
            )
            ListItem(
                modifier = Modifier.clickable(onClick = navToDataSettings),
                text = { Text("Data") },
                icon = { Icon(Icons.Default.Shield, null) },
            )
            Divider()
            ListItem(
                modifier = Modifier.clickable(onClick = navToAbout),
                text = { Text("About") },
                icon = { Icon(Icons.Default.Info, null) }
            )
        }
    }
}
