/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.febricahyaa.clockapp.alarm.AlarmSchedulerGateway
import com.febricahyaa.clockapp.data.AlarmRepository
import com.febricahyaa.clockapp.model.AlarmItem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Owns the alarm list. Persistence and scheduling are serialized so an
 * asynchronous initial load can never overwrite a user mutation made just
 * after the ViewModel is created.
 */
class AlarmViewModel(
    private val repository: AlarmRepository,
    private val scheduler: AlarmSchedulerGateway,
) : ViewModel() {

    private val _alarms = MutableStateFlow<List<AlarmItem>>(emptyList())
    val alarms: StateFlow<List<AlarmItem>> = _alarms.asStateFlow()

    private val mutationMutex = Mutex()
    private val initialized = CompletableDeferred<Unit>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loaded = repository.load()
                _alarms.value = loaded
                initialized.complete(Unit)

                runCatching {
                    scheduler.rescheduleAll(loaded)
                }
            } catch (error: Throwable) {
                initialized.completeExceptionally(error)
                throw error
            }
        }
    }

    fun add(alarm: AlarmItem) = mutate { current ->
        if (current.any { it.id == alarm.id }) {
            current.map { if (it.id == alarm.id) alarm else it }
        } else {
            current + alarm
        }
    }

    fun delete(alarm: AlarmItem) = mutate { current ->
        current.filterNot { it.id == alarm.id }
    }

    fun setEnabled(alarm: AlarmItem, enabled: Boolean) = mutate { current ->
        current.map { if (it.id == alarm.id) it.copy(enabled = enabled) else it }
    }

    fun update(alarm: AlarmItem) = mutate { current ->
        current.map { if (it.id == alarm.id) alarm else it }
    }

    private fun mutate(transform: (List<AlarmItem>) -> List<AlarmItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            mutationMutex.withLock {
                initialized.await()

                val current = _alarms.value
                val updated = transform(current)
                if (updated == current) return@withLock

                // Commit before publishing the new UI state. This makes the
                // state transition durable before another component can read
                // the repository after a process restart.
                repository.save(updated)
                _alarms.value = updated

                runCatching {
                    scheduler.rescheduleAll(updated)
                }
            }
        }
    }
}
