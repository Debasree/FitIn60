package com.fitin60.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyCheckinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(checkin: WeeklyCheckinEntity)

    @Query("SELECT * FROM weekly_checkins ORDER BY weekNumber ASC")
    fun observeAll(): Flow<List<WeeklyCheckinEntity>>

    @Query("SELECT * FROM weekly_checkins WHERE weekNumber = :week LIMIT 1")
    suspend fun get(week: Int): WeeklyCheckinEntity?

    @Query("DELETE FROM weekly_checkins")
    suspend fun clear()
}
