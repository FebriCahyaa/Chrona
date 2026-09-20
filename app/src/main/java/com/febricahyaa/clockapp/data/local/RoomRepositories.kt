/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.local

import androidx.room.withTransaction
import com.febricahyaa.clockapp.data.AlarmRepository
import com.febricahyaa.clockapp.data.StorageMigrationCoordinator
import com.febricahyaa.clockapp.data.WorldClockRepository
import com.febricahyaa.clockapp.model.AlarmItem
import com.febricahyaa.clockapp.model.WorldClockItem
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomAlarmRepository @Inject constructor(
    private val database: ChronaDatabase,
    private val migration: StorageMigrationCoordinator,
) : AlarmRepository {

    override val alarms: Flow<List<AlarmItem>> = database.alarmDao().observeAll()
        .map { entities -> entities.map(AlarmEntity::toModel) }

    override suspend fun load(): List<AlarmItem> {
        migration.ensureMigrated()
        return database.alarmDao().getAll().map(AlarmEntity::toModel)
    }

    override suspend fun save(alarms: List<AlarmItem>) {
        migration.ensureMigrated()
        database.withTransaction {
            val existing = database.alarmDao().getAll().associateBy(AlarmEntity::id)
            database.alarmDao().deleteAll()
            database.alarmDao().insertAll(
                alarms.map { item ->
                    item.toEntity(existing[item.id]?.ringtoneUri)
                },
            )
        }
    }

    override suspend fun recordHistory(
        alarmId: Long,
        eventType: String,
        triggeredAtEpochMillis: Long,
    ) {
        migration.ensureMigrated()
        database.alarmDao().insertHistory(
            AlarmHistoryEntity(
                alarmId = alarmId,
                triggeredAtEpochMillis = triggeredAtEpochMillis,
                eventType = eventType,
            ),
        )
    }
}

class RoomWorldClockRepository @Inject constructor(
    private val database: ChronaDatabase,
    private val migration: StorageMigrationCoordinator,
) : WorldClockRepository {

    override val items: Flow<List<WorldClockItem>> = database.worldClockDao().observeAll()
        .map { entities -> entities.map(WorldClockEntity::toModel) }

    override val favorites: Flow<Set<String>> = database.worldClockDao().observeAll()
        .map { entities ->
            entities.asSequence()
                .filter(WorldClockEntity::favorite)
                .map(WorldClockEntity::zoneId)
                .toSet()
        }

    override suspend fun load(): List<WorldClockItem> {
        migration.ensureMigrated()
        return database.worldClockDao().getAll().map(WorldClockEntity::toModel)
    }

    override suspend fun save(items: List<WorldClockItem>) {
        migration.ensureMigrated()
        database.withTransaction {
            val existingFavorites = database.worldClockDao().getAll()
                .associate { entity -> entity.zoneId to entity.favorite }
            database.worldClockDao().deleteAll()
            database.worldClockDao().insertAll(
                items.map { item ->
                    WorldClockEntity(
                        id = item.id,
                        city = item.city,
                        zoneId = item.zoneId,
                        favorite = existingFavorites[item.zoneId] ?: false,
                    )
                },
            )
        }
    }

    override suspend fun loadFavorites(): Set<String> {
        migration.ensureMigrated()
        return database.worldClockDao().getAll()
            .asSequence()
            .filter(WorldClockEntity::favorite)
            .map(WorldClockEntity::zoneId)
            .toSet()
    }

    override suspend fun saveFavorites(zoneIds: Set<String>) {
        migration.ensureMigrated()
        database.withTransaction {
            database.worldClockDao().getAll().forEach { entity ->
                database.worldClockDao().setFavorite(
                    entity.zoneId,
                    entity.zoneId in zoneIds,
                )
            }
        }
    }
}

private fun AlarmEntity.toModel(): AlarmItem = AlarmItem(
    id = id,
    time = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59)),
    label = label,
    enabled = enabled,
    repeatDays = repeatDays.splitToSequence(',')
        .mapNotNull { it.toIntOrNull() }
        .mapNotNull { value -> runCatching { DayOfWeek.of(value) }.getOrNull() }
        .toSet(),
    ringtoneUri = ringtoneUri,
    ringtoneName = ringtoneName,
    vibrate = vibrate,
)

private fun AlarmItem.toEntity(existingRingtoneUri: String?): AlarmEntity = AlarmEntity(
    id = id,
    hour = time.hour,
    minute = time.minute,
    label = label,
    enabled = enabled,
    repeatDays = repeatDays.map(DayOfWeek::getValue).sorted().joinToString(","),
    ringtoneUri = ringtoneUri ?: existingRingtoneUri,
    ringtoneName = ringtoneName,
    vibrate = vibrate,
)

private fun WorldClockEntity.toModel(): WorldClockItem = WorldClockItem(
    id = id,
    city = city,
    zoneId = zoneId,
)
