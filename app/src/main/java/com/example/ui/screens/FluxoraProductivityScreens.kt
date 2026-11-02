package com.example.ui.screens

import com.example.i18n.t
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FluxoraAuroraMint
import com.example.ui.theme.FluxoraCosmicViolet
import com.example.ui.theme.FluxoraCosmicVioletDeep
import com.example.ui.theme.FluxoraDeepOcean
import com.example.ui.theme.FluxoraDreamLavender
import com.example.ui.theme.FluxoraMidnightAbyss
import com.example.ui.theme.FluxoraMoonlightBlue
import com.example.ui.theme.FluxoraStormTeal
import com.example.ui.theme.FluxoraSunsetCoral

// ============================================================================
// DATA MODELS & PRESETS MATCHING DESIGN SYSTEM REFERENCE
// ============================================================================

enum class ProductivityTaskTag(val label: String, val color: Color) {
    STUDY("Study", FluxoraCosmicViolet),
    BUILD("Build", FluxoraStormTeal),
    DOCS("Docs", FluxoraMoonlightBlue),
    HEALTH("Health", FluxoraAuroraMint),
    DSA("DSA", FluxoraCosmicViolet)
}

data class ProductivityTask(
    val id: String,
    val title: String,
    val tag: ProductivityTaskTag,
    val isCompleted: Boolean,
    val dueLabel: String = "Today"
)

data class ProductivityProject(
    val id: String,
    val name: String,
    val status: String,
    val progressPercent: Int
)

// ============================================================================
// 1. DASHBOARD PRODUCTIVITY HERO BANNER & METRICS
// ============================================================================

