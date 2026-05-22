package com.sallahtracker.ui.qaza

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun QazaScreen(viewModel: QazaViewModel) {
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
                QazaHeader()
            }

            item {
                QazaSummaryCard(
                    total = state.totalMissedCount,
                    completed = state.completedMissedCount,
                    onAddClick = { viewModel.onIntent(QazaIntent.AddQazaManual) }
                )
            }

            state.missedPrayersByDate.forEach { (date, records) ->
                item {
                    DateGroupCard(
                        date = date,
                        records = records,
                        onMarkDone = { viewModel.onIntent(QazaIntent.MarkAsDone(it)) },
                        onEdit = { viewModel.onIntent(QazaIntent.EditQaza(it)) },
                        onDelete = { viewModel.onIntent(QazaIntent.DeleteQaza(it.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun QazaHeader() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Qaza Prayers",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Track and complete your missed prayers",
            fontSize = 16.sp,
            color = TextLight
        )
    }
}

@Composable
fun QazaSummaryCard(total: Int, completed: Int, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GoldCard)
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
                    text = "Total Qaza Prayers",
                    color = TextDark.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$total",
                    color = TextDark,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$completed of $total completed",
                    color = TextDark,
                    fontSize = 14.sp
                )
            }

            FloatingActionButton(
                onClick = onAddClick,
                containerColor = GoldLight,
                contentColor = TextDark,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Qaza")
            }
        }
    }
}

@Composable
fun DateGroupCard(
    date: Long,
    records: List<SalahRecord>,
    onMarkDone: (SalahRecord) -> Unit,
    onEdit: (SalahRecord) -> Unit,
    onDelete: (SalahRecord) -> Unit
) {
    val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(date))
    val count = records.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dateString,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "$count prayer${if (count > 1) "s" else ""} missed",
                fontSize = 14.sp,
                color = TextLight
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            records.forEachIndexed { index, record ->
                QazaItemRow(
                    record = record,
                    onMarkDone = { onMarkDone(record) },
                    onEdit = { onEdit(record) },
                    onDelete = { onDelete(record) }
                )
                if (index < records.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun QazaItemRow(
    record: SalahRecord,
    onMarkDone: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = record.status == SalahStatus.COMPLETED
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCompleted) Color(0xFFF1F8F1) else Color.Transparent)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isCompleted) SecondaryGreen else Color.Transparent)
                .border(2.dp, if (isCompleted) SecondaryGreen else Color.LightGray, CircleShape)
                .clickable { onMarkDone() },
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = record.type.displayName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark,
            modifier = Modifier.weight(1f)
        )
        
        IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextLight, modifier = Modifier.size(20.dp))
        }
        
        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextLight, modifier = Modifier.size(20.dp))
        }
    }
}
