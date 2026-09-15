package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.model.AlarmItem
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * Standard alarm list screen: shows all user alarms sorted by time, lets the
 * user toggle, delete, and add new alarms through a time-picker dialog.
 */
@Composable
fun AlarmScreen(
    alarms: List<AlarmItem>,
    use24HourFormat: Boolean,
    onAdd: (AlarmItem) -> Unit,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.alarm_screen_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    pluralAlarmsSubtitle(alarms.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (alarms.isEmpty()) {
                EmptyAlarmsState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(alarms.sortedBy { it.time }, key = { it.id }) { alarm ->
                        AlarmRow(
                            alarm = alarm,
                            use24HourFormat = use24HourFormat,
                            onToggle = { checked -> onToggle(alarm.id, checked) },
                            onDelete = { onDelete(alarm.id) }
                        )
                    }
                    item { Spacer(Modifier.size(88.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.alarm_add_action))
        }
    }

    if (showAddDialog) {
        AddAlarmDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { alarm ->
                onAdd(alarm)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun pluralAlarmsSubtitle(count: Int): String = if (count == 0) {
    stringResource(R.string.alarm_screen_subtitle_empty)
} else {
    stringResource(R.string.alarm_screen_subtitle_count, count)
}

@Composable
private fun EmptyAlarmsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AlarmOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            stringResource(R.string.alarm_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            stringResource(R.string.alarm_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AlarmRow(
    alarm: AlarmItem,
    use24HourFormat: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val displayed = if (use24HourFormat) {
        alarm.time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    } else {
        alarm.time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
    }
    val alpha = if (alarm.enabled) 1f else 0.45f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayed,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                val subtitle = if (alarm.label.isNotBlank()) alarm.label else repeatSummary(alarm.repeatDays)
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                    )
                }
            }
            Switch(checked = alarm.enabled, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.alarm_delete_action),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun repeatSummary(days: Set<DayOfWeek>): String {
    if (days.isEmpty()) return ""
    if (days.size == 7) return "Every day"
    val ordered = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )
    return ordered.filter { it in days }
        .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onConfirm: (AlarmItem) -> Unit
) {
    val now = LocalTime.now()
    val pickerState = rememberTimePickerState(
        initialHour = now.hour,
        initialMinute = now.minute,
        is24Hour = true
    )
    var label by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<DayOfWeek>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.alarm_dialog_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = pickerState)
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.alarm_dialog_label_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
                    ).forEach { day ->
                        val selected = day in selectedDays
                        DayChip(
                            label = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                            selected = selected,
                            onClick = {
                                selectedDays = if (selected) selectedDays - day else selectedDays + day
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    AlarmItem(
                        id = System.currentTimeMillis(),
                        time = LocalTime.of(pickerState.hour, pickerState.minute),
                        label = label.trim(),
                        enabled = true,
                        repeatDays = selectedDays
                    )
                )
            }) {
                Text(stringResource(R.string.alarm_dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.alarm_dialog_cancel)) }
        }
    )
}

@Composable
private fun DayChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
