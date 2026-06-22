package com.fitin60.app.data.parser

import kotlinx.serialization.Serializable

@Serializable
data class PlanDto(
    val programName: String = "Fit in 60",
    val days: List<DayDto> = emptyList(),
)

@Serializable
data class DayDto(
    val day: Int,
    val sleep: String = "",
    val meals: List<String> = emptyList(),
    val workout: List<String> = emptyList(),
    val notes: String = "",
)
