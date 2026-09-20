/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.febricahyaa.clockapp.time.ChronaTimeEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChronaRuntimeViewModel @Inject constructor(
    val timeEngine: ChronaTimeEngine,
) : ViewModel()
