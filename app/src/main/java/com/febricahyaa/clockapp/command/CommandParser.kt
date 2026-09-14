package com.febricahyaa.clockapp.command

/**
 * Converts raw command-bar input into a typed command.
 */
object CommandParser {
    fun parse(raw: String): ClockCommand {
        return when (raw.trim().lowercase()) {
            "" -> ClockCommand.Empty
            "dark" -> ClockCommand.Dark
            "light" -> ClockCommand.Light
            "settings" -> ClockCommand.Settings
            "12", "12h", "12-hour" -> ClockCommand.Format12
            "24", "24h", "24-hour" -> ClockCommand.Format24
            "reset" -> ClockCommand.Reset
            "help" -> ClockCommand.Help
            else -> ClockCommand.Unknown(raw.trim())
        }
    }
}
