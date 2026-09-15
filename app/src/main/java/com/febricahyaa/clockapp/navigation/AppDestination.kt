package com.febricahyaa.clockapp.navigation

import androidx.annotation.StringRes
import com.febricahyaa.clockapp.R

enum class AppDestination(@StringRes val labelRes: Int) {
    HOME(R.string.nav_home),
    CLOCK(R.string.nav_clock),
    SETTINGS(R.string.nav_settings)
}
