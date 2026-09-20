/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.location

data class CurrentLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String?,
    val country: String?,
    val accuracyMeters: Float?,
    val updatedAtMillis: Long,
)
