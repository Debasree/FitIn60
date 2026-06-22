package com.fitin60.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(program: ProgramEntity): Long

    @Query("SELECT * FROM programs ORDER BY id DESC LIMIT 1")
    fun observeActive(): Flow<ProgramEntity?>

    @Query("SELECT * FROM programs ORDER BY id DESC LIMIT 1")
    suspend fun getActive(): ProgramEntity?

    @Query("DELETE FROM programs")
    suspend fun clear()
}
