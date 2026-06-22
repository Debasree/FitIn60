package com.fitin60.app.ui.screens.importplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitin60.app.ui.components.PrimaryCard
import com.fitin60.app.ui.components.ScreenScaffold
import com.fitin60.app.ui.theme.Coral500
import com.fitin60.app.ui.theme.Mint600
import com.fitin60.app.viewmodel.Fitin60ViewModel

private const val SAMPLE_HINT = """{
  "programName": "Fit in 60",
  "days": [
    { "day": 1, "sleep": "Sleep by 10:30, 7.5h", "meals": ["Breakfast: oats", "Lunch: chicken + rice", "Dinner: fish + veg"], "workout": ["30 min walk", "3x12 squats"], "notes": "Easy start" }
  ]
}"""

@Composable
fun ImportPlanScreen(
    viewModel: Fitin60ViewModel,
    onParsed: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.importState.collectAsStateWithLifecycle()

    LaunchedEffect(state.previewDays.size) {
        if (state.previewDays.isNotEmpty()) onParsed()
    }

    ScreenScaffold(
        title = "Import plan",
        subtitle = "Paste JSON or markdown from your AI tool",
        onBack = onBack,
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PrimaryCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Expected JSON shape", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = SAMPLE_HINT,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Markdown also works: each section starts with \"Day N\" and includes Sleep:, Meals:, Workout:, Notes:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            OutlinedTextField(
                value = state.rawInput,
                onValueChange = viewModel::updateInput,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                placeholder = { Text("Paste your plan here…") },
                shape = RoundedCornerShape(20.dp),
                isError = state.errorMessage != null,
            )

            if (state.errorMessage != null) {
                Text(
                    state.errorMessage!!,
                    color = Coral500,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            Button(
                onClick = viewModel::previewParse,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Mint600),
                contentPadding = PaddingValues(vertical = 16.dp),
                enabled = state.rawInput.isNotBlank() && !state.isLoading,
            ) {
                Text(
                    if (state.isLoading) "Parsing…" else "Parse plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
