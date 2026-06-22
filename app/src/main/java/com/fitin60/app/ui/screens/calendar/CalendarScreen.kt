package com.fitin60.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitin60.app.data.local.DayPlanEntity
import com.fitin60.app.ui.components.ScreenScaffold
import com.fitin60.app.ui.theme.Mint300
import com.fitin60.app.ui.theme.Mint500
import com.fitin60.app.ui.theme.Mint600
import com.fitin60.app.viewmodel.Fitin60ViewModel

@Composable
fun CalendarScreen(
    viewModel: Fitin60ViewModel,
    onOpenDay: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = "60-Day calendar",
        subtitle = "Tap any day to view the plan",
        onBack = onBack,
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            // Week labels
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                listOf("W1", "W2", "W3", "W4", "W5", "W6", "W7", "W8").forEach {
                    Text(
                        it,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.days, key = { it.dayNumber }) { day ->
                    DayCell(
                        day = day,
                        isToday = day.dayNumber == state.currentDay,
                        onClick = { onOpenDay(day.dayNumber) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: DayPlanEntity, isToday: Boolean, onClick: () -> Unit) {
    val complete = day.sleepDone && day.mealsDone && day.workoutDone
    val partial = !complete && (day.sleepDone || day.mealsDone || day.workoutDone)

    val (bg, fg) = when {
        complete -> Mint600 to Color.White
        partial -> Mint300 to MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable { onClick() }
            .then(
                if (isToday) Modifier.background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(14.dp),
                ) else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.dayNumber.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isToday) FontWeight.Black else FontWeight.SemiBold,
                color = fg,
            )
            if (isToday) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (complete) Color.White else Mint500),
                )
            }
        }
    }
}
