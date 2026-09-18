/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.alarm.AlarmSchedulerGateway
import com.febricahyaa.clockapp.data.AlarmRepository
import com.febricahyaa.clockapp.model.AlarmItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the alarm list. Every mutation persists through [repository] and
 * re-syncs [scheduler] in one place, so the "an edited alarm is always
 * reflected in AlarmManager" invariant can't be forgotten at a call site
 * (previously every call site in the Composable had to remember to call
 * both `AlarmStore.save` and `AlarmScheduler.rescheduleAll` itself).
 */
class AlarmViewModel(
    private val repository: AlarmRepository,
    private val scheduler: AlarmSchedulerGateway,
) : ViewModel() {

    private val _alarms = MutableStateFlow<List<AlarmItem>>(emptyList())
    val alarms: StateFlow<List<AlarmItem>> = _alarms.asStateFlow()

    init {
        viewModelScope.launch {
            val loaded = repository.load()
            _alarms.value = loaded
            scheduler.rescheduleAll(loaded)
        }
    }

    fun add(alarm: AlarmItem) = mutate { it + alarm }

    fun delete(alarm: AlarmItem) = mutate { it - alarm }

    fun setEnabled(alarm: AlarmItem, enabled: Boolean) = mutate { list ->
        list.map { if (it.id == alarm.id) it.copy(enabled = enabled) else it }
    }

    fun update(alarm: AlarmItem) = mutate { list ->
        list.map { if (it.id == alarm.id) alarm else it }
    }

    private fun mutate(transform: (List<AlarmItem>) -> List<AlarmItem>) {
        val updated = transform(_alarms.value)
        _alarms.value = updated
        repository.save(updated)
        scheduler.rescheduleAll(updated)
    }
}
