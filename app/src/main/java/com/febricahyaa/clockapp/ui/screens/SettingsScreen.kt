/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.ui.components.ChronaScaffold
import com.febricahyaa.clockapp.ui.viewmodel.UpdateUiState

@Composable
fun SettingsSheetContent(
    settings: ClockSettings,
    use24HourFormat: Boolean,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onAccentChange: (ThemeAccent) -> Unit,
    onFormatChange: (Boolean) -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit = {},
    notificationPermissionGranted: Boolean = true,
    onOpenLegal: () -> Unit = {},
    updateState: UpdateUiState = UpdateUiState(),
    onCheckForUpdates: () -> Unit = {},
    onOpenUpdate: () -> Unit = {},
    modifier: Modifier = Modifier,
    showSectionHeader: Boolean = true,
) {
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(bottom = 34.dp),
    ) {
        if (showSectionHeader) {
            Spacer(Modifier.height(4.dp))
            Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(3.dp))
            Text(
                "Tune appearance, clock behavior, updates, and app information.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
        } else {
            Spacer(Modifier.height(4.dp))
        }

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
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    notificationPermissionGranted = notificationPermissionGranted,
                    onOpenLegal = onOpenLegal,
                    updateState = updateState,
                    onCheckForUpdates = onCheckForUpdates,
                    onOpenUpdate = onOpenUpdate,
                )

                ChronaSettingsWindowClass.MEDIUM -> SettingsMediumDashboard(
                    settings = settings,
                    use24HourFormat = use24HourFormat,
                    onThemeModeChange = onThemeModeChange,
                    onAccentChange = onAccentChange,
                    onFormatChange = onFormatChange,
                    onShowSecondsChange = onShowSecondsChange,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    notificationPermissionGranted = notificationPermissionGranted,
                    onOpenLegal = onOpenLegal,
                    updateState = updateState,
                    onCheckForUpdates = onCheckForUpdates,
                    onOpenUpdate = onOpenUpdate,
                )

                ChronaSettingsWindowClass.COMPACT -> SettingsCompactDashboard(
                    settings = settings,
                    use24HourFormat = use24HourFormat,
                    onThemeModeChange = onThemeModeChange,
                    onAccentChange = onAccentChange,
                    onFormatChange = onFormatChange,
                    onShowSecondsChange = onShowSecondsChange,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    notificationPermissionGranted = notificationPermissionGranted,
                    onOpenLegal = onOpenLegal,
                    updateState = updateState,
                    onCheckForUpdates = onCheckForUpdates,
                    onOpenUpdate = onOpenUpdate,
                )
            }
        }
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
    onOpenNotificationSettings: () -> Unit,
    notificationPermissionGranted: Boolean,
    onOpenLegal: () -> Unit,
    updateState: UpdateUiState,
    onCheckForUpdates: () -> Unit,
    onOpenUpdate: () -> Unit,
) {
    SettingsPreviewCard(settings, use24HourFormat)
    Spacer(Modifier.height(18.dp))
    SettingsAppearanceSection(settings, onThemeModeChange, onAccentChange)
    Spacer(Modifier.height(18.dp))
    SettingsClockSection(settings, use24HourFormat, onFormatChange, onShowSecondsChange)
    Spacer(Modifier.height(18.dp))
    SettingsNotificationsSection(notificationPermissionGranted, onOpenNotificationSettings)
    Spacer(Modifier.height(18.dp))
    SettingsUpdateSection(updateState, onCheckForUpdates, onOpenUpdate)
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
    onOpenNotificationSettings: () -> Unit,
    notificationPermissionGranted: Boolean,
    onOpenLegal: () -> Unit,
    updateState: UpdateUiState,
    onCheckForUpdates: () -> Unit,
    onOpenUpdate: () -> Unit,
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
            SettingsClockSection(settings, use24HourFormat, onFormatChange, onShowSecondsChange)
        }
        SettingsDashboardPanel(Modifier.weight(1f)) {
            SettingsNotificationsSection(notificationPermissionGranted, onOpenNotificationSettings)
            Spacer(Modifier.height(22.dp))
            SettingsUpdateSection(updateState, onCheckForUpdates, onOpenUpdate)
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
    onOpenNotificationSettings: () -> Unit,
    notificationPermissionGranted: Boolean,
    onOpenLegal: () -> Unit,
    updateState: UpdateUiState,
    onCheckForUpdates: () -> Unit,
    onOpenUpdate: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        SettingsDashboardPanel(Modifier.weight(1.15f)) {
            SettingsPreviewCard(settings, use24HourFormat)
            Spacer(Modifier.height(18.dp))
            SettingsClockSection(settings, use24HourFormat, onFormatChange, onShowSecondsChange)
        }
        SettingsDashboardPanel(Modifier.weight(1f)) {
            SettingsAppearanceSection(settings, onThemeModeChange, onAccentChange)
        }
        SettingsDashboardPanel(Modifier.weight(1f)) {
            SettingsNotificationsSection(notificationPermissionGranted, onOpenNotificationSettings)
            Spacer(Modifier.height(20.dp))
            SettingsUpdateSection(updateState, onCheckForUpdates, onOpenUpdate)
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
    onOpenNotificationSettings: () -> Unit = {},
    notificationPermissionGranted: Boolean = true,
    onOpenLegal: () -> Unit,
    updateState: UpdateUiState = UpdateUiState(),
    onCheckForUpdates: () -> Unit = {},
    onOpenUpdate: () -> Unit = {},
    onBack: () -> Unit,
) {
    ChronaScaffold(
        title = "Settings",
        subtitle = "Tune appearance, clock behavior, and app information",
        onBack = onBack,
    ) { paddingValues ->
        SettingsSheetContent(
            settings = settings,
            use24HourFormat = use24HourFormat,
            onThemeModeChange = onThemeModeChange,
            onAccentChange = onAccentChange,
            onFormatChange = onFormatChange,
            onShowSecondsChange = onShowSecondsChange,
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
