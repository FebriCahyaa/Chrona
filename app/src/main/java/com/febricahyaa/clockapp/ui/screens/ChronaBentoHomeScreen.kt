/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.navigation.AppDestination
import com.febricahyaa.clockapp.navigation.ChronaMotionKeys
import com.febricahyaa.clockapp.navigation.chronaSharedBounds
import com.febricahyaa.clockapp.ui.components.BentoIcon
import com.febricahyaa.clockapp.ui.components.BentoIconButton
import com.febricahyaa.clockapp.ui.components.ChronaScaffold
import com.febricahyaa.clockapp.ui.components.HybridBentoCard
import com.febricahyaa.clockapp.ui.components.ThemeToggle
import com.febricahyaa.clockapp.ui.components.rememberZonedNow
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private enum class ClockDisplayMode { DIGITAL, ANALOG }

@Composable
fun ChronaBentoHomeScreen(
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
    val alarmTime = next?.time?.let { formatAlarmTime(it, use24HourFormat, locale) } ?: stringResource(R.string.home_alarm_not_set)
    val alarmMeta = when {
        next == null -> stringResource(R.string.home_alarm_create)
        next.repeatDays.isEmpty() -> stringResource(R.string.home_alarm_one_time)
        else -> stringResource(R.string.home_alarm_repeats)
    }
    val windowAdaptiveInfo = androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2()
    val isWideWindow = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass == androidx.window.core.layout.WindowWidthSizeClass.EXPANDED
    val dateText = buildDateText(now, locale)
    val scrollState = rememberScrollState()

    ChronaScaffold(
        title = stringResource(R.string.home_title),
        subtitle = stringResource(R.string.home_subtitle),
        actions = {
            ThemeToggle(
                themeMode,
                onThemeModeChange,
                Modifier.size(width = 118.dp, height = 46.dp),
            )
            BentoIconButton(
                icon = Icons.Filled.Settings,
                onClick = onOpenSettings,
                modifier = Modifier.size(46.dp),
                contentDescription = stringResource(R.string.home_settings),
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(
                    horizontal = if (isWideWindow) 24.dp else 16.dp,
                    vertical = 12.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isWideWindow) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    HeroCard(
                        modifier = Modifier.weight(1.26f).heightIn(min = 360.dp),
                        themeMode = themeMode,
                        now = now,
                        dateText = dateText,
                        displayMode = clockDisplayMode,
                        use24HourFormat = use24HourFormat,
                        showSeconds = showSeconds,
                        onToggleDisplay = { clockDisplayMode = clockDisplayMode.toggle() },
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionGrid(
                            themeMode = themeMode,
                            nextAlarm = alarmTime,
                            alarmMeta = alarmMeta,
                            timerRemainingSeconds = timerRemainingSeconds,
                            timerRunning = timerRunning,
                            onNavigate = onNavigate,
                        )
                        BentoInfoCard(themeMode)
                    }
                }
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                ) {
                    HeroCard(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 340.dp),
                        themeMode = themeMode,
                        now = now,
                        dateText = dateText,
                        displayMode = clockDisplayMode,
                        use24HourFormat = use24HourFormat,
                        showSeconds = showSeconds,
                        onToggleDisplay = { clockDisplayMode = clockDisplayMode.toggle() },
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 10.dp)
                            .offset(y = 74.dp)
                            .zIndex(2f),
                    ) {
                        FloatingActionGrid(
                            themeMode = themeMode,
                            nextAlarm = alarmTime,
                            alarmMeta = alarmMeta,
                            timerRemainingSeconds = timerRemainingSeconds,
                            timerRunning = timerRunning,
                            onNavigate = onNavigate,
                        )
                    }
                }
                Spacer(Modifier.height(78.dp))
                BentoInfoCard(themeMode)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun ClockDisplayMode.toggle() = if (this == ClockDisplayMode.DIGITAL) ClockDisplayMode.ANALOG else ClockDisplayMode.DIGITAL

@Composable
private fun HeroCard(
    modifier: Modifier,
    themeMode: AppThemeMode,
    now: java.time.ZonedDateTime,
    dateText: String,
    displayMode: ClockDisplayMode,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    onToggleDisplay: () -> Unit,
) {
    HybridBentoCard(
        modifier = modifier.chronaSharedBounds(ChronaMotionKeys.DASHBOARD_HERO_CLOCK),
        themeMode = themeMode,
    ) {
        ClockHero(now, dateText, displayMode, use24HourFormat, showSeconds, onToggleDisplay)
    }
}

