package com.fitin60.app.ui.screens.weekly

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fitin60.app.data.local.WeeklyCheckinEntity
import com.fitin60.app.ui.components.PrimaryCard
import com.fitin60.app.ui.components.ScreenScaffold
import com.fitin60.app.ui.components.SectionLabel
import com.fitin60.app.ui.components.VSpacer
import com.fitin60.app.ui.theme.Mint600
import com.fitin60.app.viewmodel.Fitin60ViewModel
import java.io.File

@Composable
fun WeeklyCheckinScreen(
    viewModel: Fitin60ViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()
    val currentWeek = state.currentWeek ?: 1
    val checkinsByWeek = remember(state.checkins) {
        state.checkins.associateBy { it.weekNumber }
    }

    var openWeek by remember { mutableStateOf<Int?>(null) }

    ScreenScaffold(
        title = "Weekly check-ins",
        subtitle = "8 weeks of progress, side by side",
        onBack = onBack,
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items((1..8).toList()) { week ->
                CheckinRow(
                    week = week,
                    isCurrent = week == currentWeek,
                    checkin = checkinsByWeek[week],
                    onOpen = { openWeek = week },
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    openWeek?.let { w ->
        CheckinDialog(
            week = w,
            existing = checkinsByWeek[w],
            onDismiss = { openWeek = null },
            onSave = { weight, photoUri, notes ->
                viewModel.saveCheckin(w, weight, photoUri, notes)
                openWeek = null
            },
        )
    }
}

@Composable
private fun CheckinRow(
    week: Int,
    isCurrent: Boolean,
    checkin: WeeklyCheckinEntity?,
    onOpen: () -> Unit,
) {
    PrimaryCard(modifier = Modifier.fillMaxWidth().clickable { onOpen() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isCurrent) Mint600 else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                val photoPath = checkin?.photoPath
                if (!photoPath.isNullOrBlank()) {
                    AsyncImage(
                        model = File(photoPath),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                    )
                } else {
                    Text(
                        "W$week",
                        color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Week $week",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    when {
                        checkin == null -> "Not yet logged"
                        checkin.weightKg != null -> "${"%.1f".format(checkin.weightKg)} kg" +
                            (if (!checkin.photoPath.isNullOrBlank()) " - photo added" else "")
                        else -> "Logged"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (checkin != null) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Mint600)
            }
        }
    }
}

@Composable
private fun CheckinDialog(
    week: Int,
    existing: WeeklyCheckinEntity?,
    onDismiss: () -> Unit,
    onSave: (weightKg: Double?, photoUri: Uri?, notes: String) -> Unit,
) {
    var weight by remember(week) { mutableStateOf(existing?.weightKg?.toString() ?: "") }
    var notes by remember(week) { mutableStateOf(existing?.notes ?: "") }
    var photoUri by remember(week) { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? -> if (uri != null) photoUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val parsed = weight.toDoubleOrNull()
                    onSave(parsed, photoUri, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Mint600),
            ) { Text("Save", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Week $week check-in", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { v -> weight = v.filter { it.isDigit() || it == '.' } },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                VSpacer(12)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                VSpacer(12)
                SectionLabel("Photo")
                VSpacer(6)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                picker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        val toShow = photoUri ?: existing?.photoPath?.let { File(it) }
                        if (toShow != null) {
                            AsyncImage(
                                model = toShow,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                            )
                        } else {
                            Icon(
                                Icons.Rounded.AddPhotoAlternate,
                                contentDescription = "Add photo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.size(12.dp))
                    Text(
                        "Tap to add a body photo from your gallery.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}
