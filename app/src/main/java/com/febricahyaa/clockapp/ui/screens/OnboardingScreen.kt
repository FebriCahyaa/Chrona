/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.activity.compose.BackHandler
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.febricahyaa.clockapp.ui.components.ChronaAmbientBackdrop

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pages = remember {
        listOf(
            OnboardingPage(
                Icons.Filled.AccessTime,
                "Time, your way.",
                "Chrona keeps your local time, alarms, timer, and stopwatch together in one calm workspace.",
            ),
            OnboardingPage(
                Icons.Filled.Public,
                "See time anywhere.",
                "Add cities to World Clock and keep their local time and day relationship visible at a glance.",
            ),
            OnboardingPage(
                Icons.Filled.SystemUpdate,
                "Stay up to date.",
                "Chrona can check the public GitHub Releases feed in the background. You stay in control of when you install an update.",
            ),
        )
    }
    var page by remember { mutableIntStateOf(0) }
    val current = pages[page]

    BackHandler {
        if (page > 0) page--
    }

    Box(Modifier.fillMaxSize()) {
        ChronaAmbientBackdrop()
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.8f))

            Surface(
                modifier = Modifier.size(88.dp),
                shape = RoundedCornerShape(30.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(current.icon, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(Modifier.height(28.dp))
            AnimatedContent(
                targetState = current,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding-content",
            ) { pageData ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        pageData.title,
                        fontSize = 34.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        pageData.body,
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                pages.indices.forEach { index ->
                    Surface(
                        Modifier.size(if (index == page) 24.dp else 8.dp, 8.dp),
                        shape = RoundedCornerShape(50),
                        color = if (index == page) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ) {}
                }
            }

            Spacer(Modifier.height(22.dp))
            if (page > 0) {
                androidx.compose.material3.TextButton(
                    onClick = onComplete,
                ) {
                    Text("Skip")
                }
            }
            Button(
                onClick = {
                    if (page == pages.lastIndex) onComplete() else page++
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(if (page == pages.lastIndex) "Get started" else "Continue")
            }
        }
    }
}
