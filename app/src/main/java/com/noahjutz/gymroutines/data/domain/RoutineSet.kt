package com.noahjutz.gymroutines.data.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_set_table",
    foreignKeys = [
        ForeignKey(
            entity = RoutineSetGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["groupId"])
    ]
)
data class RoutineSet(
    val groupId: Int,
    val reps: Int? = null,
    val weight: Double? = null,
    val time: Int? = null,
    val distance: Double? = null,

    @PrimaryKey(autoGenerate = true)
    val routineSetId: Int = 0
)
