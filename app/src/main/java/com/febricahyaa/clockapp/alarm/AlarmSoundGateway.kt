/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.alarm

/**
 * Plays/stops the ringing alarm sound + vibration. Abstracted so the
 * notification action receiver and the ringing activity depend on an
 * interface rather than a concrete `Ringtone`/`Vibrator` singleton.
 */
interface AlarmSoundGateway {
    fun start()
    fun stop()
}
