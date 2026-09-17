/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.ui.components.BentoIcon
import com.febricahyaa.clockapp.ui.components.BentoIconButton
import com.febricahyaa.clockapp.ui.components.HybridBentoCard
import com.febricahyaa.clockapp.ui.components.ThemeToggle
import com.febricahyaa.clockapp.ui.components.rememberZonedNow
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

private enum class ClockDisplayMode { DIGITAL, ANALOG }

@Composable
fun HomeScreen(
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    alarms: List<AlarmItem>,
    timerRemainingSeconds: Int,
    timerRunning: Boolean,
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onNavigate: (AppDestination) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val now = rememberZonedNow()
    val locale = LocalLocale.current.platformLocale
    var clockDisplayMode by rememberSaveable { mutableStateOf(ClockDisplayMode.DIGITAL) }

    val next = nextAlarm(alarms, now)
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val isWideWindow = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    val dateText = buildDateText(now, locale)
    val alarmTime = next?.time?.let {
        val hour = it.hour % 12
        "${(if (hour == 0) 12 else hour).toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')} ${if (it.hour < 12) "AM" else "PM"}"
    } ?: "Not set"
    val alarmMeta = when {
        next == null -> "Create an alarm"
        next.repeatDays.isEmpty() -> "One time"
        else -> "Repeats"
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val contentMaxWidth = maxWidth

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isWideWindow) 26.dp else 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BentoHeader(themeMode, onThemeModeChange, onOpenSettings)

            if (isWideWindow) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    HybridBentoCard(
                        modifier = Modifier
                            .weight(1.32f)
                            .height(430.dp),
                        themeMode = themeMode,
                    ) {
                        ClockHero(
                            now = now,
                            dateText = dateText,
                            displayMode = clockDisplayMode,
                            use24HourFormat = use24HourFormat,
                            showSeconds = showSeconds,
                            onToggleDisplay = {
                                clockDisplayMode = if (clockDisplayMode == ClockDisplayMode.DIGITAL) ClockDisplayMode.ANALOG else ClockDisplayMode.DIGITAL
                            },
                        )
                    }

                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            TimeActionCard(
                                modifier = Modifier.weight(1f),
                                themeMode = themeMode,
                                icon = Icons.Filled.AccessAlarm,
                                eyebrow = if (next == null) "ALARM" else "NEXT ALARM",
                                title = "Alarm",
                                value = alarmTime,
                                meta = alarmMeta,
                                onClick = { onNavigate(AppDestination.ALARM) },
                            )
                            TimeActionCard(
                                modifier = Modifier.weight(1f),
                                themeMode = themeMode,
                                icon = Icons.Filled.Timer,
                                eyebrow = if (timerRunning) "LIVE" else "TIMER",
                                title = "Timer",
                                value = formatBentoTimer(timerRemainingSeconds),
                                meta = if (timerRunning) "Counting down" else "Ready when you are",
                                onClick = { onNavigate(AppDestination.TIMER) },
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            UtilityCard(
                                modifier = Modifier.weight(1f),
                                themeMode = themeMode,
                                icon = Icons.Filled.Public,
                                title = "World Clock",
                                subtitle = "Cities & time zones",
                                onClick = { onNavigate(AppDestination.WORLD) },
                            )
                            UtilityCard(
                                modifier = Modifier.weight(1f),
                                themeMode = themeMode,
                                icon = Icons.Filled.AccessTime,
                                title = "Stopwatch",
                                subtitle = "Precise elapsed time",
                                onClick = { onNavigate(AppDestination.STOPWATCH) },
                            )
                        }
                        BentoInfoCard(themeMode)
                    }
                }
            } else {
                HybridBentoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (contentMaxWidth < 390.dp) 350.dp else 372.dp),
                    themeMode = themeMode,
                ) {
                    ClockHero(
                        now = now,
                        dateText = dateText,
                        displayMode = clockDisplayMode,
                        use24HourFormat = use24HourFormat,
                        showSeconds = showSeconds,
                        onToggleDisplay = {
                            clockDisplayMode = if (clockDisplayMode == ClockDisplayMode.DIGITAL) ClockDisplayMode.ANALOG else ClockDisplayMode.DIGITAL
                        },
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    TimeActionCard(
                        modifier = Modifier.weight(1f),
                        themeMode = themeMode,
                        icon = Icons.Filled.AccessAlarm,
                        eyebrow = if (next == null) "ALARM" else "NEXT ALARM",
                        title = "Alarm",
                        value = alarmTime,
                        meta = alarmMeta,
                        onClick = { onNavigate(AppDestination.ALARM) },
                    )
                    TimeActionCard(
                        modifier = Modifier.weight(1f),
                        themeMode = themeMode,
                        icon = Icons.Filled.Timer,
                        eyebrow = if (timerRunning) "LIVE" else "TIMER",
                        title = "Timer",
                        value = formatBentoTimer(timerRemainingSeconds),
                        meta = if (timerRunning) "Counting down" else "Ready when you are",
                        onClick = { onNavigate(AppDestination.TIMER) },
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    UtilityCard(
                        modifier = Modifier.weight(1f),
                        themeMode = themeMode,
                        icon = Icons.Filled.Public,
                        title = "World Clock",
                        subtitle = "Cities & time zones",
                        onClick = { onNavigate(AppDestination.WORLD) },
                    )
                    UtilityCard(
                        modifier = Modifier.weight(1f),
                        themeMode = themeMode,
                        icon = Icons.Filled.AccessTime,
                        title = "Stopwatch",
                        subtitle = "Precise elapsed time",
                        onClick = { onNavigate(AppDestination.STOPWATCH) },
                    )
                }

                BentoInfoCard(themeMode)
            }
        }
    }
}

