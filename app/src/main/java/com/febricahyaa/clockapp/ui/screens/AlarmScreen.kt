package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.GradientIconBox
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import java.time.DayOfWeek
import java.time.LocalTime

@Composable
fun AlarmScreen(alarms: List<AlarmItem>, glass: Boolean, onBack: () -> Unit, onAdd: (AlarmItem) -> Unit, onToggle: (AlarmItem, Boolean) -> Unit, onDelete: (AlarmItem) -> Unit) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 12.dp)) {
        ScreenHeader("Alarms", "A softer way to start", actions = { IconCircleButton(Icons.Filled.Add, { showAdd = true }, active = true, contentDescription = "Add alarm") })
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            if (alarms.isEmpty()) item { EmptyAlarmCard(glass) }
            items(alarms.sortedBy { it.time }, key = { it.id }) { alarm -> AlarmCard(alarm, glass, { onToggle(alarm, it) }, { onDelete(alarm) }) }
        }
    }
    if (showAdd) AddAlarmDialog({ showAdd = false }, onAdd)
}

@Composable private fun EmptyAlarmCard(glass: Boolean) = ChronaCard(Modifier.fillMaxWidth(), glass = glass) { Column(Modifier.fillMaxWidth().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) { GradientIconBox(Icons.Filled.Alarm); Spacer(Modifier.height(14.dp)); Text("No alarms", style = MaterialTheme.typography.titleMedium); Text("Create your first wake up time.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable private fun AlarmCard(alarm: AlarmItem, glass: Boolean, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(alarm.time.toString().take(5), fontSize = 39.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Light); Text(alarm.label.ifBlank { if (alarm.isRepeating) "Repeating alarm" else "One time alarm" }, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(6.dp)); Text(repeatLabel(alarm.repeatDays), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary) }
            Switch(checked = alarm.enabled, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) { Icon(Icons.Filled.DeleteOutline, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

private fun repeatLabel(days: Set<DayOfWeek>): String = when { days.isEmpty() -> "Once"; days.size == 7 -> "Every day"; else -> days.sortedBy { it.value }.joinToString("  ") { it.name.take(2).replaceFirstChar(Char::uppercase) } }

@Composable private fun AddAlarmDialog(onDismiss: () -> Unit, onAdd: (AlarmItem) -> Unit) {
    val picker = rememberTimePickerState(is24Hour = true)
    var label by rememberSaveable { mutableStateOf("") }
    var days by remember { mutableStateOf(setOf<DayOfWeek>()) }
    val labels = DayOfWeek.entries
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New alarm") }, text = { Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { TimePicker(state = picker); OutlinedTextField(value = label, onValueChange = { label = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Label") }); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) { labels.forEach { day -> FilterChip(selected = day in days, onClick = { days = if (day in days) days - day else days + day }, label = { Text(day.name.take(1)) }) } } } }, confirmButton = { TextButton(onClick = { onAdd(AlarmItem(System.currentTimeMillis(), LocalTime.of(picker.hour, picker.minute), label, true, days)); onDismiss() }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
