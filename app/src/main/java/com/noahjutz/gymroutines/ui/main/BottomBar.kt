package com.noahjutz.gymroutines.ui.main

import androidx.annotation.StringRes
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
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
    object Routines :
        BottomNavItem(Screen.routineList.name, R.string.tab_routines, Icons.Default.ViewAgenda)

    object Exercises :
        BottomNavItem(Screen.exerciseList.name, R.string.tab_exercises, Icons.Default.FitnessCenter)

    object Workouts :
        BottomNavItem(Screen.insights.name, R.string.tab_insights, Icons.Default.Insights)

    object Settings :
        BottomNavItem(Screen.settings.name, R.string.tab_settings, Icons.Default.Settings)
}

val bottomNavItems = listOf(
    BottomNavItem.Routines,
    BottomNavItem.Exercises,
    BottomNavItem.Workouts,
    BottomNavItem.Settings,
)

@Composable
fun BottomBar(
    navController: NavController,
    showLabels: Boolean,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    BottomNavigation {
        for (screen in bottomNavItems) {
            BottomNavigationItem(
                icon = { Icon(screen.icon, null) },
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = (@Composable { Text(stringResource(screen.name)) }).takeIf { showLabels },
                selected = screen.route == currentRoute,
            )
        }
    }
}
