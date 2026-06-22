package com.fitin60.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitin60.app.ui.components.PrimaryCard
import com.fitin60.app.ui.components.ScreenScaffold
import com.fitin60.app.ui.components.SectionLabel
import com.fitin60.app.ui.components.VSpacer
import com.fitin60.app.ui.theme.Coral500
import com.fitin60.app.viewmodel.Fitin60ViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun SettingsScreen(
    viewModel: Fitin60ViewModel,
    onBack: () -> Unit,
    onStartAgain: () -> Unit,
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()
    var showConfirm by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = "Settings",
        subtitle = "Manage your program",
        onBack = onBack,
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimaryCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SectionLabel("Active program")
                    Text(
                        state.program?.name ?: "No program",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    VSpacer(4)
                    Text(
                        state.program?.startedAtMillis?.let {
                            "Started " + DateFormat.getDateInstance().format(Date(it))
                        } ?: "Import a 60-day plan to get started.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (state.currentDay != null) {
                        VSpacer(8)
                        Text(
                            "Day ${state.currentDay} of 60",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            PrimaryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showConfirm = true },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        tint = Coral500,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Start Again",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Coral500,
                        )
                        Text(
                            "Factory reset — deletes your plan, check-ins, and photos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            PrimaryCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Bedtime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text(
                            "Offline first",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "All data stays on this device. No accounts, no sync.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Start Again?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This wipes your current 60-day plan, all weekly check-ins, and photos. You will be asked to import a new plan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirm = false
                        onStartAgain()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Coral500),
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Start Again", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            },
        )
    }
}