@Composable
private fun BentoInfoCard(themeMode: AppThemeMode) {
    HybridBentoCard(Modifier.fillMaxWidth(), themeMode = themeMode) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Built around your time", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Material 3 Expressive motion, dynamic color, and glass depth stay quiet so the clock stays in focus.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BentoHeader(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Chrona",
                fontSize = 30.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.7).sp,
            )
            Text("time, organized beautifully", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ThemeToggle(themeMode, onThemeModeChange, Modifier.size(width = 112.dp, height = 44.dp).alpha(0.98f))
        BentoIconButton(Icons.Filled.Settings, onOpenSettings, Modifier.size(44.dp), contentDescription = "Settings")
    }
}

@Composable
private fun ClockHero(
    now: java.time.ZonedDateTime,
    dateText: String,
    displayMode: ClockDisplayMode,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    onToggleDisplay: () -> Unit,
) {
    val day = now.hour in 7..17

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Text(if (day) "DAYTIME" else "NIGHTTIME", fontSize = 9.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(5.dp))
                Text(now.zone.id.replace('_', ' '), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            BentoIconButton(
                icon = if (displayMode == ClockDisplayMode.DIGITAL) Icons.Filled.AccessTime else Icons.Filled.GridView,
                onClick = onToggleDisplay,
                contentDescription = if (displayMode == ClockDisplayMode.DIGITAL) "Switch to analog clock" else "Switch to digital clock",
                active = true,
                modifier = Modifier.size(40.dp),
            )
        }

        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Crossfade(
                targetState = displayMode,
                animationSpec = tween(durationMillis = 320),
                label = "clock-style-crossfade",
            ) { mode ->
                when (mode) {
                    ClockDisplayMode.DIGITAL -> DigitalClockUI(now.hour, now.minute, now.second, use24HourFormat, showSeconds)
                    ClockDisplayMode.ANALOG -> AnalogClockUI(now.hour, now.minute, now.second + now.nano / 1_000_000_000f)
                }
            }
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(dateText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(3.dp))
            Text("Local time", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DigitalClockUI(hour24: Int, minute: Int, second: Int, use24HourFormat: Boolean, showSeconds: Boolean) {
    val hour = if (use24HourFormat) hour24 else ((hour24 + 11) % 12) + 1
    val secondsProgress by animateFloatAsState(second / 59f, tween(850), label = "seconds-progress")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            Text(hour.toString().padStart(2, '0'), fontSize = 84.sp, lineHeight = 84.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-5).sp)
            Text(":${minute.toString().padStart(2, '0')}", fontSize = 84.sp, lineHeight = 84.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-5).sp, color = MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!use24HourFormat) {
                Text(if (hour24 < 12) "AM" else "PM", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.primary)
            }
            if (showSeconds) {
                Text("${second.toString().padStart(2, '0')} sec", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = secondsProgress.coerceIn(0.35f, 1f))))
            }
        }
    }
}

