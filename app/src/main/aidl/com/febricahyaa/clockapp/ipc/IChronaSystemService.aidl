/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.ipc;

interface IChronaSystemService {
    boolean ping();
    long currentEpochMillis();
    long currentElapsedRealtimeMillis();
}
