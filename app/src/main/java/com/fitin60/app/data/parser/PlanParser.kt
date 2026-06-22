package com.fitin60.app.data.parser

import com.fitin60.app.data.local.DayPlanEntity
import kotlinx.serialization.json.Json

sealed interface PlanParseResult {
    data class Success(
        val programName: String,
        val days: List<DayPlanEntity>,
    ) : PlanParseResult

    data class Failure(val message: String) : PlanParseResult
}

object PlanParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parse(input: String): PlanParseResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return PlanParseResult.Failure("Plan is empty.")

        val jsonAttempt = tryJson(trimmed)
        if (jsonAttempt is PlanParseResult.Success) return jsonAttempt

        val markdownAttempt = tryMarkdown(trimmed)
        if (markdownAttempt is PlanParseResult.Success) return markdownAttempt

        return PlanParseResult.Failure(
            "Could not read this plan. Paste valid JSON, or use markdown where each " +
                "day starts with \"Day N\" and includes Sleep:, Meals:, Workout:, and " +
                "optional Notes: sections."
        )
    }

    private fun tryJson(input: String): PlanParseResult {
        return try {
            val dto = json.decodeFromString(PlanDto.serializer(), input)
            if (dto.days.isEmpty()) {
                PlanParseResult.Failure("JSON plan has no days.")
            } else {
                val normalized = normalize(dto.days)
                PlanParseResult.Success(
                    programName = dto.programName.ifBlank { "Fit in 60" },
                    days = normalized,
                )
            }
        } catch (t: Throwable) {
            PlanParseResult.Failure(t.message ?: "JSON parse error")
        }
    }

    private val dayHeader = Regex(
        "^\\s*#*\\s*Day\\s+(\\d{1,2})\\b.*$",
        RegexOption.IGNORE_CASE,
    )

    private val labelRegex = Regex(
        "^(sleep|meals?|food|eating|workout|exercise|notes?)\\s*:(.*)",
        RegexOption.IGNORE_CASE,
    )

    private fun tryMarkdown(input: String): PlanParseResult {
        return try {
            val blocks = mutableListOf<MutableList<String>>()
            var current: MutableList<String>? = null

            for (line in input.lines()) {
                if (dayHeader.containsMatchIn(line)) {
                    current = mutableListOf(line)
                    blocks.add(current)
                } else current?.add(line)
            }

            if (blocks.isEmpty()) {
                return PlanParseResult.Failure("No 'Day N' headers found.")
            }

            val parsed = blocks.mapNotNull { parseDayBlock(it) }
            if (parsed.isEmpty()) {
                PlanParseResult.Failure("No valid day blocks could be parsed.")
            } else {
                PlanParseResult.Success(
                    programName = "Fit in 60",
                    days = normalize(parsed),
                )
            }
        } catch (t: Throwable) {
            PlanParseResult.Failure(t.message ?: "Markdown parse error")
        }
    }

    private fun parseDayBlock(block: List<String>): DayDto? {
        val header = block.firstOrNull() ?: return null
        val dayNumber = dayHeader.find(header)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: return null

        var section: String? = null
        var sleep = ""
        val meals = mutableListOf<String>()
        val workout = mutableListOf<String>()
        val notes = StringBuilder()

        fun add(value: String) {
            if (value.isBlank()) return
            when (section) {
                "sleep" -> sleep = if (sleep.isBlank()) value else "$sleep $value"
                "meals" -> meals.add(value)
                "workout" -> workout.add(value)
                "notes" -> {
                    if (notes.isNotEmpty()) notes.append('\n')
                    notes.append(value)
                }
            }
        }

        for (raw in block.drop(1)) {
            val line = raw.trim().trimStart('#', '-', '*', '•', ' ').trim()
            if (line.isEmpty()) continue

            val label = labelRegex.matchEntire(line)
            if (label != null) {
                section = when (label.groupValues[1].lowercase()) {
                    "sleep" -> "sleep"
                    "meals", "meal", "food", "eating" -> "meals"
                    "workout", "exercise" -> "workout"
                    "notes", "note" -> "notes"
                    else -> null
                }
                add(label.groupValues[2].trim())
            } else {
                add(line)
            }
        }

        return DayDto(
            day = dayNumber,
            sleep = sleep.trim(),
            meals = meals,
            workout = workout,
            notes = notes.toString().trim(),
        )
    }

    private fun normalize(days: List<DayDto>): List<DayPlanEntity> {
        val byDay = days
            .filter { it.day in 1..60 }
            .associateBy { it.day }
            .toSortedMap()

        val result = mutableListOf<DayPlanEntity>()
        var last: DayDto? = null
        for (day in 1..60) {
            val dto = byDay[day] ?: last?.copy(day = day) ?: DayDto(day = day)
            last = dto
            result.add(
                DayPlanEntity(
                    dayNumber = day,
                    sleep = dto.sleep.trim(),
                    meals = dto.meals.map { it.trim() }.filter { it.isNotEmpty() },
                    workout = dto.workout.map { it.trim() }.filter { it.isNotEmpty() },
                    notes = dto.notes.trim(),
                )
            )
        }
        return result
    }
}
