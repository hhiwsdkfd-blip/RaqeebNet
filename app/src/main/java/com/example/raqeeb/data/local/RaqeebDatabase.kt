package com.example.raqeeb.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.raqeeb.data.local.dao.ConnectionProfileDao
import com.example.raqeeb.data.local.dao.NetworkDeviceDao
import com.example.raqeeb.data.local.dao.TrafficSnapshotDao
import com.example.raqeeb.data.local.entity.ConnectionProfileEntity
import com.example.raqeeb.data.local.entity.NetworkDeviceEntity
import com.example.raqeeb.data.local.entity.TrafficSnapshotEntity
import com.example.raqeeb.data.model.ConnectionType

class Converters {
    @TypeConverter
    fun fromConnectionType(value: ConnectionType?): String? = value?.name

    @TypeConverter
    fun toConnectionType(value: String?): ConnectionType? =
        value?.let { runCatching { ConnectionType.valueOf(it) }.getOrDefault(ConnectionType.WIFI_ROUTER) }
}

@Database(
    entities = [
        ConnectionProfileEntity::class,
        NetworkDeviceEntity::class,
        TrafficSnapshotEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RaqeebDatabase : RoomDatabase() {

    abstract fun connectionProfileDao(): ConnectionProfileDao
    abstract fun networkDeviceDao(): NetworkDeviceDao
    abstract fun trafficSnapshotDao(): TrafficSnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: RaqeebDatabase? = null

        fun getInstance(context: Context): RaqeebDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RaqeebDatabase::class.java,
                    "raqeeb_net_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
