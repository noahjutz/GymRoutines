package com.noahjutz.gymroutines.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.noahjutz.gymroutines.R

@ExperimentalMaterialApi
@Composable
fun WorkoutBottomSheet(navToWorkoutInProgress: () -> Unit) {
    Surface(
        Modifier
            .clickable(onClick = navToWorkoutInProgress)
            .height(60.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f),
                text = stringResource(R.string.sheet_workout_in_progress),
                style = MaterialTheme.typography.h6
            )
            Icon(
                imageVector = Icons.Default.ExpandLess,
                contentDescription = stringResource(R.string.btn_expand),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}
