package com.noahjutz.gymroutines.ui.workout.completed

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@ExperimentalMaterialApi
@Composable
fun WorkoutCompleted(
    workoutId: Int,
    popBackStack: () -> Unit,
    viewModel: WorkoutCompletedViewModel = getViewModel { parametersOf(workoutId) }
) {
}
