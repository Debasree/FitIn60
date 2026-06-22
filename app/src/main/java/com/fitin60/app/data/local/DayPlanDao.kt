package com.fitin60.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DayPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(days: List<DayPlanEntity>)

    @Update
    suspend fun update(day: DayPlanEntity)

    @Query("SELECT * FROM day_plans WHERE dayNumber = :day LIMIT 1")
    fun observeDay(day: Int): Flow<DayPlanEntity?>

    @Query("SELECT * FROM day_plans WHERE dayNumber = :day LIMIT 1")
    suspend fun getDay(day: Int): DayPlanEntity?

    @Query("SELECT * FROM day_plans ORDER BY dayNumber ASC")
    fun observeAll(): Flow<List<DayPlanEntity>>

    @Query("SELECT * FROM day_plans ORDER BY dayNumber ASC")
    suspend fun getAll(): List<DayPlanEntity>

    @Query("DELETE FROM day_plans")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM day_plans WHERE sleepDone = 1 AND mealsDone = 1 AND workoutDone = 1")
    fun observeCompletedCount(): Flow<Int>
}