@Composable
fun ProductivityHeroBanner(
    modifier: Modifier = Modifier,
    onStartFocusSession: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = FluxoraDeepOcean,
        border = BorderStroke(1.dp, FluxoraCosmicViolet.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("productivity_hero_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            FluxoraMidnightAbyss.copy(alpha = 0.90f),
                            FluxoraDeepOcean.copy(alpha = 0.80f),
                            FluxoraCosmicViolet.copy(alpha = 0.30f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // Mountain landscape backdrop drawing
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomEnd)
            ) {
                val w = size.width
                val h = size.height

                // Soft celestial moon in top right
                drawCircle(
                    color = FluxoraCosmicViolet.copy(alpha = 0.35f),
                    radius = 32.dp.toPx(),
                    center = Offset(w * 0.88f, h * 0.30f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.20f),
                    radius = 20.dp.toPx(),
                    center = Offset(w * 0.88f, h * 0.30f)
                )

                // Atmospheric mountain silhouttes
                val mountainPath = Path().apply {
                    moveTo(w * 0.50f, h)
                    lineTo(w * 0.65f, h * 0.40f)
                    lineTo(w * 0.78f, h * 0.70f)
                    lineTo(w * 0.88f, h * 0.30f)
                    lineTo(w, h * 0.65f)
                    lineTo(w, h)
                    close()
                }
                drawPath(
                    path = mountainPath,
                    color = FluxoraStormTeal.copy(alpha = 0.22f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = t("your_productivity_flow"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = t("keep_the_momentum"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = FluxoraMoonlightBlue
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Play Button
                Surface(
                    onClick = onStartFocusSession,
                    shape = CircleShape,
                    color = FluxoraCosmicViolet,
                    border = BorderStroke(1.5.dp, FluxoraDreamLavender.copy(alpha = 0.50f)),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("hero_start_focus_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = t("start_focus"),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductivityMetricCardsRow(
    tasksCompleted: Int = 6,
    totalTasks: Int = 10,
    focusTime: String = "4h 28m",
    goalsProgress: Int = 68,
    streakDays: Int = 12,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card 1: Tasks Today
        ProductivityMetricCard(
            title = t("tasks_today"),
            value = "$tasksCompleted / $totalTasks",
            progress = tasksCompleted.toFloat() / totalTasks.toFloat(),
            icon = Icons.Default.CheckBox,
            iconTint = FluxoraCosmicViolet,
            modifier = Modifier.weight(1f)
        )

        // Card 2: Focus Time
        ProductivityMetricCard(
            title = t("focus_time"),
            value = focusTime,
            progress = 0.74f,
            icon = Icons.Default.Timer,
            iconTint = FluxoraStormTeal,
            modifier = Modifier.weight(1f)
        )

        // Card 3: Goals Progress
        ProductivityMetricCard(
            title = t("goals_progress"),
            value = "$goalsProgress%",
            progress = goalsProgress / 100f,
            icon = Icons.Default.TrackChanges,
            iconTint = FluxoraCosmicViolet,
            modifier = Modifier.weight(1f)
        )

        // Card 4: Streak
        ProductivityMetricCard(
            title = t("streak"),
            value = "$streakDays ${t("days")}",
            progress = 1.0f,
            icon = Icons.Default.LocalFireDepartment,
            iconTint = FluxoraSunsetCoral,
            showProgressBar = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ProductivityMetricCard(
    title: String,
    value: String,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    showProgressBar: Boolean = true
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FluxoraDeepOcean.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.15f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = iconTint.copy(alpha = 0.16f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = FluxoraMoonlightBlue
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (showProgressBar) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = iconTint,
                    trackColor = FluxoraMidnightAbyss.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ============================================================================
// 2. TODAY'S TASKS CARD & WEEKLY FOCUS & OVERALL PROGRESS
// ============================================================================

@Composable
fun ProductivityTodayTasksCard(
    tasks: List<ProductivityTask>,
    onToggleTask: (String) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = FluxoraDeepOcean.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.18f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = t("todays_tasks"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    onClick = onAddTaskClick,
                    shape = RoundedCornerShape(50),
                    color = FluxoraCosmicViolet.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, FluxoraCosmicViolet.copy(alpha = 0.40f)),
                    modifier = Modifier.testTag("add_task_header_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = t("add_task"),
                            tint = FluxoraDreamLavender,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = t("add_task"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = FluxoraDreamLavender
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            tasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToggleTask(task.id) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = "Toggle ${task.title}",
                        tint = if (task.isCompleted) FluxoraCosmicViolet else FluxoraMoonlightBlue,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (task.isCompleted) FluxoraMoonlightBlue.copy(alpha = 0.6f) else FluxoraDreamLavender,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = task.tag.color.copy(alpha = 0.16f),
                        border = BorderStroke(0.8.dp, task.tag.color.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = task.tag.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = task.tag.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductivityWeeklyFocusCard(
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val hours = listOf(4.5f, 3.8f, 5.2f, 4.0f, 6.0f, 3.2f, 2.5f)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = FluxoraDeepOcean.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.18f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = t("weekly_focus"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { i, day ->
                    val fraction = (hours[i] / 6.0f).coerceIn(0.1f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height((80 * fraction).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(FluxoraCosmicViolet, FluxoraCosmicVioletDeep)
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = FluxoraMoonlightBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductivityOverallProgressCard(
    progressPercent: Int = 72,
    weeklyDelta: Int = 8,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = FluxoraDeepOcean.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.18f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = t("overall_progress"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 9.dp.toPx()
                    // Track circle
                    drawCircle(
                        color = FluxoraMidnightAbyss.copy(alpha = 0.6f),
                        style = Stroke(width = stroke)
                    )
                    // Progress arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(FluxoraCosmicViolet, FluxoraAuroraMint, FluxoraCosmicViolet)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * (progressPercent / 100f),
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Trend Up",
                    tint = FluxoraAuroraMint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+$weeklyDelta% ${t("from_last_week")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FluxoraAuroraMint,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ============================================================================
// 3. FULL TASKS SCREEN (Matching bottom reference row)
// ============================================================================

@Composable
fun FluxoraTasksScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val tasksList = remember {
        mutableStateListOf(
            ProductivityTask("t1", "Study Graphs", ProductivityTaskTag.DSA, false, "Today"),
            ProductivityTask("t2", "Build authentication", ProductivityTaskTag.BUILD, false, "Today"),
            ProductivityTask("t3", "Write documentation", ProductivityTaskTag.DOCS, false, "Tomorrow"),
            ProductivityTask("t4", "Morning workout", ProductivityTaskTag.HEALTH, true, "Today"),
            ProductivityTask("t5", "Refine Recharts Financial Graph", ProductivityTaskTag.BUILD, true, "Yesterday"),
            ProductivityTask("t6", "Complete DSA revision", ProductivityTaskTag.STUDY, true, "Today")
        )
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FluxoraMidnightAbyss)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("fluxora_tasks_screen")
    ) {
        // Header Row: Back + "Tasks" + Search + Add
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = t("back"),
                        tint = FluxoraDreamLavender
                    )
                }
                Text(
                    text = t("tasks"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /* toggle search */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = t("tasks"),
                        tint = FluxoraMoonlightBlue
                    )
                }
                Surface(
                    onClick = { showAddDialog = true },
                    shape = CircleShape,
                    color = FluxoraCosmicViolet,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("tasks_add_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = t("add_task"),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Pills: All | Active | Completed
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All" to "all", "Active" to "active", "Completed" to "completed").forEach { (filterKey, translationKey) ->
                val isSelected = selectedFilter == filterKey
                Surface(
                    onClick = { selectedFilter = filterKey },
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) FluxoraCosmicViolet else FluxoraDeepOcean.copy(alpha = 0.6f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) FluxoraCosmicViolet else FluxoraMoonlightBlue.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = t(translationKey),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else FluxoraMoonlightBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filtered Tasks List
        val filtered = tasksList.filter {
            when (selectedFilter) {
                "Active" -> !it.isCompleted
                "Completed" -> it.isCompleted
                else -> true
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.id }) { task ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FluxoraDeepOcean.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val idx = tasksList.indexOfFirst { it.id == task.id }
                                if (idx != -1) {
                                    tasksList[idx] = task.copy(isCompleted = !task.isCompleted)
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = "Complete ${task.title}",
                                tint = if (task.isCompleted) FluxoraCosmicViolet else FluxoraMoonlightBlue
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (task.isCompleted) FluxoraMoonlightBlue.copy(alpha = 0.6f) else Color.White,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = task.tag.color.copy(alpha = 0.18f),
                                    border = BorderStroke(0.8.dp, task.tag.color.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = task.tag.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = task.tag.color,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.dueLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FluxoraMoonlightBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FluxoraDeepOcean,
                border = BorderStroke(1.dp, FluxoraCosmicViolet.copy(alpha = 0.4f)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "New Task",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text("Task description...", color = FluxoraMoonlightBlue) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = FluxoraCosmicViolet,
                            unfocusedBorderColor = FluxoraMoonlightBlue.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showAddDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text("Cancel", color = FluxoraMoonlightBlue)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    tasksList.add(
                                        0,
                                        ProductivityTask(
                                            id = "task_${System.currentTimeMillis()}",
                                            title = newTaskTitle.trim(),
                                            tag = ProductivityTaskTag.BUILD,
                                            isCompleted = false,
                                            dueLabel = "Today"
                                        )
                                    )
                                    newTaskTitle = ""
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FluxoraCosmicViolet)
                        ) {
                            Text("Add Task", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. FULL PROJECTS SCREEN (Matching reference Projects view)
// ============================================================================

@Composable
fun FluxoraProjectsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projects = remember {
        listOf(
            ProductivityProject("p1", "Fluxora App", "In Progress • 68%", 68),
            ProductivityProject("p2", "Soniva", "In Progress • 42%", 42),
            ProductivityProject("p3", "Campus Notes", "Completed • 100%", 100),
            ProductivityProject("p4", "Cloud Sync Engine", "In Progress • 85%", 85)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FluxoraMidnightAbyss)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("fluxora_projects_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = t("back"),
                        tint = FluxoraDreamLavender
                    )
                }
                Text(
                    text = t("projects"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Surface(
                shape = CircleShape,
                color = FluxoraCosmicViolet,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = t("add_project"),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(projects, key = { it.id }) { project ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = FluxoraDeepOcean.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = project.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            if (project.progressPercent == 100) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = FluxoraAuroraMint,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Details",
                                    tint = FluxoraMoonlightBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = project.status,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (project.progressPercent == 100) FluxoraAuroraMint else FluxoraStormTeal,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { project.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (project.progressPercent == 100) FluxoraAuroraMint else FluxoraCosmicViolet,
                            trackColor = FluxoraMidnightAbyss.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5. FULL CALENDAR SCREEN (Matching reference Calendar view)
// ============================================================================

@Composable
fun FluxoraCalendarScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableIntStateOf(24) }
    val daysInMonth = 30
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FluxoraMidnightAbyss)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("fluxora_calendar_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = t("back"),
                        tint = FluxoraDreamLavender
                    )
                }
                Text(
                    text = t("calendar"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = t("calendar"),
                tint = FluxoraMoonlightBlue
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FluxoraDeepOcean.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.18f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "‹",
                        fontSize = 24.sp,
                        color = FluxoraMoonlightBlue,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = "November 2026",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "›",
                        fontSize = 24.sp,
                        color = FluxoraMoonlightBlue,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Days of week header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = FluxoraMoonlightBlue,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days Grid (Starting at Sunday offset 0 for demonstration)
                val allDays = (1..daysInMonth).toList()
                val chunkedWeeks = allDays.chunked(7)

                chunkedWeeks.forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        week.forEach { day ->
                            val isSelected = day == selectedDay
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) FluxoraCosmicViolet else Color.Transparent
                                    )
                                    .clickable { selectedDay = day }
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else FluxoraDreamLavender
                                )
                            }
                        }
                        // Fill extra cells in incomplete week
                        if (week.size < 7) {
                            repeat(7 - week.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Selected Day Scheduled Event Card (Matching "DSA Revision" card in reference)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FluxoraDeepOcean.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, FluxoraCosmicViolet.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(FluxoraCosmicViolet)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (selectedDay == 24) "DSA Revision" else "Scheduled Focus Block #$selectedDay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "9:00 AM – 11:00 AM",
                        style = MaterialTheme.typography.bodySmall,
                        color = FluxoraMoonlightBlue
                    )
                }
            }
        }
    }
}

// ============================================================================
// 6. FULL ANALYTICS SCREEN (Matching reference Analytics view)
// ============================================================================

@Composable
fun FluxoraAnalyticsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf("Week") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FluxoraMidnightAbyss)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("fluxora_analytics_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = FluxoraDreamLavender
                    )
                }
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = FluxoraDeepOcean.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    listOf("Week", "Month").forEach { p ->
                        val isSel = selectedPeriod == p
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSel) FluxoraCosmicViolet else Color.Transparent)
                                .clickable { selectedPeriod = p }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = p,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else FluxoraMoonlightBlue
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Recharts-inspired smooth line chart container
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FluxoraDeepOcean.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.18f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                val w = size.width
                val h = size.height

                val points = listOf(
                    Offset(0f, h * 0.75f),
                    Offset(w * 0.16f, h * 0.60f),
                    Offset(w * 0.33f, h * 0.70f),
                    Offset(w * 0.50f, h * 0.35f),
                    Offset(w * 0.66f, h * 0.50f),
                    Offset(w * 0.83f, h * 0.25f),
                    Offset(w, h * 0.30f)
                )

                // Fill area gradient
                val fillPath = Path().apply {
                    moveTo(0f, h)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(w, h)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            FluxoraCosmicViolet.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )

                // Stroke line
                val strokePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(
                    path = strokePath,
                    color = FluxoraCosmicViolet,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Points dots
                points.forEach { pt ->
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = FluxoraCosmicViolet,
                        radius = 2.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3 Summary Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AnalyticsSummaryItem(
                icon = Icons.Default.Timer,
                title = "Focus Time",
                value = "32h 14m",
                delta = "+12%"
            )
            AnalyticsSummaryItem(
                icon = Icons.Default.CheckBox,
                title = "Tasks Completed",
                value = "48",
                delta = "+18%"
            )
            AnalyticsSummaryItem(
                icon = Icons.Default.TrendingUp,
                title = "Productivity",
                value = "72%",
                delta = "+8%"
            )
        }
    }
}

@Composable
fun AnalyticsSummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    delta: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FluxoraDeepOcean.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, FluxoraMoonlightBlue.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = FluxoraCosmicViolet,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FluxoraMoonlightBlue
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = delta,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = FluxoraAuroraMint
                )
            }
        }
    }
}
