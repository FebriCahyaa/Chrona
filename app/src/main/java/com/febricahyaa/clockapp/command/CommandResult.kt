package com.febricahyaa.clockapp.command

object CommandResult {
    fun message(command: ClockCommand): String {
        return when (command) {
            ClockCommand.Dark -> "Dark theme selected."
            ClockCommand.Light -> "Light theme selected."
            ClockCommand.Settings -> "Settings opened."
            ClockCommand.Format12 -> "12-hour format selected."
            ClockCommand.Format24 -> "24-hour format selected."
            ClockCommand.Help -> "Available commands: dark, light, settings, 12, 24, reset, help."
            ClockCommand.Empty -> "Enter a command."
            ClockCommand.Reset -> "Settings reset."
            is ClockCommand.Unknown -> "Unknown command: ${command.value}"
        }
    }
}
