package com.febricahyaa.clockapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ClockApp() }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ClockApp() {
    var darkMode by remember { mutableStateOf(true) }
    var command by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Ready") }
    var showSettings by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (darkMode) {
            androidx.compose.material3.darkColorScheme()
        } else {
            androidx.compose.material3.lightColorScheme()
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "CLOCK APP",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Your personal time dashboard")
                            }
                            IconButton(onClick = { darkMode = !darkMode }) {
                                Icon(
                                    if (darkMode) Icons.Default.LightMode
                                    else Icons.Default.DarkMode,
                                    contentDescription = "Toggle theme"
                                )
                            }
                            IconButton(onClick = { showSettings = !showSettings }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        }
                    }
                    item { ClockHero() }
                    item {
                        OutlinedTextField(
                            value = command,
                            onValueChange = { command = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Command") },
                            placeholder = { Text("dark, light, settings, reset") }
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                when (command.trim().lowercase()) {
                                    "dark" -> { darkMode = true; message = "Dark theme enabled" }
                                    "light" -> { darkMode = false; message = "Light theme enabled" }
                                    "settings" -> { showSettings = true; message = "Settings opened" }
                                    "reset" -> { command = ""; message = "Command cleared" }
                                    else -> message = "Unknown command"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Execute command")
                        }
                    }
                    item { Text(message, color = MaterialTheme.colorScheme.primary) }
                    if (showSettings) {
                        item {
                            GlassCard {
                                Text("Dashboard settings", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("Theme: ${if (darkMode) "Dark" else "Light"}")
                                Text("Widgets: Clock, Status, Command")
                            }
                        }
                    }
                    item {
                        GlassCard {
                            Text("Status", fontWeight = FontWeight.Bold)
                            Text("System online")
                            Text("Compose UI active")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClockHero() {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000)
        }
    }
    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
    val date = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(now)

    GlassCard {
        Text("CURRENT TIME", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(time, fontSize = 52.sp, fontWeight = FontWeight.Bold)
        Text(date)
    }
}

@Composable
fun GlassCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}
