package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun repeatSummary(days: Set<DayOfWeek>): String = when {
    days.isEmpty() -> "One time"
    days.size == 7 -> "Every day"
    else -> days.sortedBy { it.value }.joinToString(" ") { it.name.take(3) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    alarms: List<AlarmItem>,
    use24HourFormat: Boolean,
    onAdd: (AlarmItem) -> Unit,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    val timePattern = if (use24HourFormat) "HH:mm" else "hh:mm a"

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        ScreenHeader("Alarm", "Wake up to a better day", actions = {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
        })
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)) {
            items(alarms, key = { it.id }) { alarm ->
                ChronaCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(alarm.time.format(DateTimeFormatter.ofPattern(timePattern)),
                                fontSize = 24.sp, fontWeight = FontWeight.SemiBold,
                                color = if (alarm.enabled) MaterialTheme.colorScheme.onSurface else dim)
                            Text(alarm.label.ifBlank { repeatSummary(alarm.repeatDays) },
                                fontSize = 11.sp, color = dim)
                        }
                        Switch(checked = alarm.enabled, onCheckedChange = { onToggle(alarm.id, it) })
                        IconButton(onClick = { onDelete(alarm.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = dim)
                        }
                    }
                }
            }
        }
        Button(onClick = { showAdd = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Text("＋ Add Alarm")
        }
    }

    if (showAdd) {
        val picker = rememberTimePickerState(initialHour = 6, initialMinute = 30)
        var label by remember { mutableStateOf("") }
        var days by remember { mutableStateOf(setOf<DayOfWeek>()) }
        val dayLabels = listOf(
            DayOfWeek.MONDAY to "M", DayOfWeek.TUESDAY to "T", DayOfWeek.WEDNESDAY to "W",
            DayOfWeek.THURSDAY to "T", DayOfWeek.FRIDAY to "F", DayOfWeek.SATURDAY to "S",
            DayOfWeek.SUNDAY to "S"
        )
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add Alarm") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimePicker(state = picker)
                    OutlinedTextField(value = label, onValueChange = { label = it },
                        label = { Text("Label") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        dayLabels.forEach { (d, l) ->
                            FilterChip(selected = d in days,
                                onClick = { days = if (d in days) days - d else days + d },
                                label = { Text(l) })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onAdd(AlarmItem(
                        id = System.currentTimeMillis(),
                        time = LocalTime.of(picker.hour, picker.minute),
                        label = label, enabled = true, repeatDays = days
                    ))
                    showAdd = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}
