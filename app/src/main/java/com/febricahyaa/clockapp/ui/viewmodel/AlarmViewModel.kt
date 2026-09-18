/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.alarm.AlarmSchedulerGateway
import com.febricahyaa.clockapp.data.AlarmRepository
import com.febricahyaa.clockapp.model.AlarmItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Owns the alarm list and serializes persistence with UI state publication. */
class AlarmViewModel(
    private val repository: AlarmRepository,
    private val scheduler: AlarmSchedulerGateway,
) : ViewModel() {

    private val mutationMutex = Mutex()

    private val _alarms = MutableStateFlow<List<AlarmItem>>(emptyList())
    val alarms: StateFlow<List<AlarmItem>> = _alarms.asStateFlow()

    init {
        viewModelScope.launch {
            mutationMutex.withLock {
                val loaded = withContext(Dispatchers.IO) { repository.load() }
                _alarms.value = loaded
                scheduler.rescheduleAll(loaded)
            }
        }
    }

    fun add(alarm: AlarmItem) {
        mutate { current -> current + alarm }
    }

    fun delete(alarm: AlarmItem) {
        mutate { current -> current - alarm }
    }

    fun setEnabled(alarm: AlarmItem, enabled: Boolean) {
        mutate { current ->
            current.map { if (it.id == alarm.id) it.copy(enabled = enabled) else it }
        }
    }

    fun update(alarm: AlarmItem) {
        mutate { current ->
            current.map { if (it.id == alarm.id) alarm else it }
        }
    }

    private fun mutate(transform: (List<AlarmItem>) -> List<AlarmItem>) {
        viewModelScope.launch {
            mutationMutex.withLock {
                val updated = transform(_alarms.value)
                withContext(Dispatchers.IO) { repository.save(updated) }
                _alarms.value = updated
                scheduler.rescheduleAll(updated)
            }
        }
    }
}
