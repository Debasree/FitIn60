package com.fitin60.app.data.parser

import com.fitin60.app.data.local.DayPlanEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

    // Manual JSON parsing so we can handle "day" as an Int (7) or a range string ("1-6").
    private fun tryJson(input: String): PlanParseResult {
        return try {
            val root = json.parseToJsonElement(input).jsonObject
            val programName = root["programName"]?.jsonPrimitive?.contentOrNull ?: "Fit in 60"
            val daysArr = root["days"]?.jsonArray
                ?: return PlanParseResult.Failure("JSON has no 'days' array.")
            if (daysArr.isEmpty()) return PlanParseResult.Failure("JSON plan has no days.")

            val expanded = mutableListOf<DayDto>()
            for (elem in daysArr) {
                val obj = elem as? JsonObject ?: continue
                val dayElem = obj["day"] ?: continue
                val dayNums = parseDayNumbers(dayElem)
                if (dayNums.isEmpty()) continue
                val sleep = obj["sleep"]?.jsonPrimitive?.contentOrNull ?: ""
                val meals = obj["meals"]?.jsonArray
                    ?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                val workout = obj["workout"]?.jsonArray
                    ?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                val notes = obj["notes"]?.jsonPrimitive?.contentOrNull ?: ""
                for (dayNum in dayNums) {
                    expanded.add(DayDto(day = dayNum, sleep = sleep, meals = meals, workout = workout, notes = notes))
                }
            }

            if (expanded.isEmpty()) return PlanParseResult.Failure("No valid days could be parsed from JSON.")

            PlanParseResult.Success(
                programName = programName.ifBlank { "Fit in 60" },
                days = normalize(expanded),
            )
        } catch (t: Throwable) {
            PlanParseResult.Failure(t.message ?: "JSON parse error")
        }
    }

    private fun parseDayNumbers(element: JsonElement): List<Int> {
        if (element !is JsonPrimitive) return emptyList()
        val content = element.content
        return if ('-' in content) {
            val parts = content.split('-')
            val start = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return emptyList()
            val end = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: return emptyList()
            if (start <= end) (start..end).toList() else emptyList()
        } else {
            content.toIntOrNull()?.let { listOf(it) } ?: emptyList()
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
        val byDay = sortedMapOf<Int, DayDto>()
        for (dto in days) {
            // First entry for each day wins (preserves original ordering intent for ranges).
            if (dto.day in 1..60 && !byDay.containsKey(dto.day)) byDay[dto.day] = dto
        }
        if (byDay.isEmpty()) return emptyList()

        val result = mutableListOf<DayPlanEntity>()
        var last: DayDto = byDay.values.first()
        for (day in 1..60) {
            val dto = byDay[day] ?: last
            last = dto
            result.add(
                DayPlanEntity(
                    dayNumber = day,
                    sleep = dto.sleep.trim(),
                    meals = dto.meals.map { it.trim() }.filter { it.isNotEmpty() },
                    workout = extractDayItems(dto.workout, day),
                    notes = dto.notes.trim(),
                )
            )
        }
        return result
    }

    // Matches "Day N: ..." items and, when present, returns only the entry for `dayNumber`.
    // If the list has no such labels (plain workout list), returns everything unchanged.
    private val dayItemPrefix = Regex("^Day\\s+(\\d+)\\s*:\\s*(.+)$", RegexOption.IGNORE_CASE)

    private fun extractDayItems(items: List<String>, dayNumber: Int): List<String> {
        val trimmed = items.map { it.trim() }.filter { it.isNotEmpty() }
        val hasLabels = trimmed.any { dayItemPrefix.matches(it) }
        if (!hasLabels) return trimmed

        val matched = trimmed.mapNotNull { item ->
            val m = dayItemPrefix.matchEntire(item) ?: return@mapNotNull null
            if (m.groupValues[1].toIntOrNull() == dayNumber) m.groupValues[2].trim() else null
        }
        return matched.ifEmpty { trimmed }
    }
}
