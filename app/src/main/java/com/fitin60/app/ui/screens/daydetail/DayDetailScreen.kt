package com.fitin60.app.ui.screens.daydetail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitin60.app.data.local.DayPlanEntity
import com.fitin60.app.ui.components.BulletList
import com.fitin60.app.ui.components.SectionLabel
import com.fitin60.app.ui.theme.Mint500
import com.fitin60.app.ui.theme.Mint600
import com.fitin60.app.viewmodel.Fitin60ViewModel
import kotlin.math.absoluteValue

// ── Gradient palettes per day status ──────────────────────────────────────────
private val GradToday   = listOf(Color(0xFF0EA5E9), Color(0xFF0891B2))  // sky → teal
private val GradDone    = listOf(Color(0xFF10B981), Color(0xFF059669))  // emerald
private val GradMissed  = listOf(Color(0xFFF59E0B), Color(0xFFD97706))  // amber
private val GradFuture  = listOf(Color(0xFF6366F1), Color(0xFF4338CA))  // indigo

// Display-time filter: if workout items carry "Day N:" labels keep only
// the one matching this day (handles plans imported before the parser fix).
private val DAY_LABEL = Regex("^Day\\s+(\\d+)\\s*:\\s*(.+)$", RegexOption.IGNORE_CASE)

private fun List<String>.forDay(n: Int): List<String> {
    val labeled = filter { DAY_LABEL.matches(it) }
    if (labeled.isEmpty()) return this
    val matched = labeled.mapNotNull { s ->
        val m = DAY_LABEL.matchEntire(s) ?: return@mapNotNull null
        if (m.groupValues[1].toIntOrNull() == n) m.groupValues[2].trim() else null
    }
    return matched.ifEmpty { this }
}

// ── Screen ─────────────────────────────────────────────────────────────────────
@Composable
fun DayDetailScreen(
    viewModel: Fitin60ViewModel,
    dayNumber: Int,
    onBack: () -> Unit,
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()
    val days  = state.days

    if (days.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Mint500)
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = (dayNumber - 1).coerceIn(0, days.lastIndex),
    ) { days.size }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(pagerState = pagerState, days = days, onBack = onBack)

        HorizontalPager(
            state            = pagerState,
            contentPadding   = PaddingValues(horizontal = 20.dp),
            pageSpacing      = 12.dp,
            modifier         = Modifier.fillMaxSize(),
        ) { page ->
            val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            DayCard(
                day        = days[page],
                currentDay = state.currentDay,
                viewModel  = viewModel,
                modifier   = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val s = lerp(0.92f, 1f, 1f - offset.coerceIn(0f, 1f))
                        scaleX = s; scaleY = s
                        alpha  = lerp(0.68f, 1f, 1f - offset.coerceIn(0f, 1f))
                    },
            )
        }
    }
}

