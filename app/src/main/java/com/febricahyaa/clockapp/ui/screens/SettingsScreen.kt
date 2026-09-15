package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    use24HourFormat: Boolean,
    onFormatChange: (Boolean) -> Unit,
    showSeconds: Boolean,
    onShowSecondsChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.settings_screen_title), style = MaterialTheme.typography.headlineMedium)
        Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                SettingRow(
                    title = stringResource(R.string.settings_dark_theme_title),
                    subtitle = stringResource(R.string.settings_dark_theme_subtitle),
                    checked = isDarkTheme,
                    onCheckedChange = onThemeChanged
                )
                SettingRow(
                    title = stringResource(R.string.settings_format_title),
                    subtitle = stringResource(R.string.settings_format_subtitle),
                    checked = use24HourFormat,
                    onCheckedChange = onFormatChange
                )
                SettingRow(
                    title = stringResource(R.string.settings_seconds_title),
                    subtitle = stringResource(R.string.settings_seconds_subtitle),
                    checked = showSeconds,
                    onCheckedChange = onShowSecondsChange
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(stringResource(R.string.settings_footer), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val titleColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(220),
        label = "settingTitleColor"
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = titleColor)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
