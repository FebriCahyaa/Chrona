/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.BuildConfig
import com.febricahyaa.clockapp.core.ChronaTimeEngine
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.ui.components.HybridBentoCard
import com.febricahyaa.clockapp.ui.components.SectionEyebrow
import com.febricahyaa.clockapp.ui.components.rememberZonedNow
import com.febricahyaa.clockapp.ui.theme.accentGradientColors
import com.febricahyaa.clockapp.ui.viewmodel.UpdateUiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.Instant

@Composable
fun SettingsPreviewCard(
    settings: ClockSettings,
    use24HourFormat: Boolean,
) {
    val now = rememberZonedNow(ZoneId.systemDefault())
    val epochMillis = now.toInstant().toEpochMilli()
    val previewTime = ChronaTimeEngine.time(
        epochMillis,
        now.zone,
        use24HourFormat,
        settings.showSeconds,
    )
    val previewDate = ChronaTimeEngine.date(epochMillis, now.zone)

    HybridBentoCard(Modifier.fillMaxWidth(), themeMode = settings.themeMode) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SectionEyebrow("LIVE PREVIEW")
            Spacer(Modifier.height(8.dp))
            Text(
                previewTime,
                fontSize = 44.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1.7).sp,
            )
            Text(
                previewDate,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                now.zone.id.replace('_', ' '),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SettingsAppearanceSection(
    settings: ClockSettings,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onAccentChange: (ThemeAccent) -> Unit,
) {
    SettingsSectionTitle(Icons.Filled.Palette, "Style")
    Spacer(Modifier.height(9.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SettingsThemeChoice(
            AppThemeMode.NEUMORPHIC,
            settings.themeMode == AppThemeMode.NEUMORPHIC,
            Modifier.weight(1f),
            onThemeModeChange,
        )
        SettingsThemeChoice(
            AppThemeMode.MATERIAL_YOU,
            settings.themeMode == AppThemeMode.MATERIAL_YOU,
            Modifier.weight(1f),
            onThemeModeChange,
        )
    }

    Spacer(Modifier.height(20.dp))
    SettingsSectionTitle(Icons.Filled.Tune, "Accent")
    Spacer(Modifier.height(9.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ThemeAccent.entries.forEach { accent ->
            val selected = settings.themeAccent == accent
            val (start, end) = accentGradientColors(accent, MaterialTheme.colorScheme.primary)
            Surface(
                onClick = { onAccentChange(accent) },
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(
                    if (selected) 2.dp else 1.dp,
                    if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                ),
            ) {
                Box(
                    Modifier
                        .padding(if (selected) 3.dp else 0.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(start, end))),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(
                            Icons.Filled.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsClockSection(
    settings: ClockSettings,
    use24HourFormat: Boolean,
    onFormatChange: (Boolean) -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
) {
    SettingsSectionTitle(Icons.Filled.Tune, "Clock")
    Spacer(Modifier.height(5.dp))
    SettingsPreferenceRow(
        "24-hour format",
        "Use 00:00–23:59 instead of AM/PM",
        use24HourFormat,
        onFormatChange,
    )
    SettingsPreferenceRow(
        "Show seconds",
        "Show the live seconds readout in the hero clock",
        settings.showSeconds,
        onShowSecondsChange,
    )
}

@Composable
fun SettingsNotificationsSection(
    permissionGranted: Boolean,
    onOpenNotificationSettings: () -> Unit,
) {
    SettingsSectionTitle(Icons.Filled.Notifications, "Notifications")
    Spacer(Modifier.height(9.dp))
    Surface(
        onClick = onOpenNotificationSettings,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Notification access", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(
                    if (permissionGranted) "Allowed for alarms and timer notices"
                    else "Not allowed · tap to open Android notification settings",
                    fontSize = 11.sp,
                    color = if (permissionGranted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
fun SettingsUpdateSection(
    state: UpdateUiState,
    onCheckForUpdates: () -> Unit,
    onOpenUpdate: () -> Unit,
    onViewReleaseTimeline: () -> Unit = {},
) {
    SettingsSectionTitle(Icons.Filled.SystemUpdate, "App updates")
    Spacer(Modifier.height(9.dp))
    val snapshot = state.snapshot
    val checkedLabel = snapshot.lastCheckedAt.takeIf { it > 0L }?.let { timestamp ->
        runCatching {
            Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        }.getOrNull()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        when {
                            state.isChecking -> "Checking for updates"
                            !state.errorMessage.isNullOrBlank() -> "Update check failed"
                            state.isUpdateAvailable -> "Update available"
                            snapshot.latestVersion != null -> "Chrona is up to date"
                            else -> "Updates not checked yet"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Current ${BuildConfig.VERSION_NAME}" +
                            (snapshot.latestVersion?.let { " · Latest $it" } ?: ""),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (checkedLabel != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Last checked $checkedLabel",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (state.isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }

            state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(
                    message,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(10.dp))
            Surface(
                onClick = onCheckForUpdates,
                enabled = !state.isChecking,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Refresh, null, Modifier.size(17.dp))
                    Spacer(Modifier.size(7.dp))
                    Text("Check now", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (!snapshot.releaseUrl.isNullOrBlank() && snapshot.latestVersion != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        onClick = onViewReleaseTimeline,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                        ),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Release timeline", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Surface(
                        onClick = onOpenUpdate,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                        ),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Open GitHub", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsAboutSection(onOpenLegal: () -> Unit) {
    SettingsSectionTitle(Icons.Filled.Gavel, "About")
    Spacer(Modifier.height(9.dp))
    Surface(
        onClick = onOpenLegal,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Chrona", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Version ${BuildConfig.VERSION_NAME} · private on-device settings",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Legal, license, and app information",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingsThemeChoice(
    mode: AppThemeMode,
    selected: Boolean,
    modifier: Modifier,
    onClick: (AppThemeMode) -> Unit,
) {
    val sample = when (mode) {
        AppThemeMode.NEUMORPHIC -> MaterialTheme.colorScheme.surface
        AppThemeMode.MATERIAL_YOU -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surface
    }
    Surface(
        onClick = { onClick(mode) },
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = sample,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            Modifier.padding(9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(sample),
            )
            Spacer(Modifier.height(7.dp))
            Text(
                when (mode) {
                    AppThemeMode.NEUMORPHIC -> "Soft / raised"
                    AppThemeMode.MATERIAL_YOU -> "Dynamic / tonal"
                    else -> mode.name.lowercase().replaceFirstChar { it.uppercase() }
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SettingsPreferenceRow(
    title: String,
    subtitle: String,
    value: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = value, onCheckedChange = onChange)
    }
}