@Composable
private fun FloatingActionGrid(
    themeMode: AppThemeMode,
    nextAlarm: String,
    alarmMeta: String,
    timerRemainingSeconds: Int,
    timerRunning: Boolean,
    onNavigate: (AppDestination) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
        tonalElevation = 3.dp,
    ) {
        Column(
            Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeActionCard(
                    modifier = Modifier.weight(1f).height(136.dp).chronaSharedBounds(ChronaMotionKeys.DASHBOARD_ALARM),
                    themeMode = themeMode,
                    icon = Icons.Filled.AccessAlarm,
                    eyebrow = if (nextAlarm == stringResource(R.string.home_alarm_not_set)) stringResource(R.string.home_alarm_label) else stringResource(R.string.home_next_alarm_label),
                    title = stringResource(R.string.home_alarm_label),
                    value = nextAlarm,
                    meta = alarmMeta,
                    onClick = { onNavigate(AppDestination.ALARM) },
                )
                TimeActionCard(
                    modifier = Modifier.weight(1f).height(136.dp).chronaSharedBounds(ChronaMotionKeys.DASHBOARD_TIMER),
                    themeMode = themeMode,
                    icon = Icons.Filled.Timer,
                    eyebrow = if (timerRunning) stringResource(R.string.home_timer_live_label) else stringResource(R.string.home_timer_label),
                    title = stringResource(R.string.home_timer_label),
                    value = formatBentoTimer(timerRemainingSeconds),
                    meta = if (timerRunning) stringResource(R.string.home_timer_counting) else stringResource(R.string.home_timer_ready),
                    onClick = { onNavigate(AppDestination.TIMER) },
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UtilityCard(
                    modifier = Modifier.weight(1f).height(110.dp).chronaSharedBounds(ChronaMotionKeys.DASHBOARD_WORLD_CLOCK),
                    themeMode = themeMode,
                    icon = Icons.Filled.Public,
                    title = stringResource(R.string.home_world_clock_title),
                    subtitle = stringResource(R.string.home_world_clock_subtitle),
                    onClick = { onNavigate(AppDestination.WORLD) },
                )
                UtilityCard(
                    modifier = Modifier.weight(1f).height(110.dp).chronaSharedBounds(ChronaMotionKeys.DASHBOARD_STOPWATCH),
                    themeMode = themeMode,
                    icon = Icons.Filled.AccessTime,
                    title = stringResource(R.string.home_stopwatch_title),
                    subtitle = stringResource(R.string.home_stopwatch_subtitle),
                    onClick = { onNavigate(AppDestination.STOPWATCH) },
                )
            }
        }
    }
}

@Composable
private fun ActionGrid(
    themeMode: AppThemeMode,
    nextAlarm: String,
    alarmMeta: String,
    timerRemainingSeconds: Int,
    timerRunning: Boolean,
    onNavigate: (AppDestination) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TimeActionCard(
            modifier = Modifier.weight(1f).chronaSharedBounds(ChronaMotionKeys.DASHBOARD_ALARM),
            themeMode = themeMode,
            icon = Icons.Filled.AccessAlarm,
            eyebrow = if (nextAlarm == stringResource(R.string.home_alarm_not_set)) stringResource(R.string.home_alarm_label) else stringResource(R.string.home_next_alarm_label),
            title = stringResource(R.string.home_alarm_label),
            value = nextAlarm,
            meta = alarmMeta,
            onClick = { onNavigate(AppDestination.ALARM) },
        )
        TimeActionCard(
            modifier = Modifier.weight(1f).chronaSharedBounds(ChronaMotionKeys.DASHBOARD_TIMER),
            themeMode = themeMode,
            icon = Icons.Filled.Timer,
            eyebrow = if (timerRunning) stringResource(R.string.home_timer_live_label) else stringResource(R.string.home_timer_label),
            title = stringResource(R.string.home_timer_label),
            value = formatBentoTimer(timerRemainingSeconds),
            meta = if (timerRunning) stringResource(R.string.home_timer_counting) else stringResource(R.string.home_timer_ready),
            onClick = { onNavigate(AppDestination.TIMER) },
        )
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        UtilityCard(
            modifier = Modifier.weight(1f).chronaSharedBounds(ChronaMotionKeys.DASHBOARD_WORLD_CLOCK),
            themeMode = themeMode,
            icon = Icons.Filled.Public,
            title = stringResource(R.string.home_world_clock_title),
            subtitle = stringResource(R.string.home_world_clock_subtitle),
            onClick = { onNavigate(AppDestination.WORLD) },
        )
        UtilityCard(
            modifier = Modifier.weight(1f).chronaSharedBounds(ChronaMotionKeys.DASHBOARD_STOPWATCH),
            themeMode = themeMode,
            icon = Icons.Filled.AccessTime,
            title = stringResource(R.string.home_stopwatch_title),
            subtitle = stringResource(R.string.home_stopwatch_subtitle),
            onClick = { onNavigate(AppDestination.STOPWATCH) },
        )
    }
}

