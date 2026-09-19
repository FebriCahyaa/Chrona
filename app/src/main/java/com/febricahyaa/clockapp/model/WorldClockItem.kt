/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.model

/** A saved World Clock location identified by its stable IANA time-zone ID. */
data class WorldClockItem(
    val id: Long,
    val city: String,
    val zoneId: String,
)
