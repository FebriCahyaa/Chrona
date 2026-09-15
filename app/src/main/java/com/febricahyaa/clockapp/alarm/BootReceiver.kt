package com.febricahyaa.clockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.febricahyaa.clockapp.data.AlarmStore

/** Reschedules all enabled alarms after the device reboots, since AlarmManager entries do not survive a restart. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return
        val alarms = AlarmStore.load(context)
        AlarmScheduler.rescheduleAll(context, alarms)
    }
}
