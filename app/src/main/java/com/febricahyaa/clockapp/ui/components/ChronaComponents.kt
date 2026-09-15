package com.febricahyaa.clockapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.model.ThemeAccent
import com.febricahyaa.clockapp.ui.theme.accentGradientColors
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.delay

val LocalAccentGradient = compositionLocalOf { Brush.linearGradient(listOf(Color(0xFFFFC4A0), Color(0xFFFF8B5C))) }

@Composable
fun rememberZonedNow(zoneId: ZoneId = ZoneId.systemDefault()): ZonedDateTime {
    var now by remember(zoneId) { mutableStateOf(ZonedDateTime.now(zoneId)) }
    LaunchedEffect(zoneId) {
        while (true) {
            now = ZonedDateTime.now(zoneId)
            delay(250)
        }
    }
    return now
}

@Composable
fun ChronaCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, glass: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    val color = MaterialTheme.colorScheme.surface.copy(alpha = if (glass) .62f else 1f)
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = color),
        border = if (glass) BorderStroke(1.dp, Color.White.copy(alpha = .11f)) else null,
        elevation = CardDefaults.cardElevation(0.dp),
        content = content
    )
}

@Composable
fun GlassPill(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable RowScope.() -> Unit) {
    val shape = RoundedCornerShape(50)
    Surface(
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = shape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .08f))
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp), content = content)
    }
}

@Composable
fun GradientIconBox(icon: ImageVector, modifier: Modifier = Modifier.size(46.dp), contentDescription: String? = null) {
    Box(modifier.clip(RoundedCornerShape(15.dp)).background(LocalAccentGradient.current), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription, tint = Color(0xFF2A1710), modifier = Modifier.size(21.dp))
    }
}

@Composable
fun IconCircleButton(icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, active: Boolean = false, contentDescription: String? = null) {
    Surface(onClick = onClick, modifier = modifier.size(44.dp), shape = CircleShape,
        color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = .16f) else MaterialTheme.colorScheme.onSurface.copy(alpha = .055f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .08f))) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription, tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String, actions: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
    }
}

@Composable
fun ChronaBackdrop(glass: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(primary.copy(alpha = if (glass) .18f else .035f), size.minDimension * .60f, Offset(size.width * .98f, size.height * .04f))
            drawCircle(secondary.copy(alpha = if (glass) .11f else .022f), size.minDimension * .46f, Offset(-size.width * .10f, size.height * .74f))
            if (glass) drawCircle(Color.White.copy(alpha = .035f), size.minDimension * .28f, Offset(size.width * .43f, size.height * .32f))
        }
    }
}

@Composable
fun SoftWorldMap(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val centerY = size.height * .58f
        val dot = size.minDimension * .008f
        val cols = 23
        val rows = 8
        for (r in 0 until rows) for (c in 0 until cols) {
            val x = size.width * (c + 0.5f) / cols
            val y = centerY + (r - rows / 2f) * size.height * .035f + kotlin.math.sin(c * .7 + r) * size.height * .012f
            val density = when {
                c in 1..7 && r in 1..5 -> .58f
                c in 9..14 && r in 1..5 -> .52f
                c in 15..20 && r in 2..6 -> .44f
                else -> .07f
            }
            drawCircle(MaterialTheme.colorScheme.onSurface.copy(alpha = density * .055f), dot, Offset(x, y))
        }
    }
}

@Composable
fun AnimatedProgress(value: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(value.coerceIn(0f, 1f), label = "progress")
    Box(modifier.height(7.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .075f))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(animated).clip(RoundedCornerShape(50)).background(LocalAccentGradient.current))
    }
}

@Composable
fun AccentSwatch(accent: ThemeAccent, selected: Boolean, onClick: () -> Unit) {
    val colors = accentGradientColors(accent)
    Surface(onClick = onClick, modifier = Modifier.size(42.dp), shape = CircleShape, color = Color.Transparent,
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .10f))) {
        Box(Modifier.fillMaxSize().padding(5.dp).clip(CircleShape).background(Brush.linearGradient(colors)))
    }
}

@Composable fun CloseButton(onClick: () -> Unit) = IconCircleButton(Icons.Filled.Close, onClick, contentDescription = "Close")
