package com.noahjutz.gymroutines.ui.main

import androidx.annotation.StringRes
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.ui.Screen

sealed class BottomNavItem(
    val route: String,
    @StringRes val name: Int,
    val icon: ImageVector,
) {
    object Routines : BottomNavItem(
        route = Screen.routineList.name,
        name = R.string.screen_routine_list,
        icon = Icons.Default.ViewAgenda
    )

    object Exercises : BottomNavItem(
        route = Screen.exerciseList.name,
        name = R.string.screen_exercise_list,
        icon = Icons.Default.FitnessCenter
    )

    object Workouts : BottomNavItem(
        route = Screen.insights.name,
        name = R.string.screen_insights,
        icon = Icons.Default.Insights
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Routines,
    BottomNavItem.Exercises,
    BottomNavItem.Workouts,
)

@Composable
fun BottomBar(
    navController: NavController,
    showLabels: Boolean,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    BottomNavigation {
        for (item in bottomNavItems) {
            BottomNavigationItem(
                icon = { Icon(item.icon, null) },
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = (@Composable { Text(stringResource(item.name)) }).takeIf { showLabels },
                selected = item.route == currentRoute,
            )
        }
    }
}
