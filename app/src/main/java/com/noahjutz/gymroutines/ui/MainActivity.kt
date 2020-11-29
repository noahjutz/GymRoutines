/*
 * GymRoutines
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.viewinterop.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.noahjutz.gymroutines.ui.exercises.ExercisesScreen
import com.noahjutz.gymroutines.ui.exercises.ExercisesViewModel
import com.noahjutz.gymroutines.ui.exercises.create.CreateExerciseScreen
import com.noahjutz.gymroutines.ui.exercises.create.CreateExerciseViewModel
import com.noahjutz.gymroutines.ui.routines.RoutinesScreen
import com.noahjutz.gymroutines.ui.routines.RoutinesViewModel
import com.noahjutz.gymroutines.ui.routines.create.CreateRoutineScreen
import com.noahjutz.gymroutines.ui.routines.create.CreateRoutineViewModel
import com.noahjutz.gymroutines.ui.routines.create.pick.PickExercise
import com.noahjutz.gymroutines.ui.routines.create.pick.SharedExerciseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val sharedExerciseVM: SharedExerciseViewModel by viewModels()

    @ExperimentalFoundationApi
    @ExperimentalFocus
    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colors = if (isSystemInDarkTheme()) darkColors() else lightColors()) {
                MainScreen(sharedExerciseVM)
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalFocus
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun MainScreen(
    sharedExerciseVM: SharedExerciseViewModel
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry.value?.arguments?.getString(KEY_ROUTE)
            if (currentRoute in items.map { it.route }) {
                BottomNavigation {
                    items.forEach { screen ->
                        BottomNavigationItem(
                            icon = { Icon(screen.icon) },
                            label = { Text(screen.name) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                // This is the equivalent to popUpTo the start destination
                                navController.popBackStack(
                                    navController.graph.startDestination,
                                    false
                                )

                                // This if check gives us a "singleTop" behavior where we do not create a
                                // second instance of the composable if we are already on that destination
                                if (currentRoute != screen.route) navController.navigate(screen.route)
                            }
                        )
                    }
                }
            }
        }
    ) {
        val routinesVM = viewModel<RoutinesViewModel>()
        val createRoutineVM = viewModel<CreateRoutineViewModel>()
        val exercisesVM = viewModel<ExercisesViewModel>()
        val createExerciseVM = viewModel<CreateExerciseViewModel>()
        NavHost(navController, startDestination = "routines") {
            composable("routines") {
                RoutinesScreen(
                    addEditRoutine = {
                        val routineId = if (it < 0) routinesVM.addRoutine().toInt() else it
                        navController.navigate("createRoutine/$routineId")
                    },
                    viewModel = routinesVM
                )
            }
            composable(
                route = "createRoutine/{routineId}",
                arguments = listOf(navArgument("routineId") { type = NavType.IntType })
            ) { backStackEntry ->
                val routineId: Int = backStackEntry.arguments?.getInt("routineId") ?: -1
                createRoutineVM.setRoutine(routineId)
                CreateRoutineScreen(
                    onAddExercise = { navController.navigate("pickExercise") },
                    popBackStack = { navController.popBackStack() },
                    viewModel = createRoutineVM,
                    sharedExerciseVM = sharedExerciseVM
                )
            }
            composable("pickExercise") {
                PickExercise(
                    exercisesViewModel = exercisesVM,
                    sharedExerciseViewModel = sharedExerciseVM,
                    popBackStack = { navController.popBackStack() }
                )
            }
            composable("exercises") {
                ExercisesScreen(
                    addEditExercise = {
                        val exerciseId = if (it < 0) exercisesVM.addExercise() else it
                        navController.navigate("createExercise/$exerciseId")
                    },
                    viewModel = exercisesVM
                )
            }
            composable(
                route = "createExercise/{exerciseId}",
                arguments = listOf(navArgument("exerciseId") {type = NavType.IntType})
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getInt("exerciseId") ?: -1
                createExerciseVM.setExercise(exerciseId)
                CreateExerciseScreen(
                    popBackStack = { navController.popBackStack() },
                    viewModel = createExerciseVM
                )
            }
        }
    }
}

sealed class Screen(val route: String, val name: String, val icon: VectorAsset) {
    object Routines : Screen("routines", "Routines", Icons.Default.ViewAgenda)
    object Exercises : Screen("exercises", "Exercises", Icons.Default.DirectionsRun)
}

val items = listOf(
    Screen.Routines,
    Screen.Exercises
)