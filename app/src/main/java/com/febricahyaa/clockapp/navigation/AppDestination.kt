/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.navigation

import androidx.annotation.StringRes
import com.febricahyaa.clockapp.R

enum class AppDestination(@StringRes val labelRes: Int) {
    ALARM(R.string.nav_alarm),
    CLOCK(R.string.nav_clock),
    WORLD(R.string.nav_world),
    TIMER(R.string.nav_timer),
    STOPWATCH(R.string.nav_stopwatch);

    val inBottomBar: Boolean get() = this != ALARM
}
