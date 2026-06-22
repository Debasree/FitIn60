package com.fitin60.app.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitin60.app.data.local.DayPlanEntity
import com.fitin60.app.ui.components.BigDayBadge
import com.fitin60.app.ui.components.BulletList
import com.fitin60.app.ui.components.CircularProgress
import com.fitin60.app.ui.components.GradientCard
import com.fitin60.app.ui.components.PrimaryCard
import com.fitin60.app.ui.components.SectionLabel
import com.fitin60.app.ui.components.VSpacer
import com.fitin60.app.ui.theme.Mint600
import com.fitin60.app.viewmodel.Fitin60ViewModel

@Composable
fun TodayScreen(
    viewModel: Fitin60ViewModel,
    onOpenCalendar: () -> Unit,
    onOpenWeekly: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDay: (Int) -> Unit,
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()
    val today = state.currentDay
    val day = state.days.firstOrNull { it.dayNumber == today }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        TopBar(
            programName = state.program?.name ?: "Fitin60",
            onSettings = onOpenSettings,
        )

        VSpacer(8)

        if (day == null || today == null) {
            EmptyState()
            return@Column
        }

        HeroCard(day = today, dayPlan = day, totalDone = state.days.count { it.isComplete() })

        VSpacer(20)

        TodaysTasksCard(
            day = day,
            onToggleSleep = { viewModel.toggleSleep(day) },
            onToggleMeals = { viewModel.toggleMeals(day) },
            onToggleWorkout = { viewModel.toggleWorkout(day) },
            onOpenDayDetail = { onOpenDay(day.dayNumber) },
        )

        VSpacer(16)

        QuickActionsRow(
            onCalendar = onOpenCalendar,
            onWeekly = onOpenWeekly,
            onProgress = onOpenProgress,
        )

        VSpacer(16)

        NextCheckinCard(currentDay = today, onOpen = onOpenWeekly)

        VSpacer(32)
    }
}

private fun DayPlanEntity.isComplete() = sleepDone && mealsDone && workoutDone

@Composable
private fun TopBar(programName: String, onSettings: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                programName.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Today",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        IconButton(onClick = onSettings) {
            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
        }
    }
}

@Composable
private fun HeroCard(day: Int, dayPlan: DayPlanEntity, totalDone: Int) {
    GradientCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BigDayBadge(day = day)
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Day $day mission",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    "$totalDone / 60 days completed",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                CircularProgress(
                    fraction = totalDone / 60f,
                    label = "Program",
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (dayPlan.isComplete()) "Today complete. Recover well."
                    else "Hit sleep, meals, workout to close today.",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun TodaysTasksCard(
    day: DayPlanEntity,
    onToggleSleep: () -> Unit,
    onToggleMeals: () -> Unit,
    onToggleWorkout: () -> Unit,
    onOpenDayDetail: () -> Unit,
) {
    PrimaryCard(modifier = Modifier.fillMaxWidth().clickable { onOpenDayDetail() }) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Today's plan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text("Tap for details", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            TaskRow(
                icon = Icons.Rounded.AccessTime,
                title = "Sleep",
                subtitle = day.sleep.ifBlank { "No sleep target today." },
                done = day.sleepDone,
                onToggle = onToggleSleep,
            )
            TaskRow(
                icon = Icons.Rounded.Restaurant,
                title = "Meals",
                subtitle = day.meals.firstOrNull() ?: "No meals planned.",
                done = day.mealsDone,
                onToggle = onToggleMeals,
            )
            TaskRow(
                icon = Icons.Rounded.FitnessCenter,
                title = "Workout",
                subtitle = day.workout.firstOrNull() ?: "Rest day.",
                done = day.workoutDone,
                onToggle = onToggleWorkout,
            )

            if (day.meals.size > 1) {
                SectionLabel("Full meal plan")
                BulletList(day.meals)
            }
            if (day.workout.size > 1) {
                SectionLabel("Full workout")
                BulletList(day.workout)
            }
        }
    }
}

@Composable
private fun TaskRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    done: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = if (done) "Mark incomplete" else "Mark complete",
                tint = if (done) Mint600 else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onCalendar: () -> Unit,
    onWeekly: () -> Unit,
    onProgress: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickAction("Calendar", Icons.Rounded.CalendarMonth, Modifier.weight(1f), onCalendar)
        QuickAction("Weekly", Icons.Rounded.CheckCircle, Modifier.weight(1f), onWeekly)
        QuickAction("Progress", Icons.Rounded.ShowChart, Modifier.weight(1f), onProgress)
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    PrimaryCard(modifier = modifier.clickable { onClick() }, contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun NextCheckinCard(currentDay: Int, onOpen: () -> Unit) {
    val currentWeek = ((currentDay - 1) / 7) + 1
    val nextCheckinDay = (currentWeek * 7).coerceAtMost(60)
    val daysAway = (nextCheckinDay - currentDay).coerceAtLeast(0)

    PrimaryCard(modifier = Modifier.fillMaxWidth().clickable { onOpen() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SectionLabel("Next check-in")
                Text(
                    if (daysAway == 0) "Today: Week $currentWeek check-in"
                    else "Week $currentWeek check-in in $daysAway day${if (daysAway == 1) "" else "s"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "Log weight + a photo to track progress.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Rounded.ShowChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun EmptyState() {
    PrimaryCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Column {
            Text("No active program", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Use the sample plan or import your own from settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
