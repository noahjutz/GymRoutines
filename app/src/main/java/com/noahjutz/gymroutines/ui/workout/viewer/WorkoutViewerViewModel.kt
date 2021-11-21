package com.noahjutz.gymroutines.ui.workout.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.ExerciseRepository
import com.noahjutz.gymroutines.data.WorkoutRepository
import com.noahjutz.gymroutines.data.domain.Exercise
import com.noahjutz.gymroutines.data.domain.Workout
import com.noahjutz.gymroutines.data.domain.WorkoutWithSets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutViewerViewModel(
    private val workoutId: Int,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    private val _workout = MutableStateFlow<WorkoutWithSets?>(null)
    val workout = _workout.asStateFlow()

    init {
        viewModelScope.launch {
            _workout.value = workoutRepository.getWorkoutWithSets(workoutId)
        }
    }

    fun getExercise(exerciseId: Int): Exercise? {
        return exerciseRepository.getExercise(exerciseId)
    }
}
