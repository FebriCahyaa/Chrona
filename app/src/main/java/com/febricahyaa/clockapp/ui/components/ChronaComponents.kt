package com.febricahyaa.clockapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

val LocalAccentGradient = compositionLocalOf {
    Brush.linearGradient(listOf(Color(0xFFFFD1B3), Color(0xFFF39A69)))
}

@Composable
fun rememberZonedNow(zoneId: ZoneId = ZoneId.systemDefault()): ZonedDateTime {
    val lifecycleOwner = LocalLifecycleOwner.current
    val now by produceState(initialValue = ZonedDateTime.now(zoneId), zoneId, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                value = ZonedDateTime.now(zoneId)
                delay((1000L - (System.currentTimeMillis() % 1000L)).coerceAtLeast(16L))
            }
        }
    }
    return now
}

@Composable
fun ChronaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    glass: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    val base = if (glass) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.54f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val cardModifier = modifier.then(
        if (glass) Modifier.border(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
            shape
        ) else Modifier
    )
    val cardColors = CardDefaults.cardColors(containerColor = base)
    if (onClick != null) {
        Card(onClick = onClick, modifier = cardModifier, shape = shape, colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), content = content)
    } else {
        Card(modifier = cardModifier, shape = shape, colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), content = content)
    }
}

@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(50)
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.065f),
        shape = shape,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f), shape)
            .then(onClick?.let { Modifier.clickable { it() } } ?: Modifier)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
fun GradientIconBox(
    icon: ImageVector,
    modifier: Modifier = Modifier.size(42.dp),
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(LocalAccentGradient.current),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = Color(0xFF28160B), modifier = Modifier.size(21.dp))
    }
}

@Composable
fun IconCircleButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    contentDescription: String? = null,
) {
    val alpha = if (active) 0.16f else 0.065f
    Surface(
        onClick = onClick,
        modifier = modifier.size(42.dp),
        shape = CircleShape,
        color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = alpha) else MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription, tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
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
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
    }
}

@Composable
fun ChronaBackdrop(glass: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.tertiary
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(primary.copy(alpha = if (glass) 0.24f else 0.05f), radius = size.minDimension * 0.62f,
                center = androidx.compose.ui.geometry.Offset(size.width * 1.03f, size.height * 0.03f))
            drawCircle(secondary.copy(alpha = if (glass) 0.17f else 0.03f), radius = size.minDimension * 0.46f,
                center = androidx.compose.ui.geometry.Offset(-size.width * 0.05f, size.height * 0.70f))
            if (glass) {
                drawCircle(Color.White.copy(alpha = 0.05f), radius = size.minDimension * 0.30f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.33f))
            }
        }
    }
}

@Composable
fun AnimatedProgress(value: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(value.coerceIn(0f, 1f), label = "progress")
    Box(modifier.height(5.dp).clip(RoundedCornerShape(5.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .09f))) {
        Box(Modifier.fillMaxSize().fillMaxWidth(animated).clip(RoundedCornerShape(5.dp)).background(LocalAccentGradient.current))
    }
}

@Composable
fun CloseButton(onClick: () -> Unit) {
    IconCircleButton(Icons.Filled.Close, onClick, contentDescription = "Close")
}
