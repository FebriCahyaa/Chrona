/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
            .padding(horizontal = 22.dp)
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

        SettingsPreviewCard(
            settings = settings,
            use24HourFormat = use24HourFormat,
        )
        Spacer(Modifier.height(20.dp))

        SettingsAppearanceSection(
            settings = settings,
            onThemeModeChange = onThemeModeChange,
            onAccentChange = onAccentChange,
        )
        Spacer(Modifier.height(18.dp))

        SettingsClockSection(
            settings = settings,
            use24HourFormat = use24HourFormat,
            onFormatChange = onFormatChange,
            onShowSecondsChange = onShowSecondsChange,
        )
        Spacer(Modifier.height(18.dp))

        SettingsNotificationsSection(
            permissionGranted = notificationPermissionGranted,
            onOpenNotificationSettings = onOpenNotificationSettings,
        )
        Spacer(Modifier.height(18.dp))

        SettingsUpdateSection(
            state = updateState,
            onCheckForUpdates = onCheckForUpdates,
            onOpenUpdate = onOpenUpdate,
        )
        Spacer(Modifier.height(18.dp))

        SettingsAboutSection(onOpenLegal = onOpenLegal)
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
