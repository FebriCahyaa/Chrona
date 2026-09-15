package com.febricahyaa.clockapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.awaitEachGesture
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.navigation.AppDestination
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A floating tab bar that behaves like an iOS segmented control: tap a tab
 * to jump to it, or press-and-drag horizontally across the bar and the
 * highlight pill follows your finger in real time (no rebuild/popup
 * animation per tab — just one continuous drag), snapping to the nearest
 * tab with a small spring when you let go.
 */
@Composable
fun FloatingNavigationBar(
    selected: AppDestination,
    onSelected: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = AppDestination.entries
    val density = LocalDensity.current

    var contentWidthPx by remember { mutableFloatStateOf(0f) }
    val itemWidthPx by remember(contentWidthPx) {
        derivedStateOf { if (contentWidthPx > 0f) contentWidthPx / items.size else 0f }
    }

    // Settled position (animates with a spring when not being dragged).
    val settledX = remember { Animatable(0f) }
    // Live position while the finger is down — plain state so it can be
    // updated synchronously from the (non-suspend) drag callback.
    var liveDragX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(selected, itemWidthPx) {
        if (!isDragging && itemWidthPx > 0f) {
            settledX.animateTo(
                targetValue = items.indexOf(selected) * itemWidthPx,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    val indicatorX = if (isDragging) liveDragX else settledX.value
    val displayedIndex = if (itemWidthPx > 0f) {
        (indicatorX / itemWidthPx).roundToInt().coerceIn(0, items.lastIndex)
    } else 0
    val displaySelected = if (isDragging) items[displayedIndex] else selected

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f),
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth()
                .onSizeChanged { contentWidthPx = it.width.toFloat() }
                .pointerInput(itemWidthPx, items.size) {
                    if (itemWidthPx <= 0f) return@pointerInput
                    val maxX = itemWidthPx * (items.size - 1)
                    val slop = viewConfiguration.touchSlop
                    // Single gesture recognizer for both tap-to-select and
                    // drag-to-follow. Handling both cases here (instead of
                    // splitting tap into each item's own clickable) avoids
                    // a race where a plain tap fires the item's onClick
                    // *and* this detector's onDragEnd with a stale target,
                    // which used to make the pill/label flick back to the
                    // previous tab before landing on the tapped one.
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downX = down.position.x
                        var accumulatedDrag = 0f
                        var dragging = false
                        liveDragX = settledX.value

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (change.changedToUpIgnoreConsumed()) break

                            val deltaX = change.positionChange().x
                            if (!dragging) {
                                accumulatedDrag += deltaX
                                if (abs(accumulatedDrag) > slop) {
                                    dragging = true
                                    isDragging = true
                                }
                            }
                            if (dragging) {
                                liveDragX = (liveDragX + deltaX).coerceIn(0f, maxX)
                                change.consume()
                            }
                        }

                        isDragging = false
                        val targetIndex = if (dragging) {
                            (liveDragX / itemWidthPx).roundToInt().coerceIn(0, items.lastIndex)
                        } else {
                            (downX / itemWidthPx).toInt().coerceIn(0, items.lastIndex)
                        }
                        onSelected(items[targetIndex])
                    }
                }
        ) {
            // Sliding highlight pill, drawn behind the icons/labels.
            if (itemWidthPx > 0f) {
                val itemWidthDp = with(density) { itemWidthPx.toDp() }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(itemWidthDp)
                        .offset { IntOffset(indicatorX.roundToInt(), 0) }
                        .padding(2.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { destination ->
                    val active = destination == displaySelected
                    val icon = when (destination) {
                        AppDestination.ALARM -> Icons.Default.Alarm
                        AppDestination.CLOCK -> Icons.Default.Public
                        AppDestination.TIMER -> Icons.Default.Timer
                        AppDestination.STOPWATCH -> Icons.Default.AvTimer
                    }
                    NavigationItem(
                        label = stringResource(destination.labelRes),
                        icon = icon,
                        active = active,
                        onClick = { onSelected(destination) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationItem(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180),
        label = "navItemContent"
    )
    val scale by animateFloatAsState(
        targetValue = if (active) 1.06f else 1f,
        animationSpec = tween(180),
        label = "navItemScale"
    )

    // Selection itself is driven entirely by the parent's unified pointer
    // gesture (see FloatingNavigationBar's pointerInput) so a raw touch
    // never triggers this and races with the drag detector. `onClick` is
    // only exposed to accessibility services (TalkBack "double tap to
    // activate"), which invoke it directly without going through touch
    // dispatch, so there's no conflict.
    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .semantics {
                role = Role.Tab
                onClick {
                    onClick()
                    true
                }
            }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = contentColor)
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = contentColor)
    }
}
