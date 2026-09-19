/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.alarm

/** Plays/stops alarm audio and optional vibration. */
interface AlarmSoundGateway {
    fun start(ringtoneUri: String? = null, vibrate: Boolean = true)
    fun stop()
}
