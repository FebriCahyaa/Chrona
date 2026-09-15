package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.core.ChronaTimeEngine
import com.febricahyaa.clockapp.model.TimeZoneCatalog
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.ui.components.ChronaCard
import com.febricahyaa.clockapp.ui.components.IconCircleButton
import com.febricahyaa.clockapp.ui.components.ScreenHeader
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun WorldClockScreen(
    clocks: List<WorldClockItem>, favorites: Set<String>, glass: Boolean,
    onAdd: (WorldClockItem) -> Unit, onRemove: (WorldClockItem) -> Unit, onToggleFavorite: (String) -> Unit
) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var favoriteOnly by rememberSaveable { mutableStateOf(false) }
    val shown = if (favoriteOnly) clocks.filter { it.city in favorites } else clocks
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 12.dp)) {
        ScreenHeader("World clock", "Your day, across every time zone", actions = {
            IconCircleButton(Icons.Filled.Add, { showAdd = true }, active = true, contentDescription = "Add city")
        })
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !favoriteOnly, onClick = { favoriteOnly = false }, label = { Text("All") })
            FilterChip(selected = favoriteOnly, onClick = { favoriteOnly = true }, label = { Text("Favorites") })
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(shown, key = { it.id }) { city ->
                WorldCityCard(city, city.city in favorites, glass, onRemove = { onRemove(city) }, onToggleFavorite = { onToggleFavorite(city.city) })
            }
            if (shown.isEmpty()) item { Text("No favorite cities yet", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(24.dp)) }
        }
    }
    if (showAdd) AddCityDialog(clocks.map { it.zoneId }.toSet(), onDismiss = { showAdd = false }, onPick = { entry -> onAdd(WorldClockItem(System.currentTimeMillis(), entry.city, entry.zoneId)); showAdd = false })
}

@Composable
private fun WorldCityCard(city: WorldClockItem, favorite: Boolean, glass: Boolean, onRemove: () -> Unit, onToggleFavorite: () -> Unit) {
    val zone = ZoneId.of(city.zoneId); val now = ZonedDateTime.now(zone); val epoch = now.toInstant().toEpochMilli()
    ChronaCard(Modifier.fillMaxWidth(), glass = glass) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            CityThumbnail(city.city, Modifier.size(74.dp, 74.dp))
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(city.city, style = MaterialTheme.typography.titleMedium)
                Text(city.zoneId.substringBefore('/'), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(7.dp))
                Text(ChronaTimeEngine.shortTime(epoch, zone, true), style = MaterialTheme.typography.headlineMedium)
                Text("${ChronaTimeEngine.date(epoch, zone)}  ·  ${ChronaTimeEngine.utcOffset(zone, epoch)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onToggleFavorite) { Icon(if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = onRemove) { Icon(Icons.Filled.DeleteOutline, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable private fun CityThumbnail(city: String, modifier: Modifier = Modifier) {
    val seed = (kotlin.math.abs(city.hashCode()) % 360).toFloat()
    val a = Color.hsv(seed, .30f, .84f); val b = Color.hsv((seed + 35f) % 360f, .45f, .45f)
    Box(modifier.clip(RoundedCornerShape(18.dp)).background(Brush.verticalGradient(listOf(a, b)))) {
        Canvas(Modifier.fillMaxSize()) {
            val base = size.height * .82f
            drawCircle(Color.White.copy(alpha = .65f), size.minDimension * .08f, androidx.compose.ui.geometry.Offset(size.width * .73f, size.height * .25f))
            var x = 0f
            repeat(7) { i -> val w = size.width * (.10f + (i % 3) * .035f); val h = size.height * (.20f + ((i + city.length) % 4) * .12f); drawRect(Color.Black.copy(alpha = .23f), androidx.compose.ui.geometry.Offset(x, base-h), androidx.compose.ui.geometry.Size(w,h)); x += w + size.width*.045f }
        }
    }
}

@Composable private fun AddCityDialog(alreadyAdded: Set<String>, onDismiss: () -> Unit, onPick: (TimeZoneCatalog.Entry) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = TimeZoneCatalog.entries.filter { it.zoneId !in alreadyAdded && (query.isBlank() || it.city.contains(query, true) || it.country.contains(query, true)) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add city") }, text = {
        Column { OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.Search, null) }, placeholder = { Text("Search city or country") }); Spacer(Modifier.height(8.dp)); LazyColumn(Modifier.height(310.dp)) { items(filtered) { entry -> TextButton(onClick = { onPick(entry) }, modifier = Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) { Text(entry.city); Text(entry.country + "  ·  " + entry.zoneId, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } } }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}
