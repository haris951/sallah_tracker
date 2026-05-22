package com.sallahtracker.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sallahtracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BeigeBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            item {
                HistoryHeader()
            }

            item {
                CalendarCard(
                    currentMonth = state.currentMonth,
                    summaryMap = state.daySummaryMap,
                    selectedDate = state.selectedDate,
                    onPreviousMonth = { viewModel.onIntent(HistoryIntent.PreviousMonth) },
                    onNextMonth = { viewModel.onIntent(HistoryIntent.NextMonth) },
                    onDayClick = { viewModel.onIntent(HistoryIntent.OnDaySelected(it)) }
                )
            }

            state.selectedDaySummary?.let { summary ->
                item {
                    DetailSummaryCard(summary)
                }
            }

            item {
                LegendCard()
            }
        }
    }
}

@Composable
fun HistoryHeader() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Prayer History",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Track your prayer consistency",
            fontSize = 16.sp,
            color = TextLight
        )
    }
}

@Composable
fun CalendarCard(
    currentMonth: Calendar,
    summaryMap: Map<Long, DaySummary>,
    selectedDate: Long?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (Long) -> Unit
) {
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous")
                }
                
                Text(
                    text = monthYearFormat.format(currentMonth.time),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = TextLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            CalendarGrid(currentMonth, summaryMap, selectedDate, onDayClick)
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    summaryMap: Map<Long, DaySummary>,
    selectedDate: Long?,
    onDayClick: (Long) -> Unit
) {
    val calendar = currentMonth.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val rows = (daysInMonth + firstDayOfWeek - 2) / 7 + 1
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (column in 1..7) {
                    val dayOfMonth = row * 7 + column - (firstDayOfWeek - 1)
                    if (dayOfMonth in 1..daysInMonth) {
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        val timestamp = calendar.timeInMillis
                        
                        val summary = summaryMap[timestamp]
                        val isSelected = selectedDate == timestamp
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .clickable { onDayClick(timestamp) },
                            contentAlignment = Alignment.Center
                        ) {
                            DayCircle(
                                day = dayOfMonth,
                                summary = summary,
                                isSelected = isSelected
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DayCircle(day: Int, summary: DaySummary?, isSelected: Boolean) {
    val backgroundColor = when {
        summary == null || summary.total == 0 -> Color(0xFFF7F7F7)
        summary.percentage >= 1.0f -> PrimaryGreen
        summary.percentage >= 0.6f -> LightGreen
        summary.percentage >= 0.4f -> BeigeSecondary
        else -> SoftRed
    }
    
    val textColor = if (summary != null && summary.total > 0 && summary.percentage >= 0.6f) Color.White else TextDark

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isSelected) Modifier.border(2.dp, GoldCard, CircleShape) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                lineHeight = 14.sp
            )
            if (summary != null && summary.total > 0) {
                Text(
                    text = summary.fraction,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.9f),
                    lineHeight = 9.sp
                )
            }
        }
    }
}

@Composable
fun DetailSummaryCard(summary: DaySummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BeigeBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = PrimaryGreen)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = summary.dateString,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(value = summary.completed.toString(), label = "Completed", color = SecondaryGreen)
                StatItem(value = summary.missed.toString(), label = "Missed", color = ErrorRed)
                StatItem(value = summary.total.toString(), label = "Total", color = TextDark)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { summary.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = SecondaryGreen,
                trackColor = PendingGrey,
            )
        }
    }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 12.sp, color = TextLight)
    }
}

@Composable
fun LegendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BeigeBackground.copy(alpha = 0.5f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Legend", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LegendItem(color = PrimaryGreen, label = "100% Complete")
                    LegendItem(color = BeigeSecondary, label = "40-59%")
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LegendItem(color = LightGreen, label = "60-99%")
                    LegendItem(color = SoftRed, label = "Below 40%")
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 12.sp, color = TextLight)
    }
}
