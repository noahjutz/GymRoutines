package com.noahjutz.gymroutines.ui.workout.completed

import androidx.lifecycle.ViewModel
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.WorkoutRepository

class WorkoutCompletedViewModel(
    workoutId: Int,
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {
}