@Composable
private fun BentoInfoCard(themeMode: AppThemeMode) {
    HybridBentoCard(Modifier.fillMaxWidth(), themeMode = themeMode) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.11f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.home_info_title), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text(
                    stringResource(R.string.home_info_body),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Text(if (day) stringResource(R.string.home_daytime) else stringResource(R.string.home_nighttime), fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(5.dp))
                Text(now.zone.id.replace('_', ' '), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            BentoIconButton(
                icon = if (displayMode == ClockDisplayMode.DIGITAL) Icons.Filled.AccessTime else Icons.Filled.GridView,
                onClick = onToggleDisplay,
                contentDescription = if (displayMode == ClockDisplayMode.DIGITAL) stringResource(R.string.home_switch_to_analog) else stringResource(R.string.home_switch_to_digital),
                active = true,
                modifier = Modifier.size(42.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = displayMode,
                transitionSpec = {
                    fadeIn(tween(180)).togetherWith(fadeOut(tween(120)))
                },
                label = "clock-style-transition",
            ) { mode ->
                when (mode) {
                    ClockDisplayMode.DIGITAL -> DigitalClockUI(
                        now.hour,
                        now.minute,
                        now.second,
                        use24HourFormat,
                        showSeconds,
                    )
                    ClockDisplayMode.ANALOG -> AnalogClockUI(
                        now.hour,
                        now.minute,
                        now.second + now.nano / 1_000_000_000f,
                    )
                }
            }
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(dateText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(3.dp))
            Text(stringResource(R.string.home_local_time), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DigitalClockUI(hour24: Int, minute: Int, second: Int, use24HourFormat: Boolean, showSeconds: Boolean) {
    val hour = if (use24HourFormat) hour24 else ((hour24 + 11) % 12) + 1
    val secondsProgress by animateFloatAsState(second / 59f, tween(850), label = "seconds-progress")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            val heroClockStyle = MaterialTheme.typography.displayLarge.copy(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeight = 1.em,
                fontSize = 76.sp,
                letterSpacing = (-4.5).sp,
            )
            Text(
                text = hour.toString().padStart(2, '0'),
                style = heroClockStyle,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false,
            )
            Text(
                text = ":${minute.toString().padStart(2, '0')}",
                style = heroClockStyle,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                softWrap = false,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!use24HourFormat) {
                Text(stringResource(if (hour24 < 12) R.string.time_am else R.string.time_pm), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.primary)
            }
            if (showSeconds) {
                Text(stringResource(R.string.home_seconds_suffix, second.toString().padStart(2, '0')), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = secondsProgress.coerceIn(0.35f, 1f))))
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
    HybridBentoCard(modifier.height(154.dp), themeMode, onClick) {
        Column(Modifier.padding(15.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                BentoIcon(icon, Modifier.size(40.dp))
                Text(eyebrow, fontSize = 9.sp, letterSpacing = 1.0.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.6).sp, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            Text(meta, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
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
    HybridBentoCard(modifier.height(124.dp), themeMode, onClick) {
        Column(Modifier.padding(15.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                BentoIcon(icon, Modifier.size(38.dp))
                Icon(Icons.Filled.ChevronRight, null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
            }
            Spacer(Modifier.height(11.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun AnalogClockUI(hour: Int, minute: Int, second: Float) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surface = MaterialTheme.colorScheme.surfaceContainerHighest

    Canvas(
        Modifier.fillMaxWidth().padding(horizontal = 54.dp).aspectRatio(1f),
    ) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f
        drawCircle(surface, radius * 0.90f, center)
        drawCircle(primary.copy(alpha = 0.12f), radius * 0.90f, center, style = Stroke(radius * 0.025f))
        drawCircle(Color.White.copy(alpha = 0.12f), radius * 0.855f, center, style = Stroke(radius * 0.010f))

        for (index in 0 until 60) {
            val major = index % 5 == 0
            rotate(index * 6f, pivot = center) {
                val outer = radius * 0.78f
                val inner = radius * if (major) 0.66f else 0.73f
                drawLine(
                    color = onSurface.copy(alpha = if (major) 0.72f else 0.18f),
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

private fun formatAlarmTime(time: java.time.LocalTime, use24Hour: Boolean, locale: Locale): String {
    val pattern = if (use24Hour) "HH:mm" else "h:mm a"
    return time.format(DateTimeFormatter.ofPattern(pattern, locale))
}

private fun buildDateText(now: java.time.ZonedDateTime, locale: Locale): String =
    DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.LONG).withLocale(locale).format(now)

private fun formatBentoTimer(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe / 60) % 60
    val seconds = safe % 60
    return if (hours > 0) String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    else String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
