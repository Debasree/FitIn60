package com.fitin60.app.data.repository

import android.content.Context
import android.net.Uri
import com.fitin60.app.data.local.DayPlanDao
import com.fitin60.app.data.local.DayPlanEntity
import com.fitin60.app.data.local.ProgramDao
import com.fitin60.app.data.local.ProgramEntity
import com.fitin60.app.data.local.WeeklyCheckinDao
import com.fitin60.app.data.local.WeeklyCheckinEntity
import com.fitin60.app.data.parser.PlanParseResult
import com.fitin60.app.data.parser.PlanParser
import com.fitin60.app.data.parser.SeedPlan
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.min

class Fitin60Repository(
    private val programDao: ProgramDao,
    private val dayPlanDao: DayPlanDao,
    private val weeklyCheckinDao: WeeklyCheckinDao,
    private val appContext: Context,
) {

    fun observeProgram(): Flow<ProgramEntity?> = programDao.observeActive()
    fun observeAllDays(): Flow<List<DayPlanEntity>> = dayPlanDao.observeAll()
    fun observeDay(day: Int): Flow<DayPlanEntity?> = dayPlanDao.observeDay(day)
    fun observeCheckins(): Flow<List<WeeklyCheckinEntity>> = weeklyCheckinDao.observeAll()
    fun observeCompletedCount(): Flow<Int> = dayPlanDao.observeCompletedCount()

    suspend fun hasProgram(): Boolean = programDao.getActive() != null

    suspend fun startWithSeed() {
        replaceProgram(SeedPlan.PROGRAM_NAME, SeedPlan.build())
    }

    suspend fun importPlan(rawInput: String): PlanParseResult {
        val result = PlanParser.parse(rawInput)
        if (result is PlanParseResult.Success) {
            replaceProgram(result.programName, result.days)
        }
        return result
    }

    suspend fun previewParse(rawInput: String): PlanParseResult = PlanParser.parse(rawInput)

    private suspend fun replaceProgram(name: String, days: List<DayPlanEntity>) {
        programDao.clear()
        dayPlanDao.clear()
        weeklyCheckinDao.clear()
        programDao.insert(
            ProgramEntity(
                name = name,
                startedAtMillis = System.currentTimeMillis(),
            )
        )
        dayPlanDao.insertAll(days)
    }

    suspend fun resetProgram() {
        programDao.clear()
        dayPlanDao.clear()
        weeklyCheckinDao.clear()
        clearPhotos()
    }

    suspend fun updateDay(day: DayPlanEntity) {
        dayPlanDao.update(day)
    }

    suspend fun upsertCheckin(checkin: WeeklyCheckinEntity) {
        weeklyCheckinDao.upsert(checkin)
    }

    suspend fun getCheckin(week: Int): WeeklyCheckinEntity? = weeklyCheckinDao.get(week)

    /**
     * Copies an image picked via Android Photo Picker into app-private storage and returns
     * the absolute file path.
     */
    fun copyPhotoToInternalStorage(sourceUri: Uri): String? {
        return try {
            val dir = File(appContext.filesDir, "checkin_photos").apply { mkdirs() }
            val target = File(dir, "checkin_${UUID.randomUUID()}.jpg")
            appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                target.outputStream().use { input.copyTo(it) }
            }
            target.absolutePath
        } catch (_: Throwable) {
            null
        }
    }

    private fun clearPhotos() {
        runCatching {
            File(appContext.filesDir, "checkin_photos").listFiles()?.forEach { it.delete() }
        }
    }

    /** 1-based current day number, clamped to [1, 60]. Returns null when no program. */
    fun currentDay(program: ProgramEntity?): Int? {
        if (program == null) return null
        val elapsed = System.currentTimeMillis() - program.startedAtMillis
        val dayIndex = TimeUnit.MILLISECONDS.toDays(elapsed).toInt()
        return min(60, (dayIndex + 1).coerceAtLeast(1))
    }

    /** 1-based current week number, clamped to [1, 8]. */
    fun currentWeek(program: ProgramEntity?): Int? {
        val day = currentDay(program) ?: return null
        return min(8, ((day - 1) / 7) + 1)
    }
}
