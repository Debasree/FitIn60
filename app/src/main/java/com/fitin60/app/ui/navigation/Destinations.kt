package com.fitin60.app.ui.navigation

object Destinations {
    const val Onboarding = "onboarding"
    const val Import = "import"
    const val Preview = "preview"
    const val Today = "today"
    const val Calendar = "calendar"
    const val DayDetail = "day/{day}"
    const val Weekly = "weekly"
    const val Progress = "progress"
    const val Settings = "settings"

    fun dayDetail(day: Int) = "day/$day"
}
