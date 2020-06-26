package com.noahjutz.gymroutines.data.domain

import androidx.room.*

/**
 * TODO
 */
@Entity(tableName = "exercise_holder_table")
data class ExerciseHolder(
    val exerciseId: Int,
    val routineId: Int,
    var position: Int = -1,
    @PrimaryKey(autoGenerate = true) val exerciseHolderId: Int = 0
)