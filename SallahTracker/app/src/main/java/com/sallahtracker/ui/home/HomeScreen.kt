package com.sallahtracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sallahtracker.data.local.entity.SalahRecord
import com.sallahtracker.data.model.SalahStatus
import com.sallahtracker.ui.theme.*

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BeigeBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            item {
                HeaderSection(state.date)
            }

            item {
                ProgressCard(state.completedCount, state.totalCount)
            }

            items(state.prayers) { prayer ->
                SalahCard(
                    prayer = prayer,
                    onStatusChange = { status ->
                        viewModel.onIntent(HomeIntent.UpdateSalahStatus(prayer, status))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                ActionButtons(
                    onMarkAll = { viewModel.onIntent(HomeIntent.MarkAllCompleted) },
                    onSave = { viewModel.onIntent(HomeIntent.SaveDay) }
                )
            }
        }
    }
}

@Composable
fun HeaderSection(date: String) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Salah Tracker",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = date,
            fontSize = 16.sp,
            color = TextLight
        )
    }
}

@Composable
fun ProgressCard(completed: Int, total: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Today's Progress",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$completed/$total",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Prayers Completed",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { if (total > 0) completed.toFloat() / total else 0f },
                    modifier = Modifier.size(80.dp),
                    color = Color.White,
                    strokeWidth = 8.dp,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
fun SalahCard(
    prayer: SalahRecord,
    onStatusChange: (SalahStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BeigeBackground.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🤲", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = prayer.type.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = prayer.time,
                        fontSize = 14.sp,
                        color = TextLight
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { onStatusChange(SalahStatus.COMPLETED) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (prayer.status == SalahStatus.COMPLETED) SecondaryGreen else PendingGrey)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = if (prayer.status == SalahStatus.COMPLETED) Color.White else TextLight
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onStatusChange(SalahStatus.MISSED) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (prayer.status == SalahStatus.MISSED) ErrorRed.copy(alpha = 0.1f) else PendingGrey)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Missed",
                        tint = if (prayer.status == SalahStatus.MISSED) ErrorRed else TextLight
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtons(onMarkAll: () -> Unit, onSave: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onMarkAll,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BeigeSecondary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Mark All as Completed", color = TextDark, fontWeight = FontWeight.Medium)
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Save Day", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
