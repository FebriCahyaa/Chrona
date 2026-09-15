package com.febricahyaa.clockapp.command

object CommandParser {
    fun parse(input: String): ClockCommand {
        val normalized = input.trim().lowercase()

        return when (normalized) {
            "" -> ClockCommand.Empty
            "home" -> ClockCommand.Home
            "clock" -> ClockCommand.ClockView
            "dark" -> ClockCommand.Dark
            "light" -> ClockCommand.Light
            "settings" -> ClockCommand.Settings
            "12", "12h", "12-hour" -> ClockCommand.Format12
            "24", "24h", "24-hour" -> ClockCommand.Format24
            "help" -> ClockCommand.Help
            "reset" -> ClockCommand.Reset
            else -> ClockCommand.Unknown(normalized)
        }
    }
}
