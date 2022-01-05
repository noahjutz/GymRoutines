package com.noahjutz.gymroutines.ui.workout.completed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.WorkoutRepository
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@ExperimentalMaterialApi
@Composable
fun WorkoutCompleted(
    workoutId: Int,
    popBackStack: () -> Unit,
    navToWorkoutInProgress: () -> Unit,
    viewModel: WorkoutCompletedViewModel = getViewModel { parametersOf(workoutId) }
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = {
                navToWorkoutInProgress()
                viewModel.startWorkout()
            },
            Modifier
                .padding(16.dp)
                .height(40.dp),
            shape = RoundedCornerShape(percent = 100),
        ) {
            Icon(Icons.Default.Undo, null)
            Spacer(Modifier.width(8.dp))
            Text("Continue Workout")
        }
        Spacer(Modifier.height(80.dp))
        Column(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Nice Work.", style = typography.h2)
                Text("You've completed your workout.", style = typography.h5)
            }
            Row(
                Modifier
                    .toggleable(
                        value = false,
                        onValueChange = { checked ->
                            if (checked) {
                                viewModel.updateRoutine()
                            } else {
                                viewModel.resetRoutine()
                            }
                        }
                    )
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Update Routine")
            }
        }
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = { popBackStack() },
            Modifier
                .padding(16.dp)
                .height(40.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(percent = 100),
        ) {
            Text("Close")
        }
    }
}

@ExperimentalMaterialApi
@Composable
@Preview
fun WorkoutCompletedPreview() {
    MaterialTheme {
        Surface {
            WorkoutCompleted(
                workoutId = 0,
                popBackStack = { },
                navToWorkoutInProgress = {}
            )
        }
    }
}