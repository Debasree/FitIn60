package com.fitin60.app.ui.screens.daydetail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitin60.app.data.local.DayPlanEntity
import com.fitin60.app.ui.components.BulletList
import com.fitin60.app.ui.components.PrimaryCard
import com.fitin60.app.ui.components.ScreenScaffold
import com.fitin60.app.ui.components.SectionLabel
import com.fitin60.app.ui.components.VSpacer
import com.fitin60.app.ui.theme.Mint600
import com.fitin60.app.viewmodel.Fitin60ViewModel

@Composable
fun DayDetailScreen(
    viewModel: Fitin60ViewModel,
    dayNumber: Int,
    onBack: () -> Unit,
) {
    val day by viewModel.observeDay(dayNumber).collectAsStateWithLifecycle(initialValue = null)

    ScreenScaffold(
        title = "Day $dayNumber",
        subtitle = "Week ${(dayNumber - 1) / 7 + 1} - of 60",
        onBack = onBack,
    ) { _ ->
        val current = day
        if (current != null) {
            DayDetailBody(day = current, viewModel = viewModel)
        }
    }
}

@Composable
private fun DayDetailBody(day: DayPlanEntity, viewModel: Fitin60ViewModel) {
    var notes by remember(day.dayNumber) { mutableStateOf(day.userNotes) }
    LaunchedEffect(day.dayNumber) { notes = day.userNotes }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TaskCard(
            icon = Icons.Rounded.AccessTime,
            title = "Sleep",
            done = day.sleepDone,
            content = day.sleep.ifBlank { "Aim for at least 7 hours of restful sleep." },
            onToggle = { viewModel.toggleSleep(day) },
        )

        TaskCard(
            icon = Icons.Rounded.Restaurant,
            title = "Meals",
            done = day.mealsDone,
            content = "",
            details = day.meals,
            onToggle = { viewModel.toggleMeals(day) },
        )

        TaskCard(
            icon = Icons.Rounded.FitnessCenter,
            title = "Workout",
            done = day.workoutDone,
            content = "",
            details = day.workout,
            onToggle = { viewModel.toggleWorkout(day) },
        )

        if (day.notes.isNotBlank()) {
            PrimaryCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SectionLabel("Plan notes")
                    Text(day.notes, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        PrimaryCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SectionLabel("My notes")
                VSpacer(8)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("How did it go today?") },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(16.dp),
                )
                VSpacer(12)
                Button(
                    onClick = { viewModel.saveUserNotes(day, notes) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint600),
                ) { Text("Save notes", fontWeight = FontWeight.SemiBold) }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TaskCard(
    icon: ImageVector,
    title: String,
    done: Boolean,
    content: String,
    details: List<String> = emptyList(),
    onToggle: () -> Unit,
) {
    PrimaryCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggle) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = if (done) "Mark incomplete" else "Mark complete",
                        tint = if (done) Mint600 else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            if (content.isNotBlank()) {
                Text(content, style = MaterialTheme.typography.bodyLarge)
            }
            if (details.isNotEmpty()) {
                BulletList(details)
            } else if (content.isBlank()) {
                Text(
                    "Nothing planned.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
