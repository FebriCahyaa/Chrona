/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.theme

import androidx.compose.runtime.compositionLocalOf
import com.febricahyaa.clockapp.model.AppThemeMode

/** Theme metadata shared by presentation-only Compose primitives. */
val LocalChronaThemeMode = compositionLocalOf { AppThemeMode.MATERIAL_YOU }
