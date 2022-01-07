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

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.noahjutz.gymroutines.ui.exercises.editor.ExerciseEditor
import com.noahjutz.gymroutines.ui.exercises.list.ExerciseList
import com.noahjutz.gymroutines.ui.exercises.picker.ExercisePickerSheet
import com.noahjutz.gymroutines.ui.routines.editor.RoutineEditor
import com.noahjutz.gymroutines.ui.routines.list.RoutineList
import com.noahjutz.gymroutines.ui.settings.AppSettings
import com.noahjutz.gymroutines.ui.settings.about.AboutApp
import com.noahjutz.gymroutines.ui.settings.about.LicensesList
import com.noahjutz.gymroutines.ui.settings.appearance.AppearanceSettings
import com.noahjutz.gymroutines.ui.settings.data.DataSettings
import com.noahjutz.gymroutines.ui.settings.general.GeneralSettings
import com.noahjutz.gymroutines.ui.workout.completed.WorkoutCompleted
import com.noahjutz.gymroutines.ui.workout.in_progress.WorkoutInProgress
import com.noahjutz.gymroutines.ui.workout.insights.WorkoutInsights
import com.noahjutz.gymroutines.ui.workout.viewer.WorkoutViewer
import kotlin.time.ExperimentalTime

@Suppress("EnumEntryName")
enum class Screen {
    insights,
    routineList,
    routineEditor,
    exerciseList,
    exerciseEditor,
    exercisePicker,
    workoutInProgress,
    workoutViewer,
    settings,
    appearanceSettings,
    dataSettings,
    generalSettings,
    about,
    licenses,
    workoutCompleted
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@ExperimentalTime
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun NavGraph(
    navController: NavHostController,
    bottomSheetNavigator: BottomSheetNavigator,
) {
    val uri = "https://gymroutines.com"
    ModalBottomSheetLayout(bottomSheetNavigator = bottomSheetNavigator) {
        AnimatedNavHost(
            navController, startDestination = Screen.routineList.name,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(Screen.insights.name) {
                WorkoutInsights(
                    navToWorkoutEditor = { workoutId -> navController.navigate("${Screen.workoutViewer}/$workoutId") },
                    navToSettings = { navController.navigate(Screen.settings.name) }
                )
            }
            composable(
                route = "${Screen.workoutViewer}/{workoutId}",
                arguments = listOf(navArgument("workoutId") { type = NavType.IntType })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments!!.getInt("workoutId")
                WorkoutViewer(
                    workoutId = workoutId,
                    popBackStack = { navController.popBackStack() },
                )
            }
            composable(Screen.routineList.name) {
                RoutineList(
                    navToRoutineEditor = { routineId -> navController.navigate("${Screen.routineEditor}/$routineId") },
                    navToSettings = { navController.navigate(Screen.settings.name) }
                )
            }
            composable(
                route = "${Screen.routineEditor}/{routineId}",
                arguments = listOf(navArgument("routineId") { type = NavType.IntType })
            ) { backStackEntry ->
                val exerciseIdsToAdd by backStackEntry
                    .savedStateHandle
                    .getLiveData<List<Int>>("exerciseIdsToAdd")
                    .observeAsState()
                LaunchedEffect(exerciseIdsToAdd) {
                    backStackEntry.savedStateHandle.set("exerciseIdsToAdd", null)
                }
                RoutineEditor(
                    routineId = backStackEntry.arguments!!.getInt("routineId"),
                    navToWorkout = { workoutId: Long ->
                        navController.navigate("${Screen.workoutInProgress}/$workoutId") {
                            popUpTo(navController.graph.findStartDestination().id)
                        }
                    },
                    popBackStack = { navController.popBackStack() },
                    navToExercisePicker = { navController.navigate(Screen.exercisePicker.name) },
                    exerciseIdsToAdd = exerciseIdsToAdd ?: emptyList()
                )
            }
            composable(Screen.exerciseList.name) {
                ExerciseList(
                    navToExerciseEditor = { exerciseId -> navController.navigate("${Screen.exerciseEditor}?exerciseId=$exerciseId") },
                    navToSettings = { navController.navigate(Screen.settings.name) }
                )
            }
            composable(
                route = "${Screen.exerciseEditor}?exerciseId={exerciseId}",
                arguments = listOf(
                    navArgument("exerciseId") {
                        defaultValue = -1
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                ExerciseEditor(
                    exerciseId = backStackEntry.arguments!!.getInt("exerciseId"),
                    popBackStack = { navController.popBackStack() },
                )
            }
            composable(
                "${Screen.workoutInProgress}/{workoutId}",
                arguments = listOf(navArgument("workoutId") { type = NavType.IntType }),
                deepLinks = listOf(navDeepLink { uriPattern = "$uri/workoutInProgress/{workoutId}" })
            ) { backStackEntry ->
                val exerciseIdsToAdd by backStackEntry
                    .savedStateHandle
                    .getLiveData<List<Int>>("exerciseIdsToAdd")
                    .observeAsState()
                LaunchedEffect(exerciseIdsToAdd) {
                    backStackEntry.savedStateHandle.set("exerciseIdsToAdd", null)
                }
                val workoutId = backStackEntry.arguments!!.getInt("workoutId")
                WorkoutInProgress(
                    workoutId = workoutId,
                    exerciseIdsToAdd = exerciseIdsToAdd ?: emptyList(),
                    navToExercisePicker = { navController.navigate(Screen.exercisePicker.name) },
                    popBackStack = { navController.popBackStack() },
                    navToWorkoutCompleted = { workoutId, routineId ->
                        navController.navigate("${Screen.workoutCompleted}/$workoutId/$routineId") {
                            popUpTo(navController.graph.findStartDestination().id)
                        }
                    }
                )
            }
            composable(Screen.settings.name) {
                AppSettings(
                    popBackStack = { navController.popBackStack() },
                    navToAbout = { navController.navigate(Screen.about.name) },
                    navToAppearanceSettings = { navController.navigate(Screen.appearanceSettings.name) },
                    navToDataSettings = { navController.navigate(Screen.dataSettings.name) },
                    navToGeneralSettings = { navController.navigate(Screen.generalSettings.name) }
                )
            }
            composable(Screen.appearanceSettings.name) {
                AppearanceSettings(popBackStack = { navController.popBackStack() })
            }
            composable(Screen.dataSettings.name) {
                DataSettings(popBackStack = { navController.popBackStack() })
            }
            composable(Screen.generalSettings.name) {
                GeneralSettings(popBackStack = { navController.popBackStack() })
            }
            composable(Screen.about.name) {
                AboutApp(
                    popBackStack = { navController.popBackStack() },
                    navToLicenses = { navController.navigate(Screen.licenses.name) }
                )
            }
            composable(Screen.licenses.name) {
                LicensesList(popBackStack = { navController.popBackStack() })
            }
            bottomSheet(Screen.exercisePicker.name) {
                ExercisePickerSheet(
                    onExercisesSelected = { exerciseIds ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("exerciseIdsToAdd", exerciseIds)
                        navController.popBackStack()
                    },
                    navToExerciseEditor = {
                        navController.navigate(Screen.exerciseEditor.name)
                    }
                )
            }
            composable(
                "${Screen.workoutCompleted.name}/{workoutId}/{routineId}",
                arguments = listOf(
                    navArgument("workoutId") { type = NavType.IntType },
                    navArgument("routineId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments!!.getInt("workoutId")
                val routineId = backStackEntry.arguments!!.getInt("routineId")
                WorkoutCompleted(
                    workoutId = workoutId,
                    routineId = routineId,
                    popBackStack = { navController.popBackStack() },
                    navToWorkoutInProgress = {
                        navController.navigate("${Screen.workoutInProgress}/$workoutId") {
                            popUpTo(navController.graph.findStartDestination().id)
                        }
                    }
                )
            }
        }
    }
}
