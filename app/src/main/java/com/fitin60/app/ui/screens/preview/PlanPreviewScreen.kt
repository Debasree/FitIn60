package com.fitin60.app.ui.screens.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitin60.app.data.local.DayPlanEntity
import com.fitin60.app.ui.components.PrimaryCard
import com.fitin60.app.ui.components.ScreenScaffold
import com.fitin60.app.ui.components.SectionLabel
import com.fitin60.app.ui.theme.Mint600
import com.fitin60.app.viewmodel.Fitin60ViewModel

@Composable
fun PlanPreviewScreen(
    viewModel: Fitin60ViewModel,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.importState.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = "Plan preview",
        subtitle = "${state.previewProgramName.ifBlank { "Imported program" }} - ${state.previewDays.size} days",
        onBack = onBack,
    ) { _ ->
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.previewDays, key = { it.dayNumber }) { day ->
                    PreviewDayCard(day = day)
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                ) { Text("Edit") }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint600),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    Text("Start 60 days", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PreviewDayCard(day: DayPlanEntity) {
    PrimaryCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Day ${day.dayNumber}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (day.sleep.isNotBlank()) {
                SectionLabel("Sleep")
                Text(day.sleep, style = MaterialTheme.typography.bodyMedium)
            }
            if (day.meals.isNotEmpty()) {
                SectionLabel("Meals")
                day.meals.forEach { Text("- $it", style = MaterialTheme.typography.bodyMedium) }
            }
            if (day.workout.isNotEmpty()) {
                SectionLabel("Workout")
                day.workout.forEach { Text("- $it", style = MaterialTheme.typography.bodyMedium) }
            }
            if (day.notes.isNotBlank()) {
                SectionLabel("Notes")
                Text(day.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
