package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.ui.theme.ThemeEngine

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    use24HourFormat: Boolean,
    onFormatChange: (Boolean) -> Unit,
    showSeconds: Boolean,
    onShowSecondsChange: (Boolean) -> Unit,
    themeAccent: ThemeAccent,
    onThemeAccentChange: (ThemeAccent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.settings_screen_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.settings_screen_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        ) {
            Column {
                SettingRow(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(R.string.settings_dark_theme_title),
                    subtitle = stringResource(R.string.settings_dark_theme_subtitle),
                    checked = isDarkTheme,
                    onCheckedChange = onThemeChanged
                )
                RowDivider()
                SettingRow(
                    icon = Icons.Default.Schedule,
                    title = stringResource(R.string.settings_format_title),
                    subtitle = stringResource(R.string.settings_format_subtitle),
                    checked = use24HourFormat,
                    onCheckedChange = onFormatChange
                )
                RowDivider()
                SettingRow(
                    icon = Icons.Default.Timer,
                    title = stringResource(R.string.settings_seconds_title),
                    subtitle = stringResource(R.string.settings_seconds_subtitle),
                    checked = showSeconds,
                    onCheckedChange = onShowSecondsChange
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(R.string.theme_studio_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.theme_studio_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeAccent.entries.chunked(2).forEach { rowAccents ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowAccents.forEach { accent ->
                        AccentThemeCard(
                            accent = accent,
                            selected = accent == themeAccent,
                            modifier = Modifier.weight(1f),
                            onClick = { onThemeAccentChange(accent) }
                        )
                    }
                    // Odd item count: keep the last card from stretching to full width.
                    if (rowAccents.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.settings_footer_title), fontWeight = FontWeight.Medium)
                Text(
                    stringResource(R.string.settings_footer_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val titleColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(220),
        label = "settingTitleColor"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = titleColor)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * One selectable accent card in the Theme Studio grid — a swatch icon,
 * preset name, and a short color description, styled after the reference
 * mock (rounded card, tinted background, checkmark badge when selected).
 */
@Composable
private fun AccentThemeCard(
    accent: ThemeAccent,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val swatchColor = ThemeEngine.seedColorFor(accent) ?: MaterialTheme.colorScheme.primary
    val onSwatchColor = if (swatchColor.luminance() > 0.5f) Color.Black else Color.White

    val cardBackground by animateColorAsState(
        targetValue = swatchColor.copy(alpha = if (selected) 0.22f else 0.12f),
        animationSpec = tween(180),
        label = "accentCardBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) swatchColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        animationSpec = tween(180),
        label = "accentCardBorder"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(cardBackground)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(swatchColor),
                contentAlignment = Alignment.Center
            ) {
                if (accent == ThemeAccent.SYSTEM) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = onSwatchColor)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(accent.labelRes()),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(accent.descriptionRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(swatchColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = onSwatchColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun ThemeAccent.labelRes(): Int = when (this) {
    ThemeAccent.SYSTEM -> R.string.theme_accent_dynamic
    ThemeAccent.INDIGO -> R.string.theme_accent_indigo
    ThemeAccent.OCEAN -> R.string.theme_accent_ocean
    ThemeAccent.EMERALD -> R.string.theme_accent_emerald
    ThemeAccent.SUNSET -> R.string.theme_accent_sunset
    ThemeAccent.ROSE -> R.string.theme_accent_rose
    ThemeAccent.SLATE -> R.string.theme_accent_slate
}

private fun ThemeAccent.descriptionRes(): Int = when (this) {
    ThemeAccent.SYSTEM -> R.string.theme_accent_dynamic_desc
    ThemeAccent.INDIGO -> R.string.theme_accent_indigo_desc
    ThemeAccent.OCEAN -> R.string.theme_accent_ocean_desc
    ThemeAccent.EMERALD -> R.string.theme_accent_emerald_desc
    ThemeAccent.SUNSET -> R.string.theme_accent_sunset_desc
    ThemeAccent.ROSE -> R.string.theme_accent_rose_desc
    ThemeAccent.SLATE -> R.string.theme_accent_slate_desc
}
