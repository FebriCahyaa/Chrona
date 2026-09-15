package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.delay

/** Accent gradient (peach default) provided by ClockApp's theme wrapper. */
val LocalAccentGradient = compositionLocalOf {
    Brush.linearGradient(listOf(Color(0xFFF6A56F), Color(0xFFE2794E)))
}

/** Ticking zoned clock as Compose state (1s resolution). */
@Composable
fun rememberZonedNow(zoneId: ZoneId = ZoneId.systemDefault()): ZonedDateTime {
    val now by produceState(initialValue = ZonedDateTime.now(zoneId), zoneId) {
        while (true) {
            delay(1_000)
            value = ZonedDateTime.now(zoneId)
        }
    }
    return now
}

/** Chrona card: rounded 22dp, flat surface, optional click. */
@Composable
fun ChronaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors,
            elevation = CardDefaults.cardElevation(0.dp), content = content)
    } else {
        Card(modifier = modifier, shape = shape, colors = colors,
            elevation = CardDefaults.cardElevation(0.dp), content = content)
    }
}

@Composable
fun GradientIconBox(
    icon: ImageVector,
    modifier: Modifier = Modifier.size(34.dp),
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(LocalAccentGradient.current),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color(0xFF1A120B))
    }
}

@Composable
fun GradientFab(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
        modifier = Modifier
            .size(72.dp)
            .background(LocalAccentGradient.current)
    ) {
        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF1A120B), modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}
