package com.fitin60.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_checkins")
data class WeeklyCheckinEntity(
    @PrimaryKey val weekNumber: Int,
    val weightKg: Double?,
    val photoPath: String?,
    val notes: String,
    val recordedAtMillis: Long?,
)
