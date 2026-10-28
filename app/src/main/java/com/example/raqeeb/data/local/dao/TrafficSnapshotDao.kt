package com.example.raqeeb.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.raqeeb.data.local.entity.TrafficSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrafficSnapshotDao {

    @Query("SELECT * FROM traffic_snapshots ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSnapshots(limit: Int = 50): Flow<List<TrafficSnapshotEntity>>

    @Query("SELECT * FROM traffic_snapshots WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getSnapshotsSince(sinceTimestamp: Long): Flow<List<TrafficSnapshotEntity>>

    @Query("SELECT * FROM traffic_snapshots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSnapshot(): TrafficSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: TrafficSnapshotEntity): Long

    @Query("DELETE FROM traffic_snapshots WHERE timestamp < :beforeTimestamp")
    suspend fun pruneOldSnapshots(beforeTimestamp: Long)

    @Query("DELETE FROM traffic_snapshots")
    suspend fun deleteAllSnapshots()
}
