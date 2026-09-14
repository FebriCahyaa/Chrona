package com.febricahyaa.clockapp.command

/**
 * Commands supported by the clock dashboard.
 */
sealed interface ClockCommand {
    data object Dark : ClockCommand
    data object Light : ClockCommand
    data object Settings
    data object Format12
    data object Format24 : ClockCommand
    data object Reset : ClockCommand
    data object Help : ClockCommand
    data class Unknown(val raw: String) : ClockCommand
    data object Empty : ClockCommand
}
