/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.timer

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.febricahyaa.clockapp.model.ClockSettings
import com.febricahyaa.clockapp.ui.theme.ChronaTheme


/** Full-screen timer completion surface shown for an explicitly user-created timer. */
class TimerRingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        setContent {
            ChronaTheme(ClockSettings()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = getString(com.febricahyaa.clockapp.R.string.timer_finished_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Button(
                        modifier = Modifier.padding(top = 24.dp),
                        onClick = {
                            stopService(android.content.Intent(this@TimerRingActivity, TimerService::class.java))
                            setResult(Activity.RESULT_OK)
                            finish()
                        },
                    ) {
                        Text(getString(com.febricahyaa.clockapp.R.string.timer_finished_stop))
                    }
                }
            }
        }
    }
}
