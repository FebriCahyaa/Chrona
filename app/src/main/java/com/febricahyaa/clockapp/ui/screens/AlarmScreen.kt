package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.GradientIconBox
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(alarms: List<AlarmItem>, glass: Boolean, onBack: () -> Unit, onAdd: (AlarmItem) -> Unit, onToggle: (AlarmItem, Boolean) -> Unit, onDelete: (AlarmItem) -> Unit) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(10.dp))
        ScreenHeader("Alarm", "Wake up to a better day", actions = { IconCircleButton(Icons.Filled.ArrowBack, onBack, contentDescription = "Back"); IconCircleButton(Icons.Filled.Add, { showAdd = true }, active = true, contentDescription = "Add alarm") })
        Spacer(Modifier.height(14.dp))
        if (alarms.isEmpty()) {
            ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
                Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    GradientIconBox(Icons.Filled.Alarm)
                    Spacer(Modifier.height(12.dp))
                    Text("No alarms yet", fontWeight = FontWeight.SemiBold)
                    Text("Create a gentle routine that starts your day.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(alarms, key = { it.id }) { alarm ->
                    ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
                        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(alarm.time.format(DateTimeFormatter.ofPattern("HH:mm")), fontSize = 31.sp, fontWeight = FontWeight.Light)
                                Text(alarm.label.ifBlank { "Alarm" }, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(5.dp))
                                Text(repeatLabel(alarm.repeatDays), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Switch(checked = alarm.enabled, onCheckedChange = { onToggle(alarm, it) })
                            IconButton(onClick = { onDelete(alarm) }) { Text("×", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }
        }
    }
    if (showAdd) AddAlarmDialog({ showAdd = false }, onAdd)
}

private fun repeatLabel(days: Set<DayOfWeek>): String {
    if (days.isEmpty()) return "Once"
    if (days.size == 7) return "Every day"
    return days.sortedBy { it.value }.joinToString("  ") { it.name.take(1) }
}

@Composable
private fun AddAlarmDialog(onDismiss: () -> Unit, onAdd: (AlarmItem) -> Unit) {
    val picker = rememberTimePickerState(is24Hour = true)
    var label by rememberSaveable { mutableStateOf("") }
    var days by androidx.compose.runtime.remember { mutableStateOf(setOf<DayOfWeek>()) }
    val labels = listOf(
        DayOfWeek.MONDAY to "M", DayOfWeek.TUESDAY to "T", DayOfWeek.WEDNESDAY to "W", DayOfWeek.THURSDAY to "T",
        DayOfWeek.FRIDAY to "F", DayOfWeek.SATURDAY to "S", DayOfWeek.SUNDAY to "S"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                TimePicker(state = picker)
                androidx.compose.material3.OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    labels.forEach { (day, short) -> FilterChip(selected = day in days, onClick = { days = if (day in days) days - day else days + day }, label = { Text(short) }) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(AlarmItem(System.currentTimeMillis(), LocalTime.of(picker.hour, picker.minute), label, true, days)); onDismiss() }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
