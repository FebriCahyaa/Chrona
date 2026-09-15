package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.navigation.AppDestination

private fun iconFor(dest: AppDestination): ImageVector = when (dest) {
    AppDestination.CLOCK -> Icons.Filled.Home
    AppDestination.WORLD -> Icons.Filled.Public
    AppDestination.TIMER -> Icons.Filled.HourglassTop
    AppDestination.STOPWATCH -> Icons.Filled.AvTimer
    AppDestination.ALARM -> Icons.Filled.Home
}

@Composable
fun FloatingNavigationBar(
    selected: AppDestination,
    onSelected: (AppDestination) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = .78f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .08f)),
        shadowElevation = 10.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppDestination.entries.filter { it.inBottomBar }.forEach { dest ->
                val active = dest == selected
                Surface(
                    onClick = { onSelected(dest) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = .14f) else Color.Transparent
                ) {
                    Row(
                        Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(iconFor(dest), contentDescription = null, modifier = Modifier.size(19.dp),
                            tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        if (active) Text(dest.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
