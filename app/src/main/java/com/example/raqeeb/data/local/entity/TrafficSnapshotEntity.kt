package com.example.raqeeb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "traffic_snapshots")
data class TrafficSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val rxBytes: Long,               // Total received bytes
    val txBytes: Long,               // Total transmitted bytes
    val rxSpeedBps: Long = 0,        // Download speed in bytes/sec
    val txSpeedBps: Long = 0,        // Upload speed in bytes/sec
    val networkType: String = "WIFI" // WIFI, CELLULAR, OTHER
)
