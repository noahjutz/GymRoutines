package com.noahjutz.gymroutines.ui.workout.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.ExerciseRepository
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.WorkoutRepository
import com.noahjutz.gymroutines.data.domain.Exercise
import com.noahjutz.gymroutines.data.domain.WorkoutWithSetGroups
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WorkoutViewerViewModel(
    private val workoutId: Int,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {
    private val _workout = MutableStateFlow<WorkoutWithSetGroups?>(null)
    val workout = _workout.asStateFlow()

    val routineName = workout.map { workout ->
        workout?.workout?.routineId?.let { routineId ->
            routineRepository.getRoutine(routineId)?.name
        } ?: ""
    }

    init {
        viewModelScope.launch {
            _workout.value = workoutRepository.getWorkoutWithSetGroups(workoutId)
        }
    }

    fun getExercise(exerciseId: Int): Flow<Exercise?> {
        return exerciseRepository.getExerciseFlow(exerciseId)
    }
}
