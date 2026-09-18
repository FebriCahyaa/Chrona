/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
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

@Composable
fun AlarmScreen(
    alarms: List<AlarmItem>,
    use24HourFormat: Boolean,
    glass: Boolean,
    onBack: () -> Unit,
    onAdd: (AlarmItem) -> Unit,
    onToggle: (AlarmItem, Boolean) -> Unit,
    onDelete: (AlarmItem) -> Unit,
) {
    var showAdd by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        ScreenHeader(
            title = "Alarm",
            subtitle = "Wake up to a better day",
            onBack = onBack,
            actions = {
                IconCircleButton(Icons.Filled.Add, { showAdd = true }, active = true, contentDescription = "Add alarm")
            },
        )
        Spacer(Modifier.height(16.dp))

        if (alarms.isEmpty()) {
            ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
                Column(Modifier.fillMaxWidth().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    GradientIconBox(Icons.Filled.Alarm)
                    Spacer(Modifier.height(12.dp))
                    Text("No alarms yet", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(5.dp))
                    Text("Create a gentle routine that starts your day.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(14.dp))
                    TextButton(onClick = { showAdd = true }) { Text("Create alarm") }
                }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
                        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    alarm.time.format(DateTimeFormatter.ofPattern(if (use24HourFormat) "HH:mm" else "h:mm a", Locale.getDefault())),
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Light,
                                )
                                Text(alarm.label.ifBlank { "Alarm" }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(6.dp))
                                Text(repeatLabel(alarm.repeatDays), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Switch(checked = alarm.enabled, onCheckedChange = { onToggle(alarm, it) })
                            IconCircleButton(
                                icon = Icons.Filled.DeleteOutline,
                                onClick = { onDelete(alarm) },
                                contentDescription = "Delete alarm",
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }

    if (showAdd) AddAlarmDialog(use24HourFormat, { showAdd = false }, onAdd)
}

private fun repeatLabel(days: Set<DayOfWeek>): String = when {
    days.isEmpty() -> "Once"
    days.size == 7 -> "Every day"
    else -> days.sortedBy { it.value }.joinToString("  ") {
        when (it) {
            DayOfWeek.MONDAY -> "Mon"
            DayOfWeek.TUESDAY -> "Tue"
            DayOfWeek.WEDNESDAY -> "Wed"
            DayOfWeek.THURSDAY -> "Thu"
            DayOfWeek.FRIDAY -> "Fri"
            DayOfWeek.SATURDAY -> "Sat"
            DayOfWeek.SUNDAY -> "Sun"
        }
    }
}

@Composable
private fun AddAlarmDialog(
    use24HourFormat: Boolean,
    onDismiss: () -> Unit,
    onAdd: (AlarmItem) -> Unit,
) {
    val picker = rememberTimePickerState(is24Hour = use24HourFormat)
    var label by rememberSaveable { mutableStateOf("") }
    var days by androidx.compose.runtime.remember { mutableStateOf(setOf<DayOfWeek>()) }
    val labels = listOf(
        DayOfWeek.MONDAY to "Mon",
        DayOfWeek.TUESDAY to "Tue",
        DayOfWeek.WEDNESDAY to "Wed",
        DayOfWeek.THURSDAY to "Thu",
        DayOfWeek.FRIDAY to "Fri",
        DayOfWeek.SATURDAY to "Sat",
        DayOfWeek.SUNDAY to "Sun",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                TimePicker(state = picker)
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text("Repeat", style = MaterialTheme.typography.labelLarge)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    labels.forEach { (day, short) ->
                        FilterChip(
                            selected = day in days,
                            onClick = { days = if (day in days) days - day else days + day },
                            label = { Text(short, fontSize = 12.sp) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAdd(
                    AlarmItem(
                        System.currentTimeMillis(),
                        LocalTime.of(picker.hour, picker.minute),
                        label,
                        true,
                        days,
                    ),
                )
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
