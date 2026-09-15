package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.TimeZoneCatalog
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import com.febricahyaa.clockapp.ui.components.rememberZonedNow
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private fun regionOf(zoneId: String): String = when {
    zoneId.startsWith("Asia/") -> "Asia"
    zoneId.startsWith("Europe/") -> "Europe"
    zoneId.startsWith("America/") -> "Americas"
    zoneId.startsWith("Australia/") || zoneId.startsWith("Pacific/") -> "Oceania"
    else -> "Other"
}

private val CityEmojis = mapOf(
    "New York" to "🗽", "London" to "🎡", "Tokyo" to "🗼", "Paris" to "🗼",
    "Dubai" to "🏙️", "Sydney" to "🌉", "Singapore" to "🏝️", "Los Angeles" to "🌴",
    "Jakarta" to "🏙️", "Moscow" to "🏰", "Berlin" to "🏛️", "Hong Kong" to "🏙️",
)
private val WeatherIcons = listOf("⛅", "☀️", "🌤️", "🌙")

private fun emojiFor(city: String) = CityEmojis[city] ?: "🌍"
private fun mockTemp(city: String) = "${18 + abs(city.hashCode() % 14)}°C"

@Composable
fun WorldClockScreen(
    use24HourFormat: Boolean,
    worldClocks: List<WorldClockItem>,
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onAddCity: (WorldClockItem) -> Unit,
    onRemoveCity: (Long) -> Unit,
) {
    var filter by rememberSaveable { mutableStateOf("All") }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    val timePattern = if (use24HourFormat) "HH:mm" else "hh:mm a"

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        ScreenHeader("World Clock", "Different places, same moment", actions = {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add city")
            }
        })
        Spacer(Modifier.height(12.dp))

        val regions = listOf("All", "Favorites") + worldClocks.map { regionOf(it.zoneId) }.distinct()
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            regions.forEach { r ->
                FilterChip(selected = filter == r, onClick = { filter = r },
                    label = { Text(r, fontSize = 12.sp) })
            }
        }
        Spacer(Modifier.height(12.dp))

        val shown = when (filter) {
            "All" -> worldClocks
            "Favorites" -> worldClocks.filter { it.city in favorites }
            else -> worldClocks.filter { regionOf(it.zoneId) == filter }
        }
        if (shown.isEmpty()) {
            Text("No cities here yet — tap ＋ to add one.",
                fontSize = 13.sp, color = dim, modifier = Modifier.padding(top = 30.dp))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(shown, key = { it.id }) { item ->
                CityRow(item, timePattern, favorites, onToggleFavorite, onRemoveCity)
            }
            item { Spacer(Modifier.size(8.dp)) }
        }
    }

    if (showAddDialog) {
        AddCityDialog(
            alreadyAdded = worldClocks.map { it.zoneId }.toSet(),
            onDismiss = { showAddDialog = false },
            onPick = { entry ->
                onAddCity(WorldClockItem(System.currentTimeMillis(), entry.city, entry.zoneId))
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CityRow(
    item: WorldClockItem,
    timePattern: String,
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val zone = rememberSaveable(item.zoneId) { ZoneId.of(item.zoneId) }
    val now = rememberZonedNow(zone)
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    val fav = item.city in favorites
    val emoji = emojiFor(item.city)

    ChronaCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) { Text(emoji, fontSize = 22.sp) }
            Column(Modifier.weight(1f)) {
                Text(item.city, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(now.offset.id.replace("GMT", "UTC"), fontSize = 10.sp, color = dim)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(now.format(DateTimeFormatter.ofPattern(timePattern)),
                    fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(now.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)),
                        fontSize = 10.sp, color = dim)
                    Text("${WeatherIcons[abs(item.city.hashCode()) % WeatherIcons.size]} ${mockTemp(item.city)}",
                        fontSize = 10.sp, color = dim)
                }
            }
            IconButton(onClick = { onToggleFavorite(item.city) }) {
                Icon(
                    if (fav) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (fav) MaterialTheme.colorScheme.primary else dim
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = dim)
            }
        }
    }
}

@Composable
private fun AddCityDialog(
    alreadyAdded: Set<String>,
    onDismiss: () -> Unit,
    onPick: (TimeZoneCatalog.Entry) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = query.let { q ->
        TimeZoneCatalog.entries.filter {
            it.zoneId !in alreadyAdded &&
                (q.isBlank() || it.city.contains(q, true) || it.country.contains(q, true))
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add city") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("Search city or country") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(Modifier.size(height = 260.dp, width = 300.dp)) {
                    items(filtered) { entry ->
                        TextButton(onClick = { onPick(entry) }) {
                            Text("${entry.city}, ${entry.country}  ·  ${regionOf(entry.zoneId)}",
                                modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
