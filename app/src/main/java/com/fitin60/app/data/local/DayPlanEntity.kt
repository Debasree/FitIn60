package com.fitin60.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_plans")
data class DayPlanEntity(
    @PrimaryKey val dayNumber: Int,
    val sleep: String,
    val meals: List<String>,
    val workout: List<String>,
    val notes: String,
    val sleepDone: Boolean = false,
    val mealsDone: Boolean = false,
    val workoutDone: Boolean = false,
    val userNotes: String = "",
)
