package com.febricahyaa.clockapp.command

/**
 * User-facing result text for a parsed command.
 */
object CommandResult {
    fun message(command: ClockCommand): String {
        return when (command) {
            ClockCommand.Dark -> "Dark mode selected."
            ClockCommand.Light -> "Light mode selected."
            ClockCommand.Settings -> "Settings command received."
            ClockCommand.Format12 -> "12-hour format selected."
            ClockCommand.Format24 -> "24-hour format selected."
            ClockCommand.Reset -> "Clock settings reset."
            ClockCommand.Help -> "Available commands: dark, light, settings, reset, help."
            ClockCommand.Empty -> "Enter a command first."
            is ClockCommand.Unknown -> "Unknown command: ${command.raw}"
        }
    }
}
