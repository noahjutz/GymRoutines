/*
 * GymRoutines
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

package com.noahjutz.gymroutines.ui.routines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.data.domain.Routine
import com.noahjutz.gymroutines.util.ItemTouchHelperBuilder
import com.noahjutz.gymroutines.util.MarginItemDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoutinesFragment : Fragment(), RoutineAdapter.RoutineListener {

    private val viewModel: RoutinesViewModel by viewModels()
    private val adapter = RoutineAdapter(this)

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            MaterialTheme(colors = if (isSystemInDarkTheme()) darkColors() else lightColors()) {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { addRoutine() },
                            icon = { Icon(Icons.Default.Add) }
                        )
                    }
                ) {
                    var showDialog by remember { mutableStateOf(false) }
                    var toDelete by remember { mutableStateOf<Routine?>(null) }
                    val routines by viewModel.routines.observeAsState()
                    LazyColumnFor(items = routines ?: emptyList()) { routine ->
                        ListItem(
                            text = {
                                Text(routine.name.takeIf { it.isNotBlank() } ?: "Unnamed")
                            },
                            modifier = Modifier.clickable(
                                onClick = {
                                    onRoutineClick(routine)
                                }, onLongClick = {
                                    toDelete = routine
                                    showDialog = true
                                }
                            )
                        )
                    }
                    if (showDialog) {
                        AlertDialog(
                            title = { Text("Delete ${toDelete?.name?.takeIf { it.isNotBlank() } ?: "Unnamed"}?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        toDelete?.routineId?.let { viewModel.deleteRoutine(it) }
                                        showDialog = false
                                    },
                                    content = { Text("Yes") })
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showDialog = false },
                                    content = { Text("Cancel") })
                            },
                            onDismissRequest = { showDialog = false }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActivity()
        initViewModel()
    }

    private fun initActivity() {
        requireActivity().apply {
            title = "Routines"
            findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = VISIBLE
        }
    }

    private fun initRecyclerView() {
        val itemTouchHelper = ItemTouchHelperBuilder(
            swipeDirs = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            onSwipedCall = { viewHolder, _ -> deleteRoutine(viewHolder.adapterPosition) }
        ).build()

        recyclerView.apply {
            adapter = this@RoutinesFragment.adapter
            layoutManager = LinearLayoutManager(this@RoutinesFragment.requireContext())
            setHasFixedSize(true)
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.any_margin_default).toInt()
                )
            )
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun initViewModel() {
        viewModel.routines.observe(viewLifecycleOwner) { routines ->
            adapter.items = routines
        }
    }

    fun addRoutine() {
        val action = RoutinesFragmentDirections.addRoutine()
        findNavController().navigate(action)
    }

    private fun deleteRoutine(position: Int) {
        val routine = adapter.items[position]
        viewModel.deleteRoutine(routine.routineId)
        Snackbar.make(
            recyclerView,
            "Deleted ${routine.name}",
            Snackbar.LENGTH_SHORT
        )
    }

    override fun onRoutineClick(routine: Routine) {
        val action = RoutinesFragmentDirections.addRoutine(routine.routineId)
        findNavController().navigate(action)
    }
}
