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
            "reset" -> ClockCommand.Reset
            "help" -> ClockCommand.Help
            else -> ClockCommand.Unknown(raw.trim())
        }
    }
}
