package xyz.ashnove.justatimer.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.ashnove.justatimer.R
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import xyz.ashnove.justatimer.ui.theme.JustATimerTheme
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.ashnove.justatimer.viewmodel.TimerState
import xyz.ashnove.justatimer.viewmodel.TimerViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.filled.Palette
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import android.content.Intent

private enum class SwipeDirection { Up, Down }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun TimerScreen(timerViewModel: TimerViewModel = viewModel()) {
    val theme by timerViewModel.theme.collectAsState()

    JustATimerTheme(theme = theme) {
        val time by timerViewModel.time.collectAsState()
        val duration by timerViewModel.duration.collectAsState()
        val timerState by timerViewModel.timerState.collectAsState()
        val history by timerViewModel.history.collectAsState()
        val isTimerRunning = timerState == TimerState.Running
        val listState = rememberLazyListState()
        val context = LocalContext.current

        LaunchedEffect(history) {
            listState.animateScrollToItem(0)
        }

        val backgroundBrush = when (theme) {
            "Rose" -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFFB85450),
                    Color(0xFFC85A6E),
                    Color(0xFFA84A5C)
                )
            )
            "Black" -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2C2C2C),
                    Color(0xFF1A1A1A)
                )
            )
            else -> Brush.verticalGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFF0F0F0)
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { if (duration > 0) time.toFloat() / duration.toFloat() else 0f },
                            modifier = Modifier.size(200.dp),
                            strokeWidth = 8.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                        Row {
                            DraggableTimePart(
                                value = (time / 60).toInt(),
                                onValueChange = { timerViewModel.adjustMinutes(it) },
                                enabled = !isTimerRunning
                            )
                            Text(text = ":", style = MaterialTheme.typography.displayLarge)
                            DraggableTimePart(
                                value = (time % 60).toInt(),
                                onValueChange = { timerViewModel.adjustSeconds(it) },
                                enabled = !isTimerRunning
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    ControlButtons(timerState, timerViewModel)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Previous Planks", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start planking, lazy ass!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            itemsIndexed(history, key = { _, record -> record.second }) { _, record ->
                                val (duration, timestamp) = record
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        when (it) {
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                timerViewModel.deleteHistory(timestamp)
                                                true
                                            }
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                val sendIntent: Intent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, "Just planked for ${formatTime(duration)} minutes, can you?")
                                                    type = "text/plain"
                                                }
                                                val shareIntent = Intent.createChooser(sendIntent, null)
                                                context.startActivity(shareIntent)
                                                false // Do not dismiss the card
                                            }
                                            else -> false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    modifier = Modifier
                                        .animateItemPlacement()
                                        .padding(vertical = 4.dp),
                                    backgroundContent = {
                                        val direction = dismissState.dismissDirection
                                        val color = when (direction) {
                                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        }
                                        val alignment = when (direction) {
                                            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                            else -> Alignment.CenterStart
                                        }
                                        val icon = when (direction) {
                                            SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                            else -> Icons.Default.Share
                                        }

                                        if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                            val alpha = dismissState.progress
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(color.copy(alpha = alpha), shape = RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 24.dp),
                                                contentAlignment = alignment
                                            ) {
                                                Icon(
                                                    icon,
                                                    contentDescription = "Action",
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    HistoryCard(
                                        duration = duration,
                                        timestamp = timestamp,
                                        onCardClick = {
                                            if (!isTimerRunning) timerViewModel.setDuration(duration)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            IconButton(
                onClick = { timerViewModel.toggleTheme() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Change Theme",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ControlButtons(timerState: TimerState, timerViewModel: TimerViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                if (timerState == TimerState.Running) {
                    timerViewModel.pauseTimer()
                } else {
                    timerViewModel.startTimer()
                }
            },
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp)
        ) {
            Icon(
                imageVector = if (timerState == TimerState.Running) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (timerState == TimerState.Running) "Pause" else "Play",
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(modifier = Modifier.width(24.dp))
        Button(
            onClick = { timerViewModel.stopTimer() },
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            enabled = timerState != TimerState.Stopped,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop",
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Composable
private fun HistoryCard(duration: Long, timestamp: Long, onCardClick: () -> Unit) {
    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTimestamp(timestamp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun DraggableTimePart(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean
) {
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    AnimatedContent(
        targetState = value,
        transitionSpec = {
            if (targetState > initialState) { // Value is increasing
                // New number slides in from the bottom, old number slides out to the top
                slideInVertically { height -> height } + fadeIn() with
                        slideOutVertically { height -> -height } + fadeOut()
            } else { // Value is decreasing
                // New number slides in from the top, old number slides out to the bottom
                slideInVertically { height -> -height } + fadeIn() with
                        slideOutVertically { height -> height } + fadeOut()
            }.using(
                SizeTransform(clip = false)
            )
        },
        modifier = modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectVerticalDragGestures(
                    onDragEnd = { accumulatedDrag = 0f }
                ) { change, dragAmount ->
                    change.consume()
                    accumulatedDrag += dragAmount
                    val dragThreshold = 60f

                    if (accumulatedDrag < -dragThreshold) { // Swipe Up INCREASES value
                        onValueChange(1)
                        accumulatedDrag = 0f
                    } else if (accumulatedDrag > dragThreshold) { // Swipe Down DECREASES value
                        onValueChange(-1)
                        accumulatedDrag = 0f
                    }
                }
            }
    ) { targetValue ->
        Text(
            text = "%02d".format(targetValue),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

private fun formatTimestamp(timestamp: Long): String {
    val sdfDate = SimpleDateFormat("d'${getDayOfMonthSuffix(timestamp)}' MMMM", Locale.getDefault())
    val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = java.util.Date(timestamp)
    return "${sdfDate.format(date)}, ${sdfTime.format(date)}"
}

private fun getDayOfMonthSuffix(timestamp: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    val day = cal.get(Calendar.DAY_OF_MONTH)
    if (day in 11..13) {
        return "th"
    }
    return when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

@Preview
@Composable
fun TimerScreenPreview() {
    JustATimerTheme {
        TimerScreen()
    }
} 