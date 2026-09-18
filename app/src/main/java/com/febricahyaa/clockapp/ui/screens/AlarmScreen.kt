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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.navigation.ChronaMotionKeys
import com.febricahyaa.clockapp.navigation.chronaSharedBounds
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.ChronaScaffold
import com.febricahyaa.clockapp.ui.components.GradientIconBox
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.theme.ClockMotion
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
@Suppress("UNUSED_PARAMETER")
fun AlarmScreen(
    alarms: List<AlarmItem>,
    use24HourFormat: Boolean,
    glass: Boolean,
    onBack: () -> Unit,
    onAdd: (AlarmItem) -> Unit,
    onToggle: (AlarmItem, Boolean) -> Unit,
    onDelete: (AlarmItem) -> Unit,
    onUpdate: (AlarmItem) -> Unit,
    onEdit: () -> Unit = {},
) {
    val haptics = LocalHapticFeedback.current
    var editorAlarmId by rememberSaveable { mutableStateOf<Long?>(null) }

    ChronaScaffold(
        title = stringResource(R.string.alarm_screen_title),
        subtitle = stringResource(R.string.alarm_screen_subtitle),
        onBack = onBack,
        actions = {
            IconCircleButton(
                icon = Icons.Filled.Add,
                onClick = { editorAlarmId = NEW_ALARM_ID },
                active = true,
                contentDescription = stringResource(R.string.alarm_add_action),
            )
        },
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .chronaSharedBounds(ChronaMotionKeys.DASHBOARD_ALARM),
        ) {
            Spacer(Modifier.height(4.dp))

            if (alarms.isEmpty()) {
                ChronaCard(
                    Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    glass = glass,
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        GradientIconBox(Icons.Filled.Alarm)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.alarm_empty_title), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            stringResource(R.string.alarm_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(14.dp))
                        TextButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                editorAlarmId = NEW_ALARM_ID
                            },
                        ) {
                            Text(stringResource(R.string.alarm_create_action), style = MaterialTheme.typography.labelLarge)
                        }
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
                            onEdit = { editorAlarmId = alarm.id },
                        )
                    }
                    item { Spacer(Modifier.height(18.dp)) }
                }
            }
        }
    }

    val editorId = editorAlarmId
    val editorAlarm = when {
        editorId == null || editorId == NEW_ALARM_ID -> null
        else -> alarms.firstOrNull { alarm -> alarm.id == editorId }
    }

    if (editorId != null) {
        key(editorId) {
            AlarmEditorSheet(
                alarm = editorAlarm,
                use24HourFormat = use24HourFormat,
                glass = glass,
                onDismiss = { editorAlarmId = null },
                onAdd = { alarm ->
                    onAdd(alarm)
                    editorAlarmId = null
                },
                onUpdate = { alarm ->
                    onUpdate(alarm)
                    editorAlarmId = null
                },
            )
        }
    }
}

private const val NEW_ALARM_ID = -1L

