package com.febricahyaa.clockapp.command

object CommandParser {
    fun parse(input: String): ClockCommand {
        val normalized = input.trim().lowercase()

        return when (normalized) {
            "" -> ClockCommand.Empty
            "clock", "world", "worldclock", "home" -> ClockCommand.ClockView
            "alarm", "alarms" -> ClockCommand.Alarm
            "timer" -> ClockCommand.Timer
            "stopwatch" -> ClockCommand.Stopwatch
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
