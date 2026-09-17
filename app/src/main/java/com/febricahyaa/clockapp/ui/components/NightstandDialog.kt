/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NightstandDialog(use24HourFormat: Boolean, showSeconds: Boolean, onDismiss: () -> Unit) {
    val now = rememberZonedNow()
    val pattern = if (use24HourFormat) "HH:mm" else "hh:mm"
    val locale = LocalConfiguration.current.locales[0]
    val value = now.format(DateTimeFormatter.ofPattern(pattern, locale))
    val hour = value.substring(0, 2)
    val minute = value.substring(3, 5)
    val seconds = now.format(DateTimeFormatter.ofPattern("ss", locale))

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(Modifier.fillMaxSize().clickable { onDismiss() }, color = Color(0xFF070707)) {
            Box(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(now.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.ENGLISH)), fontSize = 14.sp, color = Color(0xFF77736D))
                    Spacer(Modifier.padding(8.dp))
                    Text(hour, fontSize = 134.sp, lineHeight = 120.sp, fontWeight = FontWeight.Light, color = Color(0xFFF4F0EA), letterSpacing = (-5).sp)
                    Text(minute, fontSize = 134.sp, lineHeight = 120.sp, fontWeight = FontWeight.Light, style = TextStyle(brush = Brush.linearGradient(listOf(Color(0xFFFFE5D5), Color(0xFFF39A69)))), letterSpacing = (-5).sp)
                    if (showSeconds) Text("$seconds", fontSize = 12.sp, letterSpacing = 4.sp, color = Color(0xFF6D6963), modifier = Modifier.padding(top = 6.dp))
                    Text("INDONESIA  •  UTC+7", fontSize = 10.sp, letterSpacing = 3.sp, color = Color(0xFF65615B), modifier = Modifier.padding(top = 18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 24.dp)) {
                        Surface(color = Color.White.copy(alpha = .055f), shape = RoundedCornerShape(20.dp)) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Cloud, null, tint = Color(0xFF8D8982))
                                Column { Text("Weather", fontSize = 9.sp, color = Color(0xFF74716B)); Text("—", fontSize = 13.sp, color = Color(0xFFD2CEC7)) }
                            }
                        }
                        Surface(color = Color.White.copy(alpha = .055f), shape = RoundedCornerShape(20.dp)) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Alarm, null, tint = Color(0xFFE9A47E))
                                Column { Text("Next alarm", fontSize = 9.sp, color = Color(0xFF74716B)); Text("Ready", fontSize = 13.sp, color = Color(0xFFD2CEC7)) }
                            }
                        }
                    }
                    Text("TAP ANYWHERE TO CLOSE", fontSize = 9.sp, letterSpacing = 2.sp, color = Color(0xFF4B4844), modifier = Modifier.padding(top = 34.dp))
                }
            }
        }
    }
}
