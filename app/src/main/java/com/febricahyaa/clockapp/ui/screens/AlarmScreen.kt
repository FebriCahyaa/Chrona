/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.febricahyaa.clockapp.ui.screens

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.GradientIconBox
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import com.febricahyaa.clockapp.ui.theme.ClockMotion
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val ALARM_DAYS = listOf(
    DayOfWeek.MONDAY to "Mon",
    DayOfWeek.TUESDAY to "Tue",
    DayOfWeek.WEDNESDAY to "Wed",
    DayOfWeek.THURSDAY to "Thu",
    DayOfWeek.FRIDAY to "Fri",
    DayOfWeek.SATURDAY to "Sat",
    DayOfWeek.SUNDAY to "Sun",
)

@Composable
fun AlarmScreen(
    alarms: List<AlarmItem>,
    use24HourFormat: Boolean,
    glass: Boolean,
    onBack: () -> Unit,
    onAdd: (AlarmItem) -> Unit,
    onToggle: (AlarmItem, Boolean) -> Unit,
    onDelete: (AlarmItem) -> Unit,
    onUpdate: (AlarmItem) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var showAdd by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        ScreenHeader(
            title = "Alarm",
            subtitle = "Schedules that stay editable without leaving the list",
            onBack = onBack,
            actions = {
                IconCircleButton(
                    icon = Icons.Filled.Add,
                    onClick = { showAdd = true },
                    active = true,
                    contentDescription = "Add alarm",
                )
            },
        )
        Spacer(Modifier.height(16.dp))

        if (alarms.isEmpty()) {
            ChronaCard(
                Modifier.widthIn(max = 560.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
                glass = glass,
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    GradientIconBox(Icons.Filled.Alarm)
                    Spacer(Modifier.height(12.dp))
                    Text("No alarms yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Create a schedule and tune repeat, sound and vibration from the same card.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(14.dp))
                    TextButton(onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        showAdd = true
                    }) { Text("Create alarm", style = MaterialTheme.typography.labelLarge) }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(alarms, key = { it.id }, contentType = { "alarm" }) { alarm ->
                    ExpandableAlarmCard(
                        alarm = alarm,
                        use24HourFormat = use24HourFormat,
                        glass = glass,
                        onToggle = { enabled ->
                            haptics.performHapticFeedback(
                                if (enabled) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
                            )
                            onToggle(alarm, enabled)
                        },
                        onDelete = {
                            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onDelete(alarm)
                        },
                        onUpdate = onUpdate,
                    )
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }

    if (showAdd) {
        AddAlarmDialog(
            use24HourFormat = use24HourFormat,
            onDismiss = { showAdd = false },
            onAdd = onAdd,
        )
    }
}

@Composable
private fun ExpandableAlarmCard(
    alarm: AlarmItem,
    use24HourFormat: Boolean,
    glass: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onUpdate: (AlarmItem) -> Unit,
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val locale = LocalLocale.current.platformLocale
    var expanded by rememberSaveable(alarm.id) { mutableStateOf(false) }

    val timeFormatter = androidx.compose.runtime.remember(use24HourFormat, locale) {
        DateTimeFormatter.ofPattern(if (use24HourFormat) "HH:mm" else "h:mm", locale)
    }
    val timeText = alarm.time.format(timeFormatter)
    val period = if (use24HourFormat) null else if (alarm.time.hour < 12) "AM" else "PM"

    val ringtonePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val pickedUri = result.data?.let { data ->
            androidx.core.content.IntentCompat.getParcelableExtra(
                data,
                RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                Uri::class.java,
            )
        }
        val name = pickedUri?.let { RingtoneManager.getRingtone(context, it)?.getTitle(context) }
            ?: "Silent"
        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
        onUpdate(
            alarm.copy(
                ringtoneUri = pickedUri?.toString(),
                ringtoneName = name,
            ),
        )
    }

    fun openRingtonePicker() {
        val existingUri = alarm.ringtoneUri?.let(Uri::parse)
            ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Alarm sound")
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
        }
        ringtonePicker.launch(intent)
    }

    ChronaCard(
        modifier = Modifier
            .animateContentSize(animationSpec = ClockMotion.alarmExpand)
            .fillMaxWidth(),
        onClick = {
            expanded = !expanded
        },
        glass = glass,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Light,
                        )
                        if (period != null) {
                            Spacer(Modifier.size(7.dp))
                            Text(
                                text = period,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = alarm.label.ifBlank { repeatLabel(alarm.repeatDays) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Switch(checked = alarm.enabled, onCheckedChange = onToggle)
                Spacer(Modifier.size(6.dp))
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse alarm options" else "Expand alarm options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (expanded) {
                Spacer(Modifier.height(14.dp))
                Text("Repeat", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ALARM_DAYS.forEach { (day, short) ->
                        FilterChip(
                            selected = day in alarm.repeatDays,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                val updatedDays = if (day in alarm.repeatDays) {
                                    alarm.repeatDays - day
                                } else {
                                    alarm.repeatDays + day
                                }
                                onUpdate(alarm.copy(repeatDays = updatedDays))
                            },
                            label = { Text(short) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                AlarmOptionRow(
                    icon = Icons.Filled.MusicNote,
                    title = "Alarm sound",
                    value = alarm.ringtoneName,
                    onClick = ::openRingtonePicker,
                )
                AlarmOptionRow(
                    icon = Icons.Filled.Vibration,
                    title = "Vibrate",
                    value = if (alarm.vibrate) "On" else "Off",
                    trailing = {
                        Switch(
                            checked = alarm.vibrate,
                            onCheckedChange = { enabled ->
                                haptics.performHapticFeedback(
                                    if (enabled) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
                                )
                                onUpdate(alarm.copy(vibrate = enabled))
                            },
                        )
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete alarm")
                    }
                }
            }
        }
    }
}

@Composable
private fun AlarmOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke()
        if (onClick != null) {
            TextButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onClick()
            }) { Text("Change", style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
private fun AddAlarmDialog(
    use24HourFormat: Boolean,
    onDismiss: () -> Unit,
    onAdd: (AlarmItem) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val picker = rememberTimePickerState(is24Hour = use24HourFormat)
    var label by rememberSaveable { mutableStateOf("") }
    var days by rememberSaveable { mutableStateOf(setOf<DayOfWeek>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add alarm", style = MaterialTheme.typography.headlineSmall) },
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
                    ALARM_DAYS.forEach { (day, short) ->
                        FilterChip(
                            selected = day in days,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                days = if (day in days) days - day else days + day
                            },
                            label = { Text(short) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onAdd(
                    AlarmItem(
                        id = System.currentTimeMillis(),
                        time = LocalTime.of(picker.hour, picker.minute),
                        label = label,
                        enabled = true,
                        repeatDays = days,
                    ),
                )
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onDismiss()
            }) { Text("Cancel") }
        },
    )
}

private fun repeatLabel(days: Set<DayOfWeek>): String = when {
    days.isEmpty() -> "Once"
    days.size == 7 -> "Every day"
    else -> days.sortedBy { it.value }.joinToString("  ") { day ->
        ALARM_DAYS.first { it.first == day }.second
    }
}
