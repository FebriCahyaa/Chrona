/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.core.ChronaTimeEngine
import com.febricahyaa.clockapp.model.TimeZoneCatalog
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.GlassPill
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import com.febricahyaa.clockapp.ui.components.rememberZonedNow
import java.time.ZoneId
import kotlin.math.abs

private fun regionOf(zoneId: String): String = when {
    zoneId.startsWith("Asia/") -> "Asia"
    zoneId.startsWith("Europe/") -> "Europe"
    zoneId.startsWith("America/") -> "Americas"
    zoneId.startsWith("Australia/") || zoneId.startsWith("Pacific/") -> "Oceania"
    zoneId.startsWith("Africa/") -> "Africa"
    else -> "Other"
}

@Composable
fun WorldClockScreen(
    items: List<WorldClockItem>,
    favorites: Set<String>,
    use24HourFormat: Boolean,
    glass: Boolean,
    onAdd: (WorldClockItem) -> Unit,
    onRemove: (WorldClockItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onBack: () -> Unit,
) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var region by rememberSaveable { mutableStateOf("All") }
    val visible = items.filter { item -> region == "All" || (region == "Favorites" && item.city in favorites) || regionOf(item.zoneId) == region }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(12.dp))
        ScreenHeader(
            title = "World Clock",
            subtitle = "Different places, same moment",
            onBack = onBack,
            actions = {
            IconCircleButton(Icons.Filled.Add, { showAdd = true }, active = true, contentDescription = "Add city")
        })
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("All", "Favorites", "Asia", "Europe", "Americas", "Oceania", "Africa").forEach { r ->
                GlassPill(selected = region == r, onClick = { region = r }) { Text(r, fontSize = 10.sp) }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (visible.isEmpty()) {
            ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
                Column(Modifier.fillMaxWidth().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No cities here yet", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(5.dp))
                    Text("Add another timezone to build your world.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visible, key = { it.id }) { item ->
                    WorldClockCard(
                        item = item,
                        favorite = favorites.contains(item.city),
                        use24HourFormat = use24HourFormat,
                        glass = glass,
                        onRemove = { onRemove(item) },
                        onToggleFavorite = { onToggleFavorite(item.city) },
                    )
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }
    if (showAdd) {
        AddCityDialog(items.map { it.zoneId }.toSet(), onDismiss = { showAdd = false }) {
            onAdd(WorldClockItem(System.currentTimeMillis(), it.city, it.zoneId))
            showAdd = false
        }
    }
}

@Composable
private fun WorldClockCard(
    item: WorldClockItem,
    favorite: Boolean,
    use24HourFormat: Boolean,
    glass: Boolean,
    onRemove: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val zoneId = runCatching { ZoneId.of(item.zoneId) }.getOrNull()
    if (zoneId == null) {
        ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
            Column(Modifier.fillMaxWidth().padding(13.dp)) {
                Text(item.city, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text(
                    "Invalid timezone",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    item.zoneId,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    val now = rememberZonedNow(zoneId)
    val localNow = rememberZonedNow()
    val epochMillis = now.toInstant().toEpochMilli()
    val time = ChronaTimeEngine.shortTime(epochMillis, now.zone, use24HourFormat)
    val date = ChronaTimeEngine.date(epochMillis, now.zone)
    val deltaSeconds = now.offset.totalSeconds - localNow.offset.totalSeconds
    val delta = formatOffsetDelta(deltaSeconds)
    ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            CityThumbnail(item.city, Modifier.size(62.dp))
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.city, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(countryOf(item.zoneId), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(ChronaTimeEngine.utcOffset(now.zone, epochMillis), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(time, fontSize = 24.sp, fontWeight = FontWeight.Light)
                Text(date, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(delta, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
            }
            IconCircleButton(
                if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                onToggleFavorite,
                modifier = Modifier.padding(start = 6.dp),
                active = favorite,
                contentDescription = "Favorite",
            )
        }
    }
}

private fun formatOffsetDelta(totalSeconds: Int): String {
    if (totalSeconds == 0) return "Same time"
    val sign = if (totalSeconds > 0) "+" else "-"
    val absolute = kotlin.math.abs(totalSeconds)
    val hours = absolute / 3_600
    val minutes = (absolute % 3_600) / 60
    return buildString {
        append(sign)
        if (hours > 0) append(hours).append("h")
        if (minutes > 0) {
            if (hours > 0) append(" ")
            append(minutes).append("m")
        }
    }
}

private fun countryOf(zoneId: String) = when {
    zoneId.contains("New_York") || zoneId.contains("Los_Angeles") || zoneId.contains("Chicago") -> "United States"
    zoneId == "Europe/London" -> "United Kingdom"
    zoneId == "Asia/Dubai" -> "United Arab Emirates"
    zoneId == "Asia/Tokyo" -> "Japan"
    zoneId == "Australia/Sydney" -> "Australia"
    zoneId.startsWith("Asia/") -> "Asia"
    zoneId.startsWith("Europe/") -> "Europe"
    else -> "World"
}

/**
 * Returns deterministic, Compose-safe HSV hues for a city thumbnail.
 *
 * floorMod is intentionally used instead of abs/hash % 360 because
 * abs(Int.MIN_VALUE) remains negative and would otherwise make Color.hsv
 * throw during composition. The returned range is always [0, 360).
 */
internal fun cityThumbnailHues(cityHash: Int): Pair<Float, Float> {
    val seed = Math.floorMod(cityHash, 360)
    val bottom = Math.floorMod(seed + 34, 360)
    return seed.toFloat() to bottom.toFloat()
}

@Composable
private fun CityThumbnail(city: String, modifier: Modifier = Modifier) {
    val (topHue, bottomHue) = cityThumbnailHues(city.hashCode())
    val top = Color.hsv(topHue, .34f, .82f)
    val bottom = Color.hsv(bottomHue, .48f, .48f)
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(Brush.verticalGradient(listOf(top, bottom)))) {
        Canvas(Modifier.fillMaxSize()) {
            val base = size.height * .82f
            val path = Path().apply { moveTo(0f, base); lineTo(size.width, base); lineTo(size.width, size.height); lineTo(0f, size.height); close() }
            drawPath(path, Color.Black.copy(alpha = .25f))
            val widths = listOf(.14f,.12f,.18f,.10f,.17f,.13f)
            var x = size.width * .02f
            widths.forEachIndexed { i, w ->
                val h = size.height * (.20f + ((i + city.length) % 4) * .11f)
                drawRect(Color.Black.copy(alpha = .50f), topLeft = Offset(x, base - h), size = androidx.compose.ui.geometry.Size(size.width * w, h))
                x += size.width * (w + .045f)
            }
            drawCircle(Color.White.copy(alpha = .55f), radius = size.minDimension * .045f, center = Offset(size.width * .78f, size.height * .20f))
        }
    }
}

@Composable
private fun AddCityDialog(alreadyAdded: Set<String>, onDismiss: () -> Unit, onPick: (TimeZoneCatalog.Entry) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = TimeZoneCatalog.entries.filter { it.zoneId !in alreadyAdded && (query.isBlank() || it.city.contains(query, true) || it.country.contains(query, true)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add city") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search city or country") })
                LazyColumn(Modifier.height(280.dp)) {
                    items(filtered) { entry ->
                        TextButton(onClick = { onPick(entry) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(entry.city, fontWeight = FontWeight.Medium)
                                Text("${entry.country} • ${regionOf(entry.zoneId)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
