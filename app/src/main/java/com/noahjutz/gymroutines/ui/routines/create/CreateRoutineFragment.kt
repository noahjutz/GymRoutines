package com.noahjutz.gymroutines.ui.routines.create

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.data.domain.ExerciseHolder
import com.noahjutz.gymroutines.data.domain.ExerciseImpl
import com.noahjutz.gymroutines.data.domain.Set
import com.noahjutz.gymroutines.databinding.FragmentCreateRoutineBinding
import com.noahjutz.gymroutines.ui.routines.create.pick.SharedExerciseViewModel
import com.noahjutz.gymroutines.util.InjectorUtils
import com.noahjutz.gymroutines.util.ItemTouchHelperBuilder
import com.noahjutz.gymroutines.util.MarginItemDecoration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_create_routine.*
import kotlinx.android.synthetic.main.listitem_exercise_holder.view.*
import kotlinx.android.synthetic.main.listitem_routine.view.buttons
import kotlinx.android.synthetic.main.listitem_routine.view.description
import kotlinx.android.synthetic.main.listitem_routine.view.divider

@Suppress("unused")
private const val TAG = "CreateRoutineFragment"

class CreateRoutineFragment : Fragment() {

    private val sharedExerciseViewModel: SharedExerciseViewModel by activityViewModels()
    private val viewModel: CreateRoutineViewModel by viewModels {
        InjectorUtils.provideCreateViewModelFactory(requireActivity().application, args.routineId)
    }
    private val args: CreateRoutineFragmentArgs by navArgs()

    private lateinit var adapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentCreateRoutineBinding>(
            inflater,
            R.layout.fragment_create_routine,
            container,
            false
        ).apply {
            fragment = this@CreateRoutineFragment
            lifecycleOwner = viewLifecycleOwner
            viewmodel = viewModel
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initActivity()
        initRecyclerView()
        initViewModel()
    }

    private fun initActivity() {
        requireActivity().apply {
            title = if (args.routineId == -1) "Create Routine" else "Edit Routine"
            bottom_nav.visibility = GONE
        }
    }

    private fun initRecyclerView() {
        val itemTouchHelper = ItemTouchHelperBuilder(
            dragDirs = ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            onMove = { _, viewHolder, target ->
                swapExercises(viewHolder.adapterPosition, target.adapterPosition)
            }
        ).build()

        val onItemClickListener = object : ExerciseAdapter.OnExerciseClickListener {
            override fun onExerciseClick(card: MaterialCardView) {
                TransitionManager.beginDelayedTransition(create_routine_root, AutoTransition())
                val v = if (card.description.visibility == VISIBLE) GONE else VISIBLE
                card.apply {
                    description.visibility = v
                    buttons.visibility = v
                    divider.visibility = v
                    set_container.visibility = v
                }
            }

            override fun onAddSetClick(exercise: ExerciseImpl, card: MaterialCardView) {
                viewModel.addSet(Set(exercise.exerciseHolder.exerciseHolderId))
            }

            override fun onDeleteClick(position: Int) {
                deleteExercise(position)
            }
        }

        adapter = ExerciseAdapter(onItemClickListener)

        recycler_view.apply {
            adapter = this@CreateRoutineFragment.adapter
            layoutManager = LinearLayoutManager(this@CreateRoutineFragment.requireContext())
            addItemDecoration(
                MarginItemDecoration(resources.getDimension(R.dimen.any_margin_default).toInt())
            )
            isNestedScrollingEnabled = false
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun initViewModel() {
        viewModel.fullRoutine.observe(
            viewLifecycleOwner,
            Observer { fullRoutine ->
                viewModel.save()
                adapter.submitList(fullRoutine.exercises)
            }
        )

        viewModel.sets.observe(
            viewLifecycleOwner,
            Observer { sets ->
                // TODO: Fix bug:
                //  submitSetList doesn't get called initially
                adapter.submitSetList(sets)
                Log.d(TAG, "submitList: ${sets.map { it.setId }}")
            }
        )

        sharedExerciseViewModel.exercises.observe(
            viewLifecycleOwner,
            Observer { exercises ->
                for (e in exercises) {
                    val exerciseHolder =
                        ExerciseHolder(
                            e.exerciseId,
                            viewModel.fullRoutine.value!!.routine.routineId
                        )
                    val exerciseImpl = ExerciseImpl(exerciseHolder, e, listOf())
                    viewModel.addExercise(exerciseImpl)
                }

                if (exercises.isNotEmpty()) sharedExerciseViewModel.clearExercises()
            }
        )
    }

    fun addExercise() {
        val action = CreateRoutineFragmentDirections.addExercise()
        findNavController().navigate(action)
    }

    private fun deleteExercise(position: Int) {
        val exercise = adapter.getExercise(position)
        viewModel.removeExercise(exercise)
        adapter.notifyItemRemoved(position)
        Snackbar.make(
            recycler_view,
            "Deleted ${exercise.exercise.name}",
            Snackbar.LENGTH_SHORT
        )
            .setAnchorView(button_add_exercise)
            .setAction("Undo") {
                viewModel.addExercise(exercise)
                adapter.notifyItemInserted(adapter.itemCount)
            }
            .show()
    }

    private fun swapExercises(fromPos: Int, toPos: Int): Boolean {
        adapter.notifyItemMoved(fromPos, toPos)
        viewModel.swapExercises(fromPos, toPos)
        return true
    }
}
