/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import android.graphics.RectF
import android.graphics.Region
import com.febricahyaa.clockapp.R
import androidx.compose.ui.input.key.type
import kotlin.math.roundToInt

private data class RuntimeWorldMapFeature(
    val data: WorldMapFeatureData,
    val composePath: Path,
    val region: Region,
    val bounds: RectF,
)

@Composable
fun WorldClockMap(
    modifier: Modifier = Modifier,
    utcHour: Int,
    onUtcHourChange: (Int) -> Unit,
) {
    val mapShape = RoundedCornerShape(22.dp)
    val background = MaterialTheme.colorScheme.surfaceVariant
    val land = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val landStroke = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    val crossed = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f)
    val selected = MaterialTheme.colorScheme.primary.copy(alpha = 0.54f)
    val accent = MaterialTheme.colorScheme.error
    val ocean = MaterialTheme.colorScheme.surface
    val captionColor = MaterialTheme.colorScheme.onSurface

    val features = remember {
        val clip = Region(
            0,
            0,
            WORLD_MAP_WIDTH.roundToInt(),
            WORLD_MAP_HEIGHT.roundToInt(),
        )
        WORLD_MAP_FEATURES.mapNotNull { data ->
            val androidPath = runCatching {
                PathParser.createPathFromPathData(data.pathData)
            }.getOrNull() ?: return@mapNotNull null
            val bounds = RectF()
            androidPath.computeBounds(bounds, true)
            val region = Region()
            region.setPath(androidPath, clip)
            RuntimeWorldMapFeature(
                data = data,
                composePath = androidPath.asComposePath(),
                region = region,
                bounds = bounds,
            )
        }
    }

    var probeY by remember { mutableFloatStateOf(0.31f) }
    val selectedFeatureIndex = remember(utcHour, probeY, features) {
        val x = utcHourToMapX(utcHour)
        val y = probeY * WORLD_MAP_HEIGHT
        features.indexOfFirst {
            it.region.contains(x.roundToInt(), y.roundToInt())
        }
    }
    val utcLabel = formatWorldMapUtc(utcHour)
    val caption = if (selectedFeatureIndex >= 0) {
        features.getOrNull(selectedFeatureIndex)?.data?.name
            ?: stringResource(R.string.world_map_open_water)
    } else {
        stringResource(R.string.world_map_open_water)
    }
    val mapAccessibility = stringResource(
        R.string.world_map_accessibility,
        utcLabel,
        caption,
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(mapShape),
    ) {
        val tagWidth = 64.dp
        val tagX = ((maxWidth - tagWidth) * ((utcHour + 12) / 24f)).coerceIn(0.dp, maxWidth - tagWidth)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(WORLD_MAP_WIDTH / WORLD_MAP_HEIGHT)
                .onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                    when (event.key) {
                        Key.DirectionLeft -> {
                            onUtcHourChange((utcHour - 1).coerceIn(-12, 12))
                            true
                        }
                        Key.DirectionRight -> {
                            onUtcHourChange((utcHour + 1).coerceIn(-12, 12))
                            true
                        }
                        Key.MoveHome -> {
                            onUtcHourChange(-12)
                            true
                        }
                        Key.MoveEnd -> {
                            onUtcHourChange(12)
                            true
                        }
                        else -> false
                    }
                }
                .focusable()
                .semantics {
                    contentDescription = mapAccessibility
                }
                .pointerInput(features) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val mapPoint = offset.toMapPoint(size.width.toFloat(), size.height.toFloat())
                            onUtcHourChange(mapPoint.toUtcHour())
                            probeY = (mapPoint.y / WORLD_MAP_HEIGHT).coerceIn(0f, 1f)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val next = change.position.toMapPoint(size.width.toFloat(), size.height.toFloat())
                            onUtcHourChange(next.toUtcHour())
                            probeY = (next.y / WORLD_MAP_HEIGHT).coerceIn(0f, 1f)
                        },
                    )
                },
        ) {
            drawRect(background)

            val scaleX = size.width / WORLD_MAP_WIDTH
            withTransform({
                scale(scaleX, scaleX, pivot = Offset.Zero)
            }) {
                val strokeWidth = 0.9f / scaleX.coerceAtLeast(0.0001f)
                features.forEachIndexed { index, feature ->
                    val isSelected = index == selectedFeatureIndex
                    val crossesMeridian = feature.bounds.left <= utcHourToMapX(utcHour) &&
                        feature.bounds.right >= utcHourToMapX(utcHour)

                    drawPath(
                        path = feature.composePath,
                        color = when {
                            isSelected -> selected
                            crossesMeridian -> crossed
                            else -> land
                        },
                    )
                    drawPath(
                        path = feature.composePath,
                        color = landStroke,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }
            }

            val x = utcHourToMapX(utcHour) * scaleX
            val y = probeY * size.height
            drawLine(
                color = accent,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2f,
            )
            drawCircle(
                color = ocean,
                radius = 7.dp.toPx(),
                center = Offset(x, y),
            )
            drawCircle(
                color = accent,
                radius = 4.5.dp.toPx(),
                center = Offset(x, y),
            )
        }

        Text(
            text = caption,
            color = captionColor,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(tagX, 0.dp)
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(50))
                .background(accent)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text(
                text = utcLabel,
                color = MaterialTheme.colorScheme.onError,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private fun Offset.toMapPoint(width: Float, height: Float): Offset = Offset(
    x = (x / width * WORLD_MAP_WIDTH).coerceIn(0f, WORLD_MAP_WIDTH),
    y = (y / height * WORLD_MAP_HEIGHT).coerceIn(0f, WORLD_MAP_HEIGHT),
)

private fun Offset.toUtcHour(): Int {
    val longitude = (x / WORLD_MAP_WIDTH) * 360f - 180f
    return (longitude / 15f).roundToInt().coerceIn(-12, 12)
}

private fun utcHourToMapX(hour: Int): Float = ((hour * 15f + 180f) / 360f) * WORLD_MAP_WIDTH

private fun formatWorldMapUtc(hour: Int): String {
    val sign = if (hour < 0) "−" else "+"
    return "UTC $sign${hour.absoluteValue()}"
}

private fun Int.absoluteValue(): Int = if (this < 0) -this else this
