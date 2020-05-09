package com.noahjutz.gymroutines.ui.routines

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noahjutz.gymroutines.R
import com.noahjutz.gymroutines.data.Rwe
import kotlinx.android.synthetic.main.listitem_routine.view.*

@Suppress("unused")
private const val TAG = "RoutinesAdapter"

private val diffUtil = object : DiffUtil.ItemCallback<Rwe>() {
    override fun areItemsTheSame(
        oldItem: Rwe,
        newItem: Rwe
    ): Boolean = oldItem.routine.routineId == newItem.routine.routineId

    override fun areContentsTheSame(
        oldItem: Rwe,
        newItem: Rwe
    ): Boolean = oldItem == newItem
}

class RweAdapter(
    private val onRoutineClickListener: OnRoutineClickListener
) : ListAdapter<Rwe, RweAdapter.RweHolder>(diffUtil) {
    fun getRweAt(pos: Int): Rwe = getItem(pos)

    inner class RweHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                this@RweAdapter.onRoutineClickListener.onRoutineClick(getItem(adapterPosition))
            }
            itemView.setOnLongClickListener {
                this@RweAdapter.onRoutineClickListener.onRoutineLongClick(getItem(adapterPosition))
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RweHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.listitem_routine, parent, false)
        return RweHolder(view)
    }

    override fun onBindViewHolder(holder: RweHolder, position: Int) {
        val rwe = getItem(position)

        val exercisesSb = StringBuilder()
        for (i in rwe.exerciseWrappers.indices) {
            exercisesSb.append(rwe.exerciseWrappers[i].exerciseId) // TODO: Show name instead of id
            if (i != rwe.exerciseWrappers.size - 1)
                exercisesSb.append("\n")
        }

        holder.apply {
            holder.itemView.name.text = rwe.routine.name
            holder.itemView.description.text = rwe.routine.description
            itemView.exercises.text = exercisesSb.toString()

            if (rwe.routine.description.trim().isEmpty()) itemView.description.visibility = GONE
            if (rwe.exerciseWrappers.isEmpty()) itemView.exercises.visibility = GONE
        }
    }

    interface OnRoutineClickListener {
        fun onRoutineClick(rwe: Rwe)
        fun onRoutineLongClick(rwe: Rwe)
    }
}
