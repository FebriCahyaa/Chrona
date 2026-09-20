/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.nativelayer

/** Low-latency AAudio fallback used only when Android has no usable alarm URI. */
object NativeAudioEngine {
    fun startAlertTone(): Boolean = ChronaNativeBridge.startLowLatencyAlertTone()

    fun stopAlertTone() = ChronaNativeBridge.stopLowLatencyAlertTone()
}
