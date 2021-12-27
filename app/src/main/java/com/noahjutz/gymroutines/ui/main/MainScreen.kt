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

package com.noahjutz.gymroutines.ui.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.noahjutz.gymroutines.ui.NavGraph
import com.noahjutz.gymroutines.ui.Screen
import kotlin.time.ExperimentalTime
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialNavigationApi::class)
@ExperimentalTime
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun MainScreen(viewModel: MainScreenViewModel = getViewModel()) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberAnimatedNavController(bottomSheetNavigator)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()

            val isCurrentDestinationHomeTab = navBackStackEntry
                ?.destination
                ?.route in bottomNavItems.map { it.route }

            if (isCurrentDestinationHomeTab) {
                Column {
                    val currentWorkoutId by viewModel.currentWorkoutId.collectAsState(initial = -1)
                    val navToWorkoutScreen = {
                        navController.navigate("${Screen.workoutInProgress}/$currentWorkoutId")
                    }
                    if (currentWorkoutId >= 0) {
                        WorkoutBottomSheet(navToWorkoutScreen)
                    }

                    val showBottomNavLabels by viewModel.showBottomLabels.collectAsState(initial = true)
                    BottomBar(
                        navController = navController,
                        showLabels = showBottomNavLabels
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            NavGraph(navController = navController, bottomSheetNavigator = bottomSheetNavigator)
        }
    }
}