@Composable
private fun TimeActionCard(
    modifier: Modifier,
    themeMode: AppThemeMode,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    eyebrow: String,
    title: String,
    value: String,
    meta: String,
    onClick: () -> Unit,
) {
    HybridBentoCard(modifier.height(164.dp), themeMode, onClick) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                BentoIcon(icon, Modifier.size(42.dp))
                Text(eyebrow, fontSize = 8.sp, letterSpacing = 1.1.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(14.dp))
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.7).sp)
            Spacer(Modifier.height(2.dp))
            Text(meta, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun UtilityCard(
    modifier: Modifier,
    themeMode: AppThemeMode,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    HybridBentoCard(modifier.height(128.dp), themeMode, onClick) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                BentoIcon(icon, Modifier.size(40.dp))
                Icon(Icons.Filled.ChevronRight, null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
            }
            Spacer(Modifier.height(13.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AnalogClockUI(hour: Int, minute: Int, second: Float) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surfaceContainerHighest = MaterialTheme.colorScheme.surfaceContainerHighest

    Canvas(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 54.dp)
            .aspectRatio(1f),
    ) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        drawCircle(surfaceContainerHighest.copy(alpha = 0.74f), radius * 0.92f, center)
        drawCircle(primary.copy(alpha = 0.12f), radius * 0.92f, center, style = Stroke(radius * 0.025f))
        drawCircle(Color.White.copy(alpha = 0.14f), radius * 0.865f, center, style = Stroke(radius * 0.010f))

        for (index in 0 until 60) {
            val major = index % 5 == 0
            rotate(index * 6f, pivot = center) {
                val outer = radius * 0.79f
                val inner = radius * if (major) 0.67f else 0.74f
                drawLine(
                    color = onSurface.copy(alpha = if (major) 0.72f else 0.19f),
                    start = androidx.compose.ui.geometry.Offset(center.x, center.y - outer),
                    end = androidx.compose.ui.geometry.Offset(center.x, center.y - inner),
                    strokeWidth = radius * if (major) 0.020f else 0.008f,
                    cap = StrokeCap.Round,
                )
            }
        }

        fun hand(angle: Float, length: Float, width: Float, color: Color, tail: Float = 0f) {
            rotate(angle, pivot = center) {
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(center.x, center.y + radius * tail),
                    end = androidx.compose.ui.geometry.Offset(center.x, center.y - radius * length),
                    strokeWidth = radius * width,
                    cap = StrokeCap.Round,
                )
            }
        }

        hand(((hour % 12) + minute / 60f) * 30f, 0.46f, 0.070f, onSurface, 0.025f)
        hand((minute + second / 60f) * 6f, 0.64f, 0.046f, primary, 0.045f)
        hand(second * 6f, 0.73f, 0.014f, tertiary, 0.11f)
        drawCircle(onSurface, radius * 0.052f, center)
        drawCircle(primary, radius * 0.022f, center)
    }
}

private fun nextAlarm(alarms: List<AlarmItem>, now: java.time.ZonedDateTime): AlarmItem? {
    val enabled = alarms.filter(AlarmItem::enabled)
    if (enabled.isEmpty()) return null
    val localNow = now.toLocalDateTime()
    return enabled.minByOrNull { alarm ->
        val today = LocalDateTime.of(now.toLocalDate(), alarm.time)
        val candidate = if (today.isAfter(localNow)) today else today.plusDays(1)
        Duration.between(localNow, candidate).toMillis()
    }
}

private fun buildDateText(now: java.time.ZonedDateTime, locale: Locale): String {
    val day = now.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val month = now.month.getDisplayName(TextStyle.FULL, locale)
    return "$day, ${now.dayOfMonth} $month ${now.year}"
}

private fun formatBentoTimer(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe / 60) % 60
    val seconds = safe % 60
    return if (hours > 0) String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    else String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
