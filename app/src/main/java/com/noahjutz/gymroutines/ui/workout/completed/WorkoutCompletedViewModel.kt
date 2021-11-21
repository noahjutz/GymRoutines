package com.noahjutz.gymroutines.ui.workout.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.WorkoutRepository
import com.noahjutz.gymroutines.data.domain.RoutineWithSets
import com.noahjutz.gymroutines.data.domain.WorkoutWithSets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutCompletedViewModel(
    routineId: Int,
    workoutId: Int,
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {
    sealed class State {
        object Loading : State()
        object Error : State()
        data class Found(val routine: RoutineWithSets, val workout: WorkoutWithSets) : State()
    }

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val routine = routineRepository.getRoutineWithSets(routineId)
            val workout = workoutRepository.getWorkoutWithSets(workoutId)
            _state.value =
                if (routine == null || workout == null) State.Error
                else State.Found(routine, workout)
        }
    }

    fun updateRoutine() {
        (state.value as? State.Found)?.let { state ->
            viewModelScope.launch {
                routineRepository.insertWorkoutAsRoutine(state.workout)
            }
        }
    }
}
