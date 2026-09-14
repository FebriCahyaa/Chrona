@file:OptIn(ExperimentalMaterial3Api::class)

package com.febricahyaa.clockapp


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.febricahyaa.clockapp.command.ClockCommand
import com.febricahyaa.clockapp.command.CommandParser
import com.febricahyaa.clockapp.command.CommandResult

private val GlassShape = RoundedCornerShape(28.dp)



@Composable
fun ClockApp() {
    var isDarkTheme by remember { mutableStateOf(true) }
    var command by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var commandResult by remember { mutableStateOf<String?>(null) }
    var use24HourFormat by remember { mutableStateOf(true) }

    ClockTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Header(
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = { isDarkTheme = it }
                )

                ClockCard()

                CommandBar(
                    value = command,
                    onValueChange = { command = it },
                    onCommand = { value ->
                        val parsedCommand = CommandParser.parse(value)
                        commandResult = CommandResult.message(parsedCommand)

                        when (parsedCommand) {
                            ClockCommand.Dark -> isDarkTheme = true
                            ClockCommand.Light -> isDarkTheme = false
                            ClockCommand.Settings -> showSettings = !showSettings
                            ClockCommand.Format12 -> use24HourFormat = false
                            ClockCommand.Format24 -> use24HourFormat = true
                            ClockCommand.Reset -> {
                                command = ""
                                showSettings = false
                                isDarkTheme = true
                            }
                            ClockCommand.Help,
                            ClockCommand.Format12,
                            ClockCommand.Format24,
                            ClockCommand.Empty,
                            is ClockCommand.Unknown -> Unit
                        }
                    }
                )

                commandResult?.let { result ->
                    CommandResultCard(message = result)
                }

                if (showSettings) {
                    SettingsCard(
                        isDarkTheme = isDarkTheme,
                        onThemeChanged = { isDarkTheme = it }
                    )
                } else {
                    StatusCard()
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "CLOCK APP  •  Built with Kotlin & Jetpack Compose",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Header(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Good to see you",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Your time, beautifully arranged.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Switch(
            checked = isDarkTheme,
            onCheckedChange = onThemeChanged,
            thumbContent = {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
private fun ClockCard(use24HourFormat: Boolean) {
    var currentTime by remember { mutableStateOf(Date()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1_000)
        }
    }

    GlassCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = timeFormatter.format(currentTime),
                fontSize = 54.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
            Text(
                text = dateFormatter.format(currentTime),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CommandBar(
    value: String,
    onValueChange: (String) -> Unit,
    onCommand: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            placeholder = { Text("Try: dark, light, settings, reset") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onDone = {
                    onCommand(value)
                    keyboardController?.hide()
                }
            )
        )

        Button(
            onClick = {
                onCommand(value)
                keyboardController?.hide()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Execute command")
        }
    }
}

@Composable
private fun CommandResultCard(message: String) {
    GlassCard {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun StatusCard() {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("System status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Everything is running smoothly.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.width(8.dp).height(8.dp),
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF55C98A)
                ) {}
                Text("Clock service active", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SettingsCard(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    GlassCard {
        Text(
            text = "Clock format",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (use24HourFormat) "24-hour" else "12-hour",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = use24HourFormat,
                onCheckedChange = onFormatChange
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark theme")
                Switch(checked = isDarkTheme, onCheckedChange = onThemeChanged)
            }
        }
    }
}

@Composable
private fun GlassCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = GlassShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            content()
        }
    }
}

@Composable
private fun ClockTheme(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (isDarkTheme) {
        androidx.compose.material3.darkColorScheme()
    } else {
        androidx.compose.material3.lightColorScheme()
    }

    MaterialTheme(colorScheme = colors, content = content)
}
