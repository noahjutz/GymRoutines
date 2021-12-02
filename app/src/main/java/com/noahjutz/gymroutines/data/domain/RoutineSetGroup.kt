package com.noahjutz.gymroutines.data.domain

import androidx.room.*

@Entity(
    tableName = "routine_set_group_table",
    foreignKeys = [
        ForeignKey(
            entity = Routine::class,
            parentColumns = ["routineId"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["routineId"])
    ]
)
data class RoutineSetGroup(
    val routineId: Int,
    val exerciseId: Int,
    val position: Int,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

data class RoutineSetGroupWithSets(
    @Embedded val group: RoutineSetGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    ) val sets: List<RoutineSet>
)
