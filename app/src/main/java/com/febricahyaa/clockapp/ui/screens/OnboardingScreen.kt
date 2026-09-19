/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.model.AppThemeMode
import com.febricahyaa.clockapp.ui.components.BentoIcon
import com.febricahyaa.clockapp.ui.components.ChronaAmbientBackdrop
import com.febricahyaa.clockapp.ui.components.HybridBentoCard
import com.febricahyaa.clockapp.ui.theme.ChronaMotionTokens
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class OnboardingPage(
    val icon: ImageVector,
    @StringRes val eyebrow: Int,
    @StringRes val title: Int,
    @StringRes val body: Int,
)

private const val ONBOARDING_PAGE_COUNT = 3

internal fun onboardingNextPage(page: Int): Int =
    (page + 1).coerceAtMost(ONBOARDING_PAGE_COUNT - 1)

internal fun onboardingPreviousPage(page: Int): Int =
    (page - 1).coerceAtLeast(0)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pages = remember {
        listOf(
            OnboardingPage(
                icon = Icons.Filled.AccessTime,
                eyebrow = R.string.onboarding_local_eyebrow,
                title = R.string.onboarding_local_title,
                body = R.string.onboarding_local_body,
            ),
            OnboardingPage(
                icon = Icons.Filled.Public,
                eyebrow = R.string.onboarding_world_eyebrow,
                title = R.string.onboarding_world_title,
                body = R.string.onboarding_world_body,
            ),
            OnboardingPage(
                icon = Icons.Filled.SystemUpdate,
                eyebrow = R.string.onboarding_updates_eyebrow,
                title = R.string.onboarding_updates_title,
                body = R.string.onboarding_updates_body,
            ),
        )
    }

    var page by rememberSaveable { mutableIntStateOf(0) }
    var selectedPreview by rememberSaveable { mutableIntStateOf(0) }
    val current = pages[page]
    val haptics = LocalHapticFeedback.current
    val localTime = remember { LocalTime.now() }
    val localDate = remember { LocalDate.now() }
    val progressDescription = stringResource(R.string.onboarding_progress)

    fun goTo(nextPage: Int) {
        val normalized = nextPage.coerceIn(0, pages.lastIndex)
        if (normalized == page) return
        page = normalized
        selectedPreview = 0
        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
    }

    fun finish() {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onComplete()
    }

    BackHandler(enabled = page > 0) {
        goTo(onboardingPreviousPage(page))
    }

    Box(Modifier.fillMaxSize()) {
        ChronaAmbientBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        stringResource(R.string.onboarding_brand),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        stringResource(R.string.onboarding_tagline),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.88f),
                ) {
                    Text(
                        text = "%02d / %02d".format(page + 1, pages.size),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (
                        slideInHorizontally(
                            animationSpec = tween(ChronaMotionTokens.SpatialDurationMillis),
                            initialOffsetX = { it / 5 * direction },
                        ) + fadeIn(
                            animationSpec = tween(ChronaMotionTokens.MicroDurationMillis),
                        )
                    ) togetherWith (
                        slideOutHorizontally(
                            animationSpec = tween(ChronaMotionTokens.SpatialDurationMillis),
                            targetOffsetX = { -it / 5 * direction },
                        ) + fadeOut(
                            animationSpec = tween(ChronaMotionTokens.MicroDurationMillis),
                        )
                    )
                },
                label = "onboarding-page",
            ) { pageIndex ->
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OnboardingHeroCard(
                        page = pages[pageIndex],
                        pageIndex = pageIndex,
                        localTime = localTime,
                        localDate = localDate,
                        selectedPreview = selectedPreview,
                        onPreviewClick = {
                            selectedPreview = if (selectedPreview == 0) 1 else 0
                            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        },
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OnboardingMiniCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Alarm,
                            title = R.string.onboarding_alarm_title,
                            body = R.string.onboarding_alarm_body,
                        )
                        OnboardingMiniCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Timer,
                            title = R.string.onboarding_timer_title,
                            body = R.string.onboarding_timer_body,
                        )
                    }
                }
            }

            Text(
                stringResource(current.eyebrow),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp,
            )
            Text(
                stringResource(current.title),
                fontSize = 32.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                stringResource(current.body),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = progressDescription },
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                pages.indices.forEach { index ->
                    val active = index == page
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(if (active) 0.14f else 0.07f)
                            .height(7.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        onClick = { goTo(index) },
                    ) {}
                }
            }

            Spacer(Modifier.height(4.dp))

            if (selectedPreview == 1) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
                ) {
                    Text(
                        stringResource(R.string.onboarding_preview_selected),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (page > 0) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { goTo(onboardingPreviousPage(page)) },
                        modifier = Modifier.widthIn(min = 96.dp),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(stringResource(R.string.nav_back))
                    }
                }

                Button(
                    onClick = {
                        if (page == pages.lastIndex) finish()
                        else goTo(onboardingNextPage(page))
                    },
                    modifier = Modifier.widthIn(min = 128.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(if (page == pages.lastIndex) stringResource(R.string.onboarding_get_started) else stringResource(R.string.onboarding_continue))
                }
            }

            if (page > 0) {
                androidx.compose.material3.TextButton(
                    onClick = ::finish,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }
        }
    }
}

@Composable
private fun OnboardingHeroCard(
    page: OnboardingPage,
    pageIndex: Int,
    localTime: LocalTime,
    localDate: LocalDate,
    selectedPreview: Int,
    onPreviewClick: () -> Unit,
) {
    HybridBentoCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp),
        themeMode = AppThemeMode.MATERIAL_YOU,
        onClick = onPreviewClick,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BentoIcon(page.icon, Modifier.size(48.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (selectedPreview == 1) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f)
                    },
                ) {
                    Text(
                        if (selectedPreview == 1) stringResource(R.string.onboarding_interactive) else stringResource(R.string.onboarding_tap_explore),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            when (pageIndex) {
                0 -> {
                    Text(
                        localTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontSize = 58.sp,
                        lineHeight = 60.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        localDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        PreviewCity("Jakarta", "Asia/Jakarta")
                        PreviewCity("Tokyo", "Asia/Tokyo")
                        PreviewCity("London", "Europe/London")
                    }
                }

                else -> {
                    Text(
                        stringResource(R.string.onboarding_release_feed),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        stringResource(R.string.onboarding_updates_body),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.78f),
                    ) {
                        Text(
                            stringResource(R.string.onboarding_latest_release),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewCity(city: String, zoneId: String) {
    val now = remember(zoneId) { java.time.ZonedDateTime.now(ZoneId.of(zoneId)) }
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(city, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(zoneId, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            now.format(DateTimeFormatter.ofPattern("HH:mm")),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun OnboardingMiniCard(
    modifier: Modifier,
    icon: ImageVector,
    @StringRes title: Int,
    @StringRes body: Int,
) {
    HybridBentoCard(
        modifier = modifier.height(112.dp),
        themeMode = AppThemeMode.MATERIAL_YOU,
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BentoIcon(icon, Modifier.size(40.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(title), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(
                    stringResource(body),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
