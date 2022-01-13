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

package com.noahjutz.gymroutines.ui.settings.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.noahjutz.gymroutines.BuildConfig
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.ui.components.TopBar
import com.noahjutz.gymroutines.util.openUrl

private object Urls {
    const val sourceCode = "https://codeberg.org/noahjutz/GymRoutines"
}

@ExperimentalMaterialApi
@Composable
fun AboutApp(
    popBackStack: () -> Unit,
    navToLicenses: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.screen_about),
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_pop_back))
                    }
                }
            )
        }
    ) {
        val context = LocalContext.current.applicationContext
        LazyColumn {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colorResource(id = R.color.ic_launcher_background),
                        elevation = 4.dp,
                    ) {
                        Image(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(48.dp),
                            imageVector = ImageVector.vectorResource(R.drawable.ic_gymroutines),
                            contentDescription = null,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.app_name), style = typography.h4)
                }

                ListItem(
                    text = { Text(stringResource(R.string.label_app_version)) },
                    secondaryText = { Text(BuildConfig.VERSION_NAME) },
                    icon = { Icon(Icons.Default.Update, null) },
                )
                ListItem(
                    modifier = Modifier.clickable(onClick = navToLicenses),
                    text = { Text(stringResource(R.string.label_app_licenses)) },
                    icon = { Icon(Icons.Default.ListAlt, null) },
                )
                ListItem(
                    modifier = Modifier.clickable { context.openUrl(Urls.sourceCode) },
                    text = { Text(stringResource(R.string.label_app_source_code)) },
                    icon = { Icon(Icons.Default.Code, null) },
                    trailing = { Icon(Icons.Default.Launch, null) },
                )

                Divider()

                ListItem(
                    text = { Text(stringResource(R.string.label_contact)) },
                    secondaryText = { Text("noahjutz@tutanota.de") },
                    icon = { Icon(Icons.Default.ContactMail, null) },
                )
            }
        }
    }
}
