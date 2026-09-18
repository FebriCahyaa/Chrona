/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

/** Responsive layout buckets used by the Settings adaptive dashboard. */
enum class ChronaSettingsWindowClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
}

fun chronaSettingsWindowClass(maxWidthDp: Int): ChronaSettingsWindowClass = when {
    maxWidthDp >= 980 -> ChronaSettingsWindowClass.EXPANDED
    maxWidthDp >= 640 -> ChronaSettingsWindowClass.MEDIUM
    else -> ChronaSettingsWindowClass.COMPACT
}
