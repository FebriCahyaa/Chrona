package com.febricahyaa.clockapp.command

sealed interface ClockCommand {
    data object Dark : ClockCommand
    data object Light : ClockCommand
    data object Settings : ClockCommand
    data object Format12 : ClockCommand
    data object Format24 : ClockCommand
    data object Help : ClockCommand
    data object Empty : ClockCommand
    data object Reset : ClockCommand
    data class Unknown(val value: String) : ClockCommand
}
