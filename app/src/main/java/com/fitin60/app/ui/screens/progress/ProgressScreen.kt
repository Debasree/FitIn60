package com.fitin60.app.ui.screens.progress

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fitin60.app.data.local.WeeklyCheckinEntity
import com.fitin60.app.ui.components.CircularProgress
import com.fitin60.app.ui.components.PrimaryCard
import com.fitin60.app.ui.components.ScreenScaffold
import com.fitin60.app.ui.components.SectionLabel
import com.fitin60.app.ui.components.VSpacer
import com.fitin60.app.ui.theme.Mint500
import com.fitin60.app.ui.theme.Mint600
import com.fitin60.app.viewmodel.Fitin60ViewModel
import java.io.File

@Composable
fun ProgressScreen(
    viewModel: Fitin60ViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()
    val completed = state.days.count { it.sleepDone && it.mealsDone && it.workoutDone }
    val checkins = state.checkins
    val weightSeries = checkins.mapNotNull { c -> c.weightKg?.let { c.weekNumber to it } }

    ScreenScaffold(
        title = "Progress",
        subtitle = "$completed / 60 days closed",
        onBack = onBack,
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PrimaryCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgress(
                        fraction = completed / 60f,
                        label = "Days",
                        size = 100.dp,
                    )
                    Spacer(Modifier.width(20.dp))
                    Column {
                        SectionLabel("Streak")
                        Text(
                            "$completed days",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            "Keep showing up.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            PrimaryCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SectionLabel("Weight (kg)")
                    VSpacer(8)
                    if (weightSeries.size < 2) {
                        Text(
                            "Log weight in 2+ weekly check-ins to see a curve.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        WeightChart(
                            series = weightSeries,
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                        )
                    }
                }
            }

            PrimaryCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SectionLabel("Photo timeline")
                    VSpacer(8)
                    val photoCheckins = checkins.filter { !it.photoPath.isNullOrBlank() }
                    if (photoCheckins.isEmpty()) {
                        Text(
                            "Photos you add to weekly check-ins appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(photoCheckins) { c -> PhotoTile(c) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PhotoTile(c: WeeklyCheckinEntity) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(width = 110.dp, height = 150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AsyncImage(
                model = c.photoPath?.let { File(it) },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        VSpacer(6)
        Text("Week ${c.weekNumber}", style = MaterialTheme.typography.labelLarge)
        if (c.weightKg != null) {
            Text(
                "${"%.1f".format(c.weightKg)} kg",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeightChart(
    series: List<Pair<Int, Double>>,
    modifier: Modifier = Modifier,
) {
    val minWeight = series.minOf { it.second }
    val maxWeight = series.maxOf { it.second }
    val span = (maxWeight - minWeight).takeIf { it > 0.1 } ?: 1.0
    val lineColor = Mint600
    val fillColor = Mint500.copy(alpha = 0.18f)
    val gridColor = Color.LightGray.copy(alpha = 0.4f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padX = 12f
        val padY = 12f

        // grid
        val rows = 4
        for (i in 0..rows) {
            val y = padY + (h - padY * 2) * (i / rows.toFloat())
            drawLine(
                color = gridColor,
                start = Offset(padX, y),
                end = Offset(w - padX, y),
                strokeWidth = 1f,
            )
        }

        val points = series.mapIndexed { idx, (_, weight) ->
            val x = padX + (w - padX * 2) * (idx / (series.size - 1).coerceAtLeast(1).toFloat())
            val normalized = ((weight - minWeight) / span).toFloat()
            val y = padY + (h - padY * 2) * (1f - normalized)
            Offset(x, y)
        }

        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(points.last().x, h - padY)
            lineTo(points.first().x, h - padY)
            close()
        }
        drawPath(path = fillPath, color = fillColor)
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 4f))

        points.forEach { p ->
            drawCircle(color = lineColor, radius = 5f, center = p)
            drawCircle(color = Color.White, radius = 2.5f, center = p)
        }
    }
}
