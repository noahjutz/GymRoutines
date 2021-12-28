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

package com.noahjutz.gymroutines.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.noahjutz.gymroutines.data.AppPrefs
import com.noahjutz.gymroutines.data.ColorTheme
import com.noahjutz.gymroutines.data.datastore
import com.noahjutz.gymroutines.ui.main.MainScreen
import com.noahjutz.gymroutines.ui.theme.GymRoutinesTheme
import com.noahjutz.gymroutines.util.valueOf
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.map

val LocalActivity = compositionLocalOf<MainActivity> { error("MainActivity not found") }
val LocalThemePreference = compositionLocalOf { ColorTheme.FollowSystem }
val LocalColorTheme = compositionLocalOf<ColorTheme> { error("") }

class MainActivity : AppCompatActivity() {

    @OptIn(
        ExperimentalTime::class, ExperimentalFoundationApi::class,
        ExperimentalMaterialApi::class, ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            val appTheme: ColorTheme by applicationContext.datastore.data
                .map {
                    valueOf(
                        it[AppPrefs.AppTheme.key] ?: ColorTheme.FollowSystem.name,
                        ColorTheme.FollowSystem
                    )
                }
                .collectAsState(initial = ColorTheme.FollowSystem)

            val colors = when (appTheme) {
                ColorTheme.FollowSystem -> if (isSystemInDarkTheme()) ColorTheme.Black else ColorTheme.White
                ColorTheme.White -> ColorTheme.White
                ColorTheme.Black -> ColorTheme.Black
            }

            CompositionLocalProvider(
                LocalActivity provides this@MainActivity,
                LocalThemePreference provides appTheme,
                LocalColorTheme provides colors
            ) {
                GymRoutinesTheme(colors = LocalThemePreference.current) {
                    MainScreen()
                }
            }
        }
    }
}
