package com.febricahyaa.clockapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.model.TimeZoneCatalog
import com.febricahyaa.clockapp.model.WorldClockItem
import com.febricahyaa.clockapp.ui.components.ClockDisplay
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * World Clock screen: a hero card with the device's local time, plus a list
 * of saved cities in other time zones that the user can add to or remove
 * from.
 */
@Composable
fun ClockScreen(
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    worldClocks: List<WorldClockItem>,
    onAddCity: (WorldClockItem) -> Unit,
    onRemoveCity: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                        )
                    )
                    .padding(vertical = 26.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.clock_screen_title),
                        color = Color.White.copy(alpha = .78f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    ClockDisplay(
                        use24HourFormat = use24HourFormat,
                        showSeconds = showSeconds,
                        lightContent = true
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.clock_world_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${worldClocks.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (worldClocks.isEmpty()) {
                Text(
                    stringResource(R.string.clock_world_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(worldClocks, key = { it.id }) { item ->
                        WorldClockRow(
                            item = item,
                            use24HourFormat = use24HourFormat,
                            onRemove = { onRemoveCity(item.id) }
                        )
                    }
                    item { Spacer(Modifier.size(88.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.clock_add_city_action))
        }
    }

    if (showAddDialog) {
        AddCityDialog(
            alreadyAdded = worldClocks.map { it.zoneId }.toSet(),
            onDismiss = { showAddDialog = false },
            onPick = { entry ->
                onAddCity(
                    WorldClockItem(
                        id = System.currentTimeMillis(),
                        city = entry.city,
                        zoneId = entry.zoneId
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun WorldClockRow(
    item: WorldClockItem,
    use24HourFormat: Boolean,
    onRemove: () -> Unit
) {
    var now by remember { mutableStateOf(ZonedDateTime.now(ZoneId.of(item.zoneId))) }
    LaunchedEffect(item.zoneId) {
        while (true) {
            now = ZonedDateTime.now(ZoneId.of(item.zoneId))
            delay(1_000)
        }
    }
    val timePattern = if (use24HourFormat) "HH:mm" else "hh:mm a"
    val dayOffset = now.toLocalDate().compareTo(ZonedDateTime.now().toLocalDate())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.city, fontWeight = FontWeight.SemiBold)
                Text(
                    text = when {
                        dayOffset > 0 -> stringResource(R.string.clock_world_tomorrow)
                        dayOffset < 0 -> stringResource(R.string.clock_world_yesterday)
                        else -> item.zoneId.substringAfter('/').replace('_', ' ')
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = now.format(DateTimeFormatter.ofPattern(timePattern)),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clock_remove_city_action))
            }
        }
    }
}

@Composable
private fun AddCityDialog(
    alreadyAdded: Set<String>,
    onDismiss: () -> Unit,
    onPick: (TimeZoneCatalog.Entry) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        TimeZoneCatalog.entries.filter {
            query.isBlank() ||
                it.city.contains(query, ignoreCase = true) ||
                it.country.contains(query, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.clock_add_city_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(R.string.clock_add_city_search_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filtered) { entry ->
                        val alreadyPicked = entry.zoneId in alreadyAdded
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                                    .then(
                                        if (!alreadyPicked) Modifier.clickable { onPick(entry) } else Modifier
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.city, fontWeight = FontWeight.Medium)
                                    Text(
                                        entry.country,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (alreadyPicked) {
                                    Text(
                                        stringResource(R.string.clock_add_city_added),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.clock_add_city_close)) }
        }
    )
}


