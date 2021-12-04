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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.noahjutz.gymroutines.ui.exercises.editor.ExerciseEditor
import com.noahjutz.gymroutines.ui.exercises.list.ExerciseList
import com.noahjutz.gymroutines.ui.exercises.picker.ExercisePickerSheet
import com.noahjutz.gymroutines.ui.routines.RoutineList
import com.noahjutz.gymroutines.ui.routines.editor.RoutineEditor
import com.noahjutz.gymroutines.ui.settings.AppSettings
import com.noahjutz.gymroutines.ui.settings.about.AboutApp
import com.noahjutz.gymroutines.ui.settings.about.LicensesList
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
    about,
    licenses,
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
                    navToWorkoutEditor = { workoutId -> navController.navigate("${Screen.workoutViewer}/$workoutId") }
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
                    navToRoutineEditor = { routineId -> navController.navigate("${Screen.routineEditor}/$routineId") }
                )
            }
            composable(
                route = "${Screen.routineEditor}/{routineId}",
                arguments = listOf(navArgument("routineId") { type = NavType.IntType })
            ) { backStackEntry ->
                val exerciseIdsToAdd = backStackEntry
                    .arguments
                    ?.getIntegerArrayList("exerciseIdsToAdd")
                    ?.toList()
                    ?: emptyList()
                RoutineEditor(
                    routineId = backStackEntry.arguments!!.getInt("routineId"),
                    navToWorkout = { workoutId: Long ->
                        navController.navigate("${Screen.workoutInProgress}/$workoutId") {
                            popUpTo(navController.graph.findStartDestination().id)
                        }
                    },
                    popBackStack = { navController.popBackStack() },
                    navToExercisePicker = { navController.navigate(Screen.exercisePicker.name) },
                    exerciseIdsToAdd = exerciseIdsToAdd
                )
            }
            composable(Screen.exerciseList.name) {
                ExerciseList(
                    navToExerciseEditor = { exerciseId -> navController.navigate("${Screen.exerciseEditor}?exerciseId=$exerciseId") }
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
                arguments = listOf(
                    navArgument("workoutId") {
                        defaultValue = -1
                        type = NavType.IntType
                    },
                    navArgument("routineId") {
                        defaultValue = -1
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val exerciseIdsToAdd = backStackEntry
                    .arguments
                    ?.getIntegerArrayList("exerciseIdsToAdd")
                    ?.toList()
                    ?: emptyList()
                WorkoutInProgress(
                    navToExercisePicker = { navController.navigate(Screen.exercisePicker.name) },
                    popBackStack = { navController.popBackStack() },
                    workoutId = backStackEntry.arguments!!.getInt("workoutId"),
                    exerciseIdsToAdd = exerciseIdsToAdd
                )
            }
            composable(Screen.settings.name) {
                AppSettings(navToAbout = { navController.navigate(Screen.about.name) })
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
                            ?.arguments
                            ?.putIntegerArrayList("exerciseIdsToAdd", ArrayList(exerciseIds))
                        navController.popBackStack()
                    },
                    navToExerciseEditor = {
                        navController.navigate(Screen.exerciseEditor.name)
                    }
                )
            }
        }
    }
}
