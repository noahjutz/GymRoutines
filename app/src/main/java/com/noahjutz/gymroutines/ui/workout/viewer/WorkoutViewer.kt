package com.noahjutz.gymroutines.ui.workout.viewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noahjutz.gymroutines.data.domain.WorkoutWithSetGroups
import com.noahjutz.gymroutines.data.domain.duration
import com.noahjutz.gymroutines.ui.components.TopBar
import com.noahjutz.gymroutines.util.formatSimple
import com.noahjutz.gymroutines.util.pretty
import com.noahjutz.gymroutines.util.toStringOrBlank
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun WorkoutViewer(
    workoutId: Int,
    viewModel: WorkoutViewerViewModel = getViewModel { parametersOf(workoutId) },
    popBackStack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar(
                title = "View Workout",
                navigationIcon = {
                    IconButton(onClick = popBackStack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) {
        val workout by viewModel.workout.collectAsState()
        if (workout != null) WorkoutViewerContent(workout!!, viewModel)
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@ExperimentalTime
@Composable
fun WorkoutViewerContent(workout: WorkoutWithSetGroups, viewModel: WorkoutViewerViewModel) {
    LazyColumn {
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                text = workout.workout.name,
                modifier = Modifier.padding(horizontal = 24.dp),
                style = typography.h4,
            )
            Text(
                text = workout.workout.endTime.pretty(),
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Text(
                text = workout.workout.duration.pretty(),
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }

        items(workout.setGroups.sortedBy { it.group.position }) { setGroup ->
            val exercise by viewModel.getExercise(setGroup.group.exerciseId)
                .collectAsState(initial = null)

            Card(
                Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column {
                    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colors.primary) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                exercise?.name.toString(),
                                style = typography.h5,
                                modifier = Modifier
                                    .padding(24.dp)
                                    .weight(1f)
                            )
                        }
                    }
                    Column(Modifier.padding(vertical = 16.dp)) {
                        Row(Modifier.padding(horizontal = 4.dp)) {
                            val headerTextStyle = TextStyle(
                                color = MaterialTheme.colors.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            if (exercise?.logReps == true) Box(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Reps",
                                    style = headerTextStyle
                                )
                            }
                            if (exercise?.logWeight == true) Box(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Weight",
                                    style = headerTextStyle
                                )
                            }
                            if (exercise?.logTime == true) Box(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Time",
                                    style = headerTextStyle
                                )
                            }
                            if (exercise?.logDistance == true) Box(
                                Modifier
                                    .padding(4.dp)
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Distance",
                                    style = headerTextStyle
                                )
                            }
                            Box(
                                Modifier
                                    .padding(4.dp)
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null)
                            }
                        }
                        for (set in setGroup.sets) {
                            val dismissState = rememberDismissState()
                            LaunchedEffect(dismissState.currentValue) {
                                if (dismissState.currentValue != DismissValue.Default) {
                                    dismissState.snapTo(DismissValue.Default)
                                }
                            }
                            SwipeToDismiss(
                                state = dismissState,
                                background = {
                                    val alignment = when (dismissState.dismissDirection) {
                                        DismissDirection.StartToEnd -> Alignment.CenterStart
                                        DismissDirection.EndToStart -> Alignment.CenterEnd
                                        else -> Alignment.Center
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colors.secondary)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = alignment
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            null,
                                            tint = MaterialTheme.colors.onSecondary
                                        )
                                    }
                                },
                            ) {
                                Surface {
                                    Row(
                                        Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        val TableCell: @Composable RowScope.(@Composable BoxScope.() -> Unit) -> Unit =
                                            {
                                                ProvideTextStyle(
                                                    value = typography.body1.copy(
                                                        textAlign = TextAlign.Center,
                                                        color = MaterialTheme.colors.onSurface
                                                    )
                                                ) {
                                                    Surface(
                                                        modifier = Modifier
                                                            .padding(4.dp)
                                                            .weight(1f),
                                                        color = MaterialTheme.colors.onSurface.copy(
                                                            alpha = 0.1f
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                    ) {
                                                        Box(
                                                            Modifier
                                                                .padding(horizontal = 4.dp)
                                                                .height(56.dp),
                                                            contentAlignment = Alignment.Center,
                                                            content = it
                                                        )
                                                    }
                                                }

                                            }
                                        if (exercise?.logReps == true) {
                                            TableCell { Text(set.reps.toStringOrBlank()) }
                                        }
                                        if (exercise?.logWeight == true) {
                                            TableCell { Text(set.weight.formatSimple()) }
                                        }
                                        if (exercise?.logTime == true) {
                                            TableCell { Text(set.time.toStringOrBlank()) }
                                        }
                                        if (exercise?.logDistance == true) {
                                            TableCell { Text(set.distance.formatSimple()) }
                                        }
                                        Box(
                                            Modifier
                                                .padding(4.dp)
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (set.complete) MaterialTheme.colors.secondary
                                                    else MaterialTheme.colors.onSurface.copy(
                                                        alpha = 0.1f
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                "Complete",
                                                tint = if (set.complete) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
