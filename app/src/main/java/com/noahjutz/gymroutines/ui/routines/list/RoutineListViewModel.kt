/*
 * Splitfit
 * Copyright (C) 2020  Noah Jutz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.noahjutz.gymroutines.ui.routines.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahjutz.gymroutines.data.RoutineRepository
import com.noahjutz.gymroutines.data.domain.Routine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RoutineListViewModel(
    private val repository: RoutineRepository,
) : ViewModel() {
    private val _nameFilter = MutableStateFlow("")
    val nameFilter = _nameFilter.asStateFlow()

    val routines: Flow<List<Routine>> =
        repository.routines.combine(nameFilter) { routines, filter ->
            routines.filter { filter.lowercase() in it.name.lowercase() }
        }

    fun setNameFilter(name: String) {
        _nameFilter.value = name
    }

    fun deleteRoutine(routineId: Int) {
        viewModelScope.launch {
            repository.getRoutine(routineId)?.let { repository.delete(it) }
        }
    }

    fun addRoutine(onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insert(Routine())
            onComplete(id)
        }
    }
}
