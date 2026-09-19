/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.update

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.data.update.AppUpdateSnapshot
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronaReleaseTimeline(
    snapshot: AppUpdateSnapshot,
    onDismiss: () -> Unit,
    onOpenExternal: () -> Unit,
) {
    val blocks = remember(snapshot.releaseNotes) {
        ChronaReleaseMarkdownParser.parse(snapshot.releaseNotes.orEmpty())
    }
    val publishedLabel = remember(snapshot.publishedAt) {
        snapshot.publishedAt?.let { value ->
            runCatching {
                Instant.parse(value)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
            }.getOrNull()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.release_timeline_title), fontSize = 27.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        snapshot.releaseName ?: stringResource(R.string.release_timeline_default_name),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (publishedLabel != null) {
                        Text(
                            stringResource(R.string.release_timeline_published, publishedLabel),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.nav_close))
                }
            }

            Spacer(Modifier.height(16.dp))
            ReleaseTimelineContent(blocks)

            Spacer(Modifier.height(18.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    onClick = onOpenExternal,
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.OpenInNew, null, Modifier.size(17.dp))
                        Spacer(Modifier.size(7.dp))
                        Text(stringResource(R.string.release_timeline_open_github), fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReleaseTimelineContent(blocks: List<ChronaReleaseBlock>) {
    if (blocks.isEmpty()) {
        Surface(
            Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
        ) {
            Row(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.SystemUpdate, null, Modifier.size(20.dp))
                Text(stringResource(R.string.release_timeline_empty), fontSize = 12.sp)
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        blocks.forEachIndexed { index, block ->
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(20.dp),
                ) {
                    Icon(
                        imageVector = if (block is ChronaReleaseBlock.Heading) Icons.Filled.CheckCircle else Icons.Filled.SystemUpdate,
                        contentDescription = null,
                        modifier = Modifier.size(if (block is ChronaReleaseBlock.Heading) 17.dp else 13.dp),
                        tint = if (block is ChronaReleaseBlock.Heading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (index != blocks.lastIndex) {
                        androidx.compose.foundation.layout.Spacer(
                            Modifier
                                .height(38.dp)
                                .width(1.dp),
                        )
                    }
                }
                Spacer(Modifier.size(10.dp))
                when (block) {
                    is ChronaReleaseBlock.Heading -> Text(
                        block.text,
                        fontSize = when (block.level) { 1 -> 17.sp; 2 -> 14.sp; else -> 13.sp },
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    is ChronaReleaseBlock.Bullet -> Row(
                        Modifier.padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Text(if (block.checked == true) "✓" else "•", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text(block.text, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                    is ChronaReleaseBlock.Numbered -> Row(
                        Modifier.padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Text("${block.number}.", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text(block.text, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                    is ChronaReleaseBlock.Paragraph -> Text(
                        block.text,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    is ChronaReleaseBlock.Code -> Surface(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Text(
                            block.text,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }
        }
    }
}
