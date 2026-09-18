/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.time.ChronaTimeFormatter
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.ChronaScaffold
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.rememberEpochMillisNowState
import java.time.Instant
import java.time.ZoneId

@Composable
fun WorldClockDetailScreen(
    item: WorldClockItem,
    favorite: Boolean,
    use24HourFormat: Boolean,
    glass: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
) {
    val epochMillisState = rememberEpochMillisNowState()
    val epochMillis by epochMillisState
    val zoneId = remember(item.zoneId) { runCatching { ZoneId.of(item.zoneId) }.getOrNull() }

    ChronaScaffold(
        title = item.city,
        subtitle = "${stringResource(countryOfDetail(item.zoneId))} · ${item.zoneId}",
        onBack = onBack,
    ) { paddingValues ->
        if (zoneId == null) {
            ChronaCard(
                modifier = Modifier.fillMaxWidth().padding(paddingValues).padding(20.dp),
                glass = glass,
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(stringResource(R.string.world_invalid_timezone), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        item.zoneId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            return@ChronaScaffold
        }

        val zoned = remember(zoneId, epochMillis) { Instant.ofEpochMilli(epochMillis).atZone(zoneId) }
        val systemZone = remember { ZoneId.systemDefault() }
        val localOffsetSeconds = remember(epochMillis, systemZone) {
            Instant.ofEpochMilli(epochMillis).atZone(systemZone).offset.totalSeconds
        }
        val time = remember(zoned, use24HourFormat) {
            ChronaTimeFormatter.shortTime(epochMillis, zoneId, use24HourFormat)
        }
        val date = remember(zoned) { ChronaTimeFormatter.date(epochMillis, zoneId) }
        val utc = remember(zoned) { ChronaTimeFormatter.utcOffset(zoneId, epochMillis) }
        val delta = formatOffsetDeltaDetail(zoned.offset.totalSeconds - localOffsetSeconds)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ChronaCard(
                modifier = Modifier
                    .fillMaxWidth(),
                glass = glass,
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CityThumbnail(item.city, Modifier.size(96.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.city, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                stringResource(countryOfDetail(item.zoneId)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconCircleButton(
                            icon = if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            onClick = onToggleFavorite,
                            active = favorite,
                            contentDescription = if (favorite) stringResource(R.string.world_remove_favorite, item.city) else stringResource(R.string.world_add_favorite, item.city),
                        )
                    }

                    Spacer(Modifier.height(26.dp))
                    Text(time, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.ExtraLight)
                    Text(date, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(utc, style = MaterialTheme.typography.labelLarge)
                        Text("·", color = MaterialTheme.colorScheme.outline)
                        Text(delta, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(stringResource(R.string.world_timezone_details), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Text(item.zoneId, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.world_detail_update_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
                        Text(if (favorite) stringResource(R.string.world_remove_from_favorites) else stringResource(R.string.world_add_to_favorites))
                    }
                }
            }
        }
    }
}

@Composable
private fun formatOffsetDeltaDetail(totalSeconds: Int): String {
    if (totalSeconds == 0) return stringResource(R.string.world_delta_same_time)
    val sign = if (totalSeconds > 0) "+" else "-"
    val absolute = kotlin.math.abs(totalSeconds)
    val hours = absolute / 3_600
    val minutes = (absolute % 3_600) / 60
    return buildString {
        append(sign)
        if (hours > 0) append(stringResource(R.string.world_delta_hours, hours))
        if (minutes > 0) {
            if (hours > 0) append(' ')
            append(stringResource(R.string.world_delta_minutes, minutes))
        }
    }
}

@StringRes
private fun countryOfDetail(zoneId: String): Int = when {
    zoneId == "Asia/Jakarta" || zoneId == "Asia/Makassar" || zoneId == "Asia/Jayapura" -> R.string.country_indonesia
    zoneId == "Asia/Singapore" -> R.string.country_singapore
    zoneId == "Asia/Kuala_Lumpur" -> R.string.country_malaysia
    zoneId == "Asia/Bangkok" -> R.string.country_thailand
    zoneId == "Asia/Tokyo" -> R.string.country_japan
    zoneId == "Asia/Seoul" -> R.string.country_south_korea
    zoneId == "Asia/Shanghai" || zoneId == "Asia/Hong_Kong" -> R.string.country_china
    zoneId == "Asia/Kolkata" -> R.string.country_india
    zoneId == "Asia/Dubai" -> R.string.country_united_arab_emirates
    zoneId == "Europe/London" -> R.string.country_united_kingdom
    zoneId == "Europe/Paris" -> R.string.country_france
    zoneId == "Europe/Berlin" -> R.string.country_germany
    zoneId == "Europe/Moscow" -> R.string.country_russia
    zoneId == "America/Sao_Paulo" -> R.string.country_brazil
    zoneId == "America/Toronto" -> R.string.country_canada
    zoneId.startsWith("America/") -> R.string.country_united_states
    zoneId == "Australia/Sydney" -> R.string.country_australia
    zoneId == "Pacific/Auckland" -> R.string.country_new_zealand
    zoneId == "Africa/Cairo" -> R.string.country_egypt
    zoneId == "Africa/Johannesburg" -> R.string.country_south_africa
    else -> R.string.world_region_other
}
