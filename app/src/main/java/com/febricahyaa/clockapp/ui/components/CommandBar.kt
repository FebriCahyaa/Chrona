/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

/**
 * Entry point for dashboard commands (home, clock, dark, light, settings,
 * 12/24, reset, help). Lives as a small icon button in the header so it
 * never competes with screen content; tapping it opens a bottom-sheet popup
 * with the actual input field, closer to how a command palette should feel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandBar(
    onCommand: (ClockCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    var sheetOpen by remember { mutableStateOf(false) }

    IconButton(onClick = { sheetOpen = true }, modifier = modifier) {
        Icon(Icons.Default.Terminal, contentDescription = stringResource(R.string.command_bar_collapsed_hint))
    }

    if (sheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        var input by remember { mutableStateOf("") }
        var lastCommand by remember { mutableStateOf<ClockCommand?>(null) }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }

        fun close() {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                sheetOpen = false
                input = ""
                lastCommand = null
            }
        }

        fun submit() {
            val command = CommandParser.parse(input)
            lastCommand = command
            if (command !is ClockCommand.Empty) {
                onCommand(command)
            }
            input = ""
            keyboardController?.hide()
        }

        ModalBottomSheet(onDismissRequest = { close() }, sheetState = sheetState) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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

        LaunchedEffect(Unit) {
            delay(80)
            focusRequester.requestFocus()
        }
    }
}
