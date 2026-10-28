package com.example.raqeeb.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.raqeeb.data.local.entity.NetworkDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkDeviceDao {

    @Query("SELECT * FROM network_devices ORDER BY isOnline DESC, lastSeenAt DESC")
    fun getAllDevices(): Flow<List<NetworkDeviceEntity>>

    @Query("SELECT * FROM network_devices WHERE isOnline = 1")
    fun getOnlineDevices(): Flow<List<NetworkDeviceEntity>>

    @Query("SELECT COUNT(*) FROM network_devices WHERE isOnline = 1")
    fun getOnlineCount(): Flow<Int>

    @Query("SELECT * FROM network_devices WHERE ipAddress = :ip LIMIT 1")
    suspend fun getDeviceByIp(ip: String): NetworkDeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(device: NetworkDeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(devices: List<NetworkDeviceEntity>)

    @Update
    suspend fun updateDevice(device: NetworkDeviceEntity)

    @Query("UPDATE network_devices SET isOnline = 0 WHERE ipAddress NOT IN (:activeIps)")
    suspend fun markOthersOffline(activeIps: List<String>)

    @Query("UPDATE network_devices SET customName = :customName, deviceType = :deviceType WHERE ipAddress = :ip")
    suspend fun updateCustomInfo(ip: String, customName: String, deviceType: String)

    @Query("DELETE FROM network_devices")
    suspend fun deleteAllDevices()
}
