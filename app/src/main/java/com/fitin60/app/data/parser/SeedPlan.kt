package com.fitin60.app.data.parser

import com.fitin60.app.data.local.DayPlanEntity

object SeedPlan {

    const val PROGRAM_NAME = "Fitin60 Starter"

    /** A reasonable starter 60-day plan used when the user opts to skip importing. */
    fun build(): List<DayPlanEntity> {
        val days = mutableListOf<DayPlanEntity>()
        for (day in 1..60) {
            val week = ((day - 1) / 7) + 1
            val dow = ((day - 1) % 7) + 1
            days.add(
                DayPlanEntity(
                    dayNumber = day,
                    sleep = sleepFor(week),
                    meals = mealsFor(week, dow),
                    workout = workoutFor(week, dow),
                    notes = noteFor(week),
                )
            )
        }
        return days
    }

    private fun sleepFor(week: Int): String = when (week) {
        1 -> "Sleep by 11:00 PM. Target 7 hours."
        2 -> "Sleep by 10:45 PM. Target 7.25 hours."
        3, 4 -> "Sleep by 10:30 PM. Target 7.5 hours. No screens 30 min before bed."
        5, 6 -> "Sleep by 10:30 PM. Target 7.5 hours. Stretch 5 min before bed."
        else -> "Sleep by 10:15 PM. Target 8 hours. Cool, dark room."
    }

    private fun mealsFor(week: Int, dow: Int): List<String> {
        val breakfast = when (week) {
            1, 2 -> "Breakfast: 3 eggs, oats with berries, black coffee."
            3, 4 -> "Breakfast: Greek yogurt, banana, almonds, green tea."
            5, 6 -> "Breakfast: Veggie omelette, whole-grain toast, fruit."
            else -> "Breakfast: Protein smoothie (whey, oats, peanut butter, banana)."
        }
        val lunch = if (dow % 2 == 1) {
            "Lunch: Grilled chicken, brown rice, salad with olive oil."
        } else {
            "Lunch: Paneer/tofu stir-fry, quinoa, mixed greens."
        }
        val snack = "Snack: Apple + 10 almonds, or protein shake post-workout."
        val dinner = when (week) {
            1, 2 -> "Dinner: Fish/lentils, roasted veg, small portion of rice."
            3, 4 -> "Dinner: Lean meat or dal, steamed veg, salad."
            5, 6 -> "Dinner: High-protein bowl, light carbs, no fried food."
            else -> "Dinner: Protein + veg only. Stop eating by 8:30 PM."
        }
        return listOf(breakfast, lunch, snack, dinner)
    }

    private fun workoutFor(week: Int, dow: Int): List<String> {
        if (dow == 7) return listOf("Rest day", "20 min easy walk", "Mobility / stretching 10 min")
        return when (week) {
            1 -> listOf(
                "Warm-up: 5 min brisk walk",
                "Bodyweight squats 3 x 12",
                "Push-ups (knee if needed) 3 x 8",
                "Plank 3 x 20s",
                "Cool-down: 20 min walk",
            )
            2 -> listOf(
                "Warm-up: 5 min",
                "Goblet squats 3 x 12",
                "Push-ups 3 x 10",
                "Reverse lunges 3 x 10/side",
                "Plank 3 x 30s",
                "Walk 25 min",
            )
            3 -> listOf(
                "Warm-up: 5 min",
                "Dumbbell squats 4 x 10",
                "Push-ups 4 x 10",
                "Bent-over rows 4 x 10",
                "Walk/jog intervals 20 min",
            )
            4 -> listOf(
                "Warm-up: 5 min",
                "Squat 4 x 8",
                "Bench / push-ups 4 x 10",
                "Row 4 x 10",
                "Plank 3 x 45s",
                "Cardio 25 min",
            )
            5 -> listOf(
                "Warm-up: 5 min",
                "Deadlift / hinge 4 x 8",
                "Overhead press 4 x 8",
                "Pull-ups / lat pulldown 4 x 8",
                "HIIT 15 min",
            )
            6 -> listOf(
                "Warm-up: 5 min",
                "Squat 5 x 5",
                "Press 5 x 5",
                "Row 5 x 5",
                "Core circuit 3 rounds",
                "Cardio 20 min",
            )
            7 -> listOf(
                "Warm-up: 5 min",
                "Squat 5 x 5 (heavier)",
                "Bench / push variation 5 x 5",
                "Deadlift 3 x 5",
                "HIIT 20 min",
            )
            else -> listOf(
                "Warm-up: 5 min",
                "Full-body lift, max sustainable effort",
                "Conditioning 25 min",
                "Cool-down stretch 10 min",
            )
        }
    }

    private fun noteFor(week: Int): String = when (week) {
        1 -> "Build the habit. Effort over intensity this week."
        2 -> "Show up. Add small progressions."
        3, 4 -> "Track weight on weekly check-in. Stay consistent."
        5, 6 -> "Push intensity. Sleep is non-negotiable."
        7 -> "Tighten nutrition. Last stretch."
        else -> "Finish strong. Take your final photo with intent."
    }
}