@Composable
private fun ExpandableAlarmCard(
    alarm: AlarmItem,
    use24HourFormat: Boolean,
    glass: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onUpdate: (AlarmItem) -> Unit,
    onEdit: () -> Unit,
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val locale = LocalLocale.current.platformLocale
    val silentRingtoneLabel = stringResource(R.string.alarm_ringtone_silent)
    var expanded by rememberSaveable(alarm.id) { mutableStateOf(false) }

    val timeFormatter = remember(use24HourFormat, locale) {
        DateTimeFormatter.ofPattern(if (use24HourFormat) "HH:mm" else "h:mm", locale)
    }
    val timeText = alarm.time.format(timeFormatter)
    val period = if (use24HourFormat) null else stringResource(if (alarm.time.hour < 12) R.string.time_am else R.string.time_pm)

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
            ?: silentRingtoneLabel
        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
        onUpdate(
            alarm.copy(
                ringtoneUri = pickedUri?.toString(),
                ringtoneName = name,
            ),
        )
    }

    fun openRingtonePicker() {
        val existingUri = alarm.ringtoneUri?.let { value -> Uri.parse(value) }
            ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        ringtonePicker.launch(
            Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.alarm_ringtone_picker_title))
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
            },
        )
    }

    ChronaCard(
        modifier = Modifier
            .animateContentSize(animationSpec = ClockMotion.alarmExpand)
            .fillMaxWidth(),
        onClick = { expanded = !expanded },
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
                        text = if (alarm.label.isBlank()) {
                            repeatLabel(
                                days = alarm.repeatDays,
                                locale = locale,
                                onceLabel = stringResource(R.string.alarm_repeat_once),
                                everyDayLabel = stringResource(R.string.alarm_repeat_every_day),
                            )
                        } else {
                            alarm.label
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Switch(checked = alarm.enabled, onCheckedChange = onToggle)
                Spacer(Modifier.size(6.dp))
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) stringResource(R.string.alarm_collapse) else stringResource(R.string.alarm_expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (expanded) {
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.alarm_repeat), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    DayOfWeek.values().forEach { day ->
                        val short = day.getDisplayName(TextStyle.SHORT, locale)
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
                    title = stringResource(R.string.alarm_sound),
                    value = alarm.ringtoneName.ifBlank { stringResource(R.string.alarm_default_ringtone) },
                    onClick = { openRingtonePicker() },
                )
                AlarmOptionRow(
                    icon = Icons.Filled.Vibration,
                    title = stringResource(R.string.alarm_vibrate),
                    value = if (alarm.vibrate) stringResource(R.string.alarm_value_on) else stringResource(R.string.alarm_value_off),
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
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.alarm_edit_action))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.alarm_delete_action))
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
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing?.invoke()
        if (onClick != null) {
            TextButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onClick()
            }) {
                Text(stringResource(R.string.alarm_change), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun AlarmEditorSheet(
    alarm: AlarmItem?,
    use24HourFormat: Boolean,
    glass: Boolean,
    onDismiss: () -> Unit,
    onAdd: (AlarmItem) -> Unit,
    onUpdate: (AlarmItem) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val locale = LocalLocale.current.platformLocale
    val isEditing = alarm != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initialAlarm = remember(alarm?.id) {
        alarm ?: AlarmItem(
            id = 0L,
            time = LocalTime.now(),
            label = "",
            enabled = true,
            repeatDays = emptySet(),
        )
    }
    val picker = rememberTimePickerState(
        initialHour = initialAlarm.time.hour,
        initialMinute = initialAlarm.time.minute,
        is24Hour = use24HourFormat,
    )
    val silentRingtoneLabel = stringResource(R.string.alarm_ringtone_silent)
    var label by rememberSaveable(initialAlarm.id) { mutableStateOf(initialAlarm.label) }
    var days by remember(initialAlarm.id) { mutableStateOf(initialAlarm.repeatDays) }
    var vibrate by rememberSaveable(initialAlarm.id) { mutableStateOf(initialAlarm.vibrate) }
    var enabled by rememberSaveable(initialAlarm.id) { mutableStateOf(initialAlarm.enabled) }
    var ringtoneUri by rememberSaveable(initialAlarm.id) { mutableStateOf(initialAlarm.ringtoneUri) }
    var ringtoneName by rememberSaveable(initialAlarm.id) { mutableStateOf(initialAlarm.ringtoneName) }

    val timeFormatter = remember(use24HourFormat, locale) {
        DateTimeFormatter.ofPattern(if (use24HourFormat) "HH:mm" else "h:mm", locale)
    }

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
        ringtoneUri = pickedUri?.toString()
        ringtoneName = pickedUri?.let { RingtoneManager.getRingtone(context, it)?.getTitle(context) }
            ?: silentRingtoneLabel
        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }

    fun openRingtonePicker() {
        val existingUri = ringtoneUri?.let { value -> Uri.parse(value) }
            ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        ringtonePicker.launch(
            Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.alarm_ringtone_picker_title))
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
            },
        )
    }

    val selectedTime = LocalTime.of(picker.hour, picker.minute)
    val selectedTimeText = selectedTime.format(timeFormatter)
    val selectedPeriod = if (use24HourFormat) null else stringResource(if (selectedTime.hour < 12) R.string.time_am else R.string.time_pm)
    val repeatSummary = repeatLabel(days, locale, stringResource(R.string.alarm_repeat_once), stringResource(R.string.alarm_repeat_every_day))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .imePadding()
                .navigationBarsPadding()
                .widthIn(max = 720.dp)
                .align(Alignment.CenterHorizontally)
                .chronaSharedBounds(ChronaMotionKeys.ALARM_EDITOR),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Text(
                    text = if (isEditing) stringResource(R.string.alarm_editor_edit_title) else stringResource(R.string.alarm_editor_new_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isEditing) {
                        stringResource(R.string.alarm_editor_edit_subtitle)
                    } else {
                        stringResource(R.string.alarm_editor_new_subtitle)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 18.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    ChronaCard(glass = glass) {
                        Column(
                            Modifier.fillMaxWidth().padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    selectedTimeText,
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Light,
                                )
                                if (selectedPeriod != null) {
                                    Spacer(Modifier.size(8.dp))
                                    Text(
                                        selectedPeriod,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.alarm_time_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(12.dp))
                            TimePicker(state = picker)
                        }
                    }
                }

                item {
                    Text(stringResource(R.string.alarm_editor_schedule_section), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                item {
                    ChronaCard(glass = glass) {
                        Column(Modifier.fillMaxWidth().padding(18.dp)) {
                            OutlinedTextField(
                                value = label,
                                onValueChange = { value: String -> label = value },
                                label = { Text(text = stringResource(R.string.alarm_label)) },
                                placeholder = { Text(text = stringResource(R.string.alarm_label_hint)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(R.string.alarm_repeat), style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                            ) {
                                DayOfWeek.values().forEach { day ->
                        val short = day.getDisplayName(TextStyle.SHORT, locale)
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
                            Spacer(Modifier.height(8.dp))
                            Text(
                                repeatSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                item {
                    Text(stringResource(R.string.alarm_editor_alert_section), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                item {
                    ChronaCard(glass = glass) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp)) {
                            AlarmOptionRow(
                                icon = Icons.Filled.MusicNote,
                                title = stringResource(R.string.alarm_sound),
                                value = if (ringtoneName.isBlank()) {
                                    stringResource(R.string.alarm_default_ringtone)
                                } else {
                                    ringtoneName
                                },
                                onClick = { openRingtonePicker() },
                            )
                            AlarmOptionRow(
                                icon = Icons.Filled.Vibration,
                                title = stringResource(R.string.alarm_vibrate),
                                value = if (vibrate) stringResource(R.string.alarm_value_on) else stringResource(R.string.alarm_value_off),
                                trailing = {
                                    Switch(
                                        checked = vibrate,
                                        onCheckedChange = { next ->
                                            vibrate = next
                                            haptics.performHapticFeedback(
                                                if (next) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
                                            )
                                        },
                                    )
                                },
                            )
                            AlarmOptionRow(
                                icon = Icons.Filled.Alarm,
                                title = stringResource(R.string.alarm_enabled),
                                value = if (enabled) stringResource(R.string.alarm_status_active) else stringResource(R.string.alarm_status_disabled),
                                trailing = {
                                    Switch(
                                        checked = enabled,
                                        onCheckedChange = { next ->
                                            enabled = next
                                            haptics.performHapticFeedback(
                                                if (next) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
                                            )
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.alarm_cancel))
                }
                Button(
                    onClick = {
                        val updated = AlarmItem(
                            id = alarm?.id ?: System.currentTimeMillis(),
                            time = selectedTime,
                            label = label.trim(),
                            enabled = enabled,
                            repeatDays = days,
                            ringtoneUri = ringtoneUri,
                            ringtoneName = ringtoneName,
                            vibrate = vibrate,
                        )
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        if (isEditing) onUpdate(updated) else onAdd(updated)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (isEditing) stringResource(R.string.alarm_save_changes) else stringResource(R.string.alarm_create_action))
                }
            }
        }
    }
}

private fun repeatLabel(
    days: Set<DayOfWeek>,
    locale: java.util.Locale,
    onceLabel: String,
    everyDayLabel: String,
): String = when {
    days.isEmpty() -> onceLabel
    days.size == 7 -> everyDayLabel
    else -> days.sortedBy { it.value }
        .joinToString("  ") { it.getDisplayName(TextStyle.SHORT, locale) }
}
