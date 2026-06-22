package com.fitin60.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ProgramEntity::class,
        DayPlanEntity::class,
        WeeklyCheckinEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class Fitin60Database : RoomDatabase() {

    abstract fun programDao(): ProgramDao
    abstract fun dayPlanDao(): DayPlanDao
    abstract fun weeklyCheckinDao(): WeeklyCheckinDao

    companion object {
        @Volatile private var INSTANCE: Fitin60Database? = null

        fun getInstance(context: Context): Fitin60Database =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    Fitin60Database::class.java,
                    "fitin60.db",
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