// ── Top navigation bar ─────────────────────────────────────────────────────────
@Composable
private fun TopBar(
    pagerState: PagerState,
    days:       List<DayPlanEntity>,
    onBack:     () -> Unit,
) {
    val d = days.getOrNull(pagerState.currentPage)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector        = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint               = MaterialTheme.colorScheme.onBackground,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = "Day ${d?.dayNumber ?: "–"}",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onBackground,
            )
            if (d != null) {
                Text(
                    text  = "Week ${(d.dayNumber - 1) / 7 + 1} · of 60",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Text(
                text       = "${pagerState.currentPage + 1} / ${days.size}",
                modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// ── Single day card (one page in the pager) ────────────────────────────────────
@Composable
private fun DayCard(
    day:        DayPlanEntity,
    currentDay: Int?,
    viewModel:  Fitin60ViewModel,
    modifier:   Modifier = Modifier,
) {
    val isToday  = day.dayNumber == currentDay
    val isPast   = currentDay != null && day.dayNumber < currentDay
    val isDone   = day.sleepDone && day.mealsDone && day.workoutDone
    val gradient = when {
        isToday          -> GradToday
        isPast && isDone -> GradDone
        isPast           -> GradMissed
        else             -> GradFuture
    }
    val doneCount = listOf(day.sleepDone, day.mealsDone, day.workoutDone).count { it }
    val workout   = remember(day.dayNumber, day.workout) { day.workout.forDay(day.dayNumber) }
    var notes     by remember(day.dayNumber) { mutableStateOf(day.userNotes) }
    LaunchedEffect(day.userNotes) { notes = day.userNotes }

    Surface(
        modifier        = modifier,
        shape           = RoundedCornerShape(28.dp),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation  = 4.dp,
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            CardHeader(day = day, gradient = gradient, isToday = isToday, doneCount = doneCount)

            Column(
                modifier              = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement   = Arrangement.spacedBy(14.dp),
            ) {
                SectionCard(
                    icon     = Icons.Rounded.AccessTime,
                    title    = "Sleep",
                    done     = day.sleepDone,
                    onToggle = { viewModel.toggleSleep(day) },
                ) {
                    Text(
                        text  = day.sleep.ifBlank { "Aim for at least 7 hours of restful sleep." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                SectionCard(
                    icon     = Icons.Rounded.Restaurant,
                    title    = "Meals",
                    done     = day.mealsDone,
                    onToggle = { viewModel.toggleMeals(day) },
                ) {
                    if (day.meals.isNotEmpty()) {
                        BulletList(day.meals)
                    } else {
                        Text(
                            text  = "No meals planned.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                SectionCard(
                    icon     = Icons.Rounded.FitnessCenter,
                    title    = "Workout",
                    done     = day.workoutDone,
                    onToggle = { viewModel.toggleWorkout(day) },
                ) {
                    if (workout.isNotEmpty()) {
                        BulletList(workout)
                    } else {
                        Text(
                            text  = "Rest day — recovery and stretching.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (day.notes.isNotBlank()) {
                    Surface(
                        shape    = RoundedCornerShape(18.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionLabel("Plan notes")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text  = day.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // User's own notes (only saved on explicit tap)
                Surface(
                    shape    = RoundedCornerShape(18.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionLabel("My notes")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value         = notes,
                            onValueChange = { notes = it },
                            placeholder   = { Text("How did today go?") },
                            modifier      = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            shape         = RoundedCornerShape(14.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedTextColor   = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                        if (notes != day.userNotes) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.saveUserNotes(day, notes) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape    = RoundedCornerShape(14.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = Mint600),
                            ) {
                                Text("Save notes", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ── Gradient header with animated completion ring ──────────────────────────────
@Composable
private fun CardHeader(
    day:      DayPlanEntity,
    gradient: List<Color>,
    isToday:  Boolean,
    doneCount: Int,
) {
    val ringFraction by animateFloatAsState(
        targetValue    = doneCount / 3f,
        animationSpec  = tween(durationMillis = 800),
        label          = "ring",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Brush.linearGradient(gradient)),
    ) {
        // Decorative ambient circles for depth
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-55).dp, y = (-55).dp)
                .background(color = Color.White.copy(alpha = 0.07f), shape = CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(160.dp)
                .offset(x = 45.dp, y = 45.dp)
                .background(color = Color.White.copy(alpha = 0.07f), shape = CircleShape),
        )

        Row(
            modifier          = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: title stack
            Column(modifier = Modifier.weight(1f)) {
                if (isToday) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.22f),
                    ) {
                        Text(
                            text          = "TODAY",
                            modifier      = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style         = MaterialTheme.typography.labelSmall,
                            fontWeight    = FontWeight.ExtraBold,
                            color         = Color.White,
                            letterSpacing = 2.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text       = "Day ${day.dayNumber}",
                    style      = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color      = Color.White,
                )
                Text(
                    text  = "Week ${(day.dayNumber - 1) / 7 + 1} of 60",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.80f),
                )
            }

            // Right: animated completion ring
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress   = { 1f },
                    modifier   = Modifier.size(68.dp),
                    color      = Color.White.copy(alpha = 0.20f),
                    strokeWidth = 6.dp,
                    trackColor  = Color.Transparent,
                )
                CircularProgressIndicator(
                    progress   = { ringFraction },
                    modifier   = Modifier.size(68.dp),
                    color      = Color.White,
                    strokeWidth = 6.dp,
                    trackColor  = Color.Transparent,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "$doneCount/3",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color      = Color.White,
                    )
                    Text(
                        text  = "done",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.80f),
                    )
                }
            }
        }
    }
}

// ── Reusable section card (Sleep / Meals / Workout) ────────────────────────────
@Composable
private fun SectionCard(
    icon:     ImageVector,
    title:    String,
    done:     Boolean,
    onToggle: () -> Unit,
    content:  @Composable () -> Unit,
) {
    Surface(
        shape    = RoundedCornerShape(20.dp),
        color    = if (done)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier          = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            if (done) Mint600
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.13f)
                        ),
                    contentAlignment  = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = if (done) Color.White
                                             else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.weight(1f),
                    color      = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(
                    onClick  = onToggle,
                    modifier = Modifier.size(38.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.CheckCircle,
                        contentDescription = if (done) "Unmark" else "Mark complete",
                        tint               = if (done) Mint600
                                             else MaterialTheme.colorScheme.outline,
                        modifier           = Modifier.size(28.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
