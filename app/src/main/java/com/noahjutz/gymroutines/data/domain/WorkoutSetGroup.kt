package com.noahjutz.gymroutines.data.domain

import androidx.room.*

@Entity(
    tableName = "workout_set_group_table",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            childColumns = ["workoutId"],
            parentColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutId"])
    ]
)
data class WorkoutSetGroup(
    val workoutId: Int,
    val exerciseId: Int,
    val position: Int,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

data class WorkoutSetGroupWithSets(
    @Embedded val group: WorkoutSetGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    ) val sets: List<WorkoutSet>
)
