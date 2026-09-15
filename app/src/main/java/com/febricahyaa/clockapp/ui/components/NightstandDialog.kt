package com.febricahyaa.clockapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.febricahyaa.clockapp.model.AlarmItem
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NightstandDialog(alarms: List<AlarmItem>, onDismiss: () -> Unit) {
    val now = rememberZonedNow()
    val next = alarms.filter { it.enabled }.minByOrNull { it.time }
    val hhmm = now.format(DateTimeFormatter.ofPattern("HH:mm"))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().clickable(onClick = onDismiss),
            color = Color(0xFF070707)
        ) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    now.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.ENGLISH)),
                    fontSize = 15.sp, color = Color(0xFF8F8C86)
                )
                Spacer(Modifier.height(22.dp))
                Text(hhmm.substring(0, 2), fontSize = 128.sp, fontWeight = FontWeight.SemiBold,
                    lineHeight = 118.sp, color = Color(0xFFF5F2EC))
                Text(hhmm.substring(3), fontSize = 128.sp, fontWeight = FontWeight.SemiBold,
                    lineHeight = 118.sp, style = TextStyle(brush = LocalAccentGradient.current))
                Spacer(Modifier.height(18.dp))
                Text("INDONESIA (UTC+7)", fontSize = 11.sp, letterSpacing = 3.sp, color = Color(0xFF6F6C66))
                Spacer(Modifier.height(22.dp))
                Surface(color = Color.White.copy(alpha = 0.06f), shape = RoundedCornerShape(18.dp)) {
                    Row(
                        Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        Text("⛅ 28°C Partly cloudy", fontSize = 13.sp, color = Color(0xFFC9C5BD))
                        Text(
                            "⏰ Next alarm " + (next?.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--"),
                            fontSize = 13.sp, color = Color(0xFFC9C5BD)
                        )
                    }
                }
                Spacer(Modifier.height(42.dp))
                Text("TAP ANYWHERE TO CLOSE", fontSize = 10.sp, letterSpacing = 2.sp, color = Color(0xFF55524D))
            }
        }
    }
}
