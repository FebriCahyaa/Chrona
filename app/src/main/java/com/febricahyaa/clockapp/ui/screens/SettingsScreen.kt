/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockDisplayMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.ui.components.ChronaScaffold
import com.febricahyaa.clockapp.ui.update.ChronaReleaseTimeline
import com.febricahyaa.clockapp.ui.viewmodel.UpdateUiState

@Composable
fun SettingsSheetContent(
    settings: ClockSettings,
    use24HourFormat: Boolean,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onAccentChange: (ThemeAccent) -> Unit,
    onFormatChange: (Boolean) -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
    onClockDisplayModeChange: (ClockDisplayMode) -> Unit,
    onOpenNotificationSettings: () -> Unit = {},
    notificationPermissionGranted: Boolean = true,
    onOpenLegal: () -> Unit = {},
    updateState: UpdateUiState = UpdateUiState(),
    onCheckForUpdates: () -> Unit = {},
    onOpenUpdate: () -> Unit = {},
    modifier: Modifier = Modifier,
    showSectionHeader: Boolean = true,
) {
    var showReleaseTimeline by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (showSectionHeader) {
            item(key = "settings-header") {
                Column {
                    Text(
                        stringResource(R.string.settings_screen_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        stringResource(R.string.settings_screen_subtitle),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item(key = "settings-content") {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1180.dp),
            ) {
                when (chronaSettingsWindowClass(maxWidth.value.toInt())) {
                    ChronaSettingsWindowClass.EXPANDED -> SettingsExpandedDashboard(
                        settings = settings,
                        use24HourFormat = use24HourFormat,
                        onThemeModeChange = onThemeModeChange,
                        onAccentChange = onAccentChange,
                        onFormatChange = onFormatChange,
                        onShowSecondsChange = onShowSecondsChange,
                        onClockDisplayModeChange = onClockDisplayModeChange,
                        onOpenNotificationSettings = onOpenNotificationSettings,
                        notificationPermissionGranted = notificationPermissionGranted,
                        onOpenLegal = onOpenLegal,
                        updateState = updateState,
                        onCheckForUpdates = onCheckForUpdates,
                        onOpenUpdate = onOpenUpdate,
                        onViewReleaseTimeline = { showReleaseTimeline = true },
                    )

                    ChronaSettingsWindowClass.MEDIUM -> SettingsMediumDashboard(
                        settings = settings,
                        use24HourFormat = use24HourFormat,
                        onThemeModeChange = onThemeModeChange,
                        onAccentChange = onAccentChange,
                        onFormatChange = onFormatChange,
                        onShowSecondsChange = onShowSecondsChange,
                        onClockDisplayModeChange = onClockDisplayModeChange,
                        onOpenNotificationSettings = onOpenNotificationSettings,
                        notificationPermissionGranted = notificationPermissionGranted,
                        onOpenLegal = onOpenLegal,
                        updateState = updateState,
                        onCheckForUpdates = onCheckForUpdates,
                        onOpenUpdate = onOpenUpdate,
                        onViewReleaseTimeline = { showReleaseTimeline = true },
                    )

                    ChronaSettingsWindowClass.COMPACT -> SettingsCompactDashboard(
                        settings = settings,
                        use24HourFormat = use24HourFormat,
                        onThemeModeChange = onThemeModeChange,
                        onAccentChange = onAccentChange,
                        onFormatChange = onFormatChange,
                        onShowSecondsChange = onShowSecondsChange,
                        onClockDisplayModeChange = onClockDisplayModeChange,
                        onOpenNotificationSettings = onOpenNotificationSettings,
                        notificationPermissionGranted = notificationPermissionGranted,
                        onOpenLegal = onOpenLegal,
                        updateState = updateState,
                        onCheckForUpdates = onCheckForUpdates,
                        onOpenUpdate = onOpenUpdate,
                        onViewReleaseTimeline = { showReleaseTimeline = true },
                    )
                }
            }
        }
    }

    if (showReleaseTimeline && updateState.snapshot.latestVersion != null) {
        ChronaReleaseTimeline(
            snapshot = updateState.snapshot,
            onDismiss = { showReleaseTimeline = false },
            onOpenExternal = {
                showReleaseTimeline = false
                onOpenUpdate()
            },
        )
    }
}

@Composable
private fun SettingsCompactDashboard(
    settings: ClockSettings,
    use24HourFormat: Boolean,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onAccentChange: (ThemeAccent) -> Unit,
    onFormatChange: (Boolean) -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
    onClockDisplayModeChange: (ClockDisplayMode) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    notificationPermissionGranted: Boolean,
    onOpenLegal: () -> Unit,
    updateState: UpdateUiState,
    onCheckForUpdates: () -> Unit,
    onOpenUpdate: () -> Unit,
    onViewReleaseTimeline: () -> Unit = {},
) {
    SettingsPreviewCard(settings, use24HourFormat)
    Spacer(Modifier.height(18.dp))
    SettingsAppearanceSection(settings, onThemeModeChange, onAccentChange)
    Spacer(Modifier.height(18.dp))
    SettingsClockSection(settings, use24HourFormat, onFormatChange, onShowSecondsChange, onClockDisplayModeChange)
    Spacer(Modifier.height(18.dp))
    SettingsNotificationsSection(notificationPermissionGranted, onOpenNotificationSettings)
    Spacer(Modifier.height(18.dp))
    SettingsUpdateSection(updateState, onCheckForUpdates, onOpenUpdate, onViewReleaseTimeline)
    Spacer(Modifier.height(18.dp))
    SettingsAboutSection(onOpenLegal)
}

@Composable
private fun SettingsMediumDashboard(
    settings: ClockSettings,
    use24HourFormat: Boolean,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onAccentChange: (ThemeAccent) -> Unit,
    onFormatChange: (Boolean) -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
    onClockDisplayModeChange: (ClockDisplayMode) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    notificationPermissionGranted: Boolean,
    onOpenLegal: () -> Unit,
    updateState: UpdateUiState,
    onCheckForUpdates: () -> Unit,
    onOpenUpdate: () -> Unit,
    onViewReleaseTimeline: () -> Unit = {},
) {
    SettingsPreviewCard(settings, use24HourFormat)
    Spacer(Modifier.height(14.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        SettingsDashboardPanel(Modifier.weight(1f)) {
            SettingsAppearanceSection(settings, onThemeModeChange, onAccentChange)
            Spacer(Modifier.height(22.dp))
            SettingsClockSection(settings, use24HourFormat, onFormatChange, onShowSecondsChange, onClockDisplayModeChange)
        }
        SettingsDashboardPanel(Modifier.weight(1f)) {
            SettingsNotificationsSection(notificationPermissionGranted, onOpenNotificationSettings)
            Spacer(Modifier.height(22.dp))
            SettingsUpdateSection(updateState, onCheckForUpdates, onOpenUpdate, onViewReleaseTimeline)
            Spacer(Modifier.height(22.dp))
            SettingsAboutSection(onOpenLegal)
        }
    }
}

@Composable
private fun SettingsExpandedDashboard(
    settings: ClockSettings,
    use24HourFormat: Boolean,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onAccentChange: (ThemeAccent) -> Unit,
    onFormatChange: (Boolean) -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
    onClockDisplayModeChange: (ClockDisplayMode) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    notificationPermissionGranted: Boolean,
    onOpenLegal: () -> Unit,
    updateState: UpdateUiState,
    onCheckForUpdates: () -> Unit,
    onOpenUpdate: () -> Unit,
    onViewReleaseTimeline: () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        SettingsDashboardPanel(Modifier.weight(1.15f)) {
            SettingsPreviewCard(settings, use24HourFormat)
            Spacer(Modifier.height(18.dp))
            SettingsClockSection(settings, use24HourFormat, onFormatChange, onShowSecondsChange, onClockDisplayModeChange)
        }
        SettingsDashboardPanel(Modifier.weight(1f)) {
            SettingsAppearanceSection(settings, onThemeModeChange, onAccentChange)
        }
        SettingsDashboardPanel(Modifier.weight(1f)) {
            SettingsNotificationsSection(notificationPermissionGranted, onOpenNotificationSettings)
            Spacer(Modifier.height(20.dp))
            SettingsUpdateSection(updateState, onCheckForUpdates, onOpenUpdate, onViewReleaseTimeline)
            Spacer(Modifier.height(20.dp))
            SettingsAboutSection(onOpenLegal)
        }
    }
}

@Composable
private fun SettingsDashboardPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.09f),
        ),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsScreen(
    settings: ClockSettings,
    use24HourFormat: Boolean,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onAccentChange: (ThemeAccent) -> Unit,
    onFormatChange: (Boolean) -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
    onClockDisplayModeChange: (ClockDisplayMode) -> Unit,
    onOpenNotificationSettings: () -> Unit = {},
    notificationPermissionGranted: Boolean = true,
    onOpenLegal: () -> Unit,
    updateState: UpdateUiState = UpdateUiState(),
    onCheckForUpdates: () -> Unit = {},
    onOpenUpdate: () -> Unit = {},
    onBack: () -> Unit,
) {
    ChronaScaffold(
        title = stringResource(R.string.settings_screen_title),
        subtitle = stringResource(R.string.settings_screen_subtitle),
        onBack = onBack,
    ) { paddingValues ->
        SettingsSheetContent(
            settings = settings,
            use24HourFormat = use24HourFormat,
            onThemeModeChange = onThemeModeChange,
            onAccentChange = onAccentChange,
            onFormatChange = onFormatChange,
            onShowSecondsChange = onShowSecondsChange,
            onClockDisplayModeChange = onClockDisplayModeChange,
            onOpenNotificationSettings = onOpenNotificationSettings,
            notificationPermissionGranted = notificationPermissionGranted,
            onOpenLegal = onOpenLegal,
            updateState = updateState,
            onCheckForUpdates = onCheckForUpdates,
            onOpenUpdate = onOpenUpdate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            showSectionHeader = false,
        )
    }
}
