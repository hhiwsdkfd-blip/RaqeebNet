package com.example.raqeeb.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.raqeeb.data.local.entity.ConnectionProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionProfileDao {

    @Query("SELECT * FROM connection_profiles ORDER BY id DESC")
    fun getAllProfiles(): Flow<List<ConnectionProfileEntity>>

    @Query("SELECT * FROM connection_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<ConnectionProfileEntity?>

    @Query("SELECT * FROM connection_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfileOnce(): ConnectionProfileEntity?

    @Query("SELECT * FROM connection_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): ConnectionProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ConnectionProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ConnectionProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ConnectionProfileEntity)

    @Query("UPDATE connection_profiles SET isActive = 0")
    suspend fun clearActiveProfiles()

    @Query("UPDATE connection_profiles SET isActive = 1 WHERE id = :id")
    suspend fun setActiveProfile(id: Long)

    @Query("DELETE FROM connection_profiles")
    suspend fun deleteAllProfiles()
}
