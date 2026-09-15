package com.febricahyaa.clockapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.R
import com.febricahyaa.clockapp.command.ClockCommand
import com.febricahyaa.clockapp.command.CommandParser
import com.febricahyaa.clockapp.command.CommandResult
import kotlinx.coroutines.delay

/**
 * Collapsible entry point for dashboard commands (home, clock, dark, light,
 * settings, 12/24, reset, help). Starts as a compact pill so it doesn't
 * compete with screen content; tapping it reveals the input field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandBar(
    onCommand: (ClockCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var lastCommand by remember { mutableStateOf<ClockCommand?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    fun submit() {
        val command = CommandParser.parse(input)
        lastCommand = command
        if (command !is ClockCommand.Empty) {
            onCommand(command)
        }
        input = ""
        keyboardController?.hide()
    }

    fun collapse() {
        expanded = false
        input = ""
        lastCommand = null
        keyboardController?.hide()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AnimatedContent(
            targetState = expanded,
            label = "commandBarExpanded",
            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) }
        ) { isExpanded ->
            if (isExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = { Text(stringResource(R.string.command_bar_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() })
                    )
                    IconButton(onClick = { submit() }) {
                        Icon(Icons.Default.Send, contentDescription = stringResource(R.string.command_bar_run))
                    }
                    IconButton(onClick = { collapse() }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.command_bar_close))
                    }
                }
                LaunchedEffect(Unit) {
                    delay(80)
                    focusRequester.requestFocus()
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.command_bar_collapsed_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        lastCommand?.let { command ->
            Text(
                text = CommandResult.message(command),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
