/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val id: Long,
    val hour: Int,
    val minute: Int,
    val label: String,
    val enabled: Boolean,
    val repeatDays: String,
    val ringtoneUri: String?,
    val ringtoneName: String,
    val vibrate: Boolean,
)

@Entity(tableName = "world_clock_items")
data class WorldClockEntity(
    @PrimaryKey val id: Long,
    val city: String,
    val zoneId: String,
    val favorite: Boolean,
)

@Entity(tableName = "alarm_history")
data class AlarmHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val alarmId: Long,
    val triggeredAtEpochMillis: Long,
    val eventType: String,
)

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour, minute, id")
    fun observeAll(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms ORDER BY hour, minute, id")
    suspend fun getAll(): List<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AlarmEntity>)

    @Query("DELETE FROM alarms")
    suspend fun deleteAll()

    @Insert
    suspend fun insertHistory(event: AlarmHistoryEntity)

    @Query("SELECT * FROM alarm_history ORDER BY triggeredAtEpochMillis DESC LIMIT :limit")
    fun observeHistory(limit: Int): Flow<List<AlarmHistoryEntity>>
}

@Dao
interface WorldClockDao {
    @Query("SELECT * FROM world_clock_items ORDER BY id")
    fun observeAll(): Flow<List<WorldClockEntity>>

    @Query("SELECT * FROM world_clock_items ORDER BY id")
    suspend fun getAll(): List<WorldClockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WorldClockEntity>)

    @Query("DELETE FROM world_clock_items")
    suspend fun deleteAll()

    @Query("UPDATE world_clock_items SET favorite = :favorite WHERE zoneId = :zoneId")
    suspend fun setFavorite(zoneId: String, favorite: Boolean)
}

@Database(
    entities = [AlarmEntity::class, WorldClockEntity::class, AlarmHistoryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ChronaDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun worldClockDao(): WorldClockDao
}
