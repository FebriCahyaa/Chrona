/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */
package com.febricahyaa.clockapp.navigation

/** Stable shared-element keys. Keep these values backward compatible across destinations. */
object ChronaMotionKeys {
    fun worldClockCard(id: Long): String = "world-clock-card:$id"
    const val DASHBOARD_HERO_CLOCK = "dashboard.hero.clock"
    const val DASHBOARD_WORLD_CLOCK = "dashboard.card.world-clock"
    const val DASHBOARD_ALARM = "dashboard.card.alarm"
    const val DASHBOARD_TIMER = "dashboard.card.timer"
    const val DASHBOARD_STOPWATCH = "dashboard.card.stopwatch"
    const val ALARM_EDITOR = "editor.alarm"
    const val TIMER_EDITOR = "editor.timer"
    const val TIMER_STATE_SURFACE = "state.timer"
    const val STOPWATCH_STATE_SURFACE = "state.stopwatch"
}
