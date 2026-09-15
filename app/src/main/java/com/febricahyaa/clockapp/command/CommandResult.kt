package com.febricahyaa.clockapp.command

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.febricahyaa.clockapp.R

object CommandResult {
    @Composable
    fun message(command: ClockCommand): String {
        return when (command) {
            ClockCommand.Home -> stringResource(R.string.command_feedback_home)
            ClockCommand.ClockView -> stringResource(R.string.command_feedback_clock)
            ClockCommand.Dark -> stringResource(R.string.command_feedback_dark)
            ClockCommand.Light -> stringResource(R.string.command_feedback_light)
            ClockCommand.Settings -> stringResource(R.string.command_feedback_settings)
            ClockCommand.Format12 -> stringResource(R.string.command_feedback_format_12)
            ClockCommand.Format24 -> stringResource(R.string.command_feedback_format_24)
            ClockCommand.Help -> stringResource(R.string.command_feedback_help)
            ClockCommand.Empty -> stringResource(R.string.command_feedback_empty)
            ClockCommand.Reset -> stringResource(R.string.command_feedback_reset)
            is ClockCommand.Unknown -> stringResource(R.string.command_feedback_unknown, command.value)
        }
    }
}
