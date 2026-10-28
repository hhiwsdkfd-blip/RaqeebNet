package com.example.raqeeb.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "raqeeb_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "DARK", "LIGHT", "SYSTEM"
        val KEY_AUTO_SCAN_INTERVAL = intPreferencesKey("auto_scan_interval_minutes") // 0 = manual, 15, 30, 60
        val KEY_ALERTS_ENABLED = booleanPreferencesKey("alerts_enabled")
        val KEY_QUOTA_ALERT_THRESHOLD = intPreferencesKey("quota_alert_threshold_percent") // e.g. 80, 90
        val KEY_LAST_SCAN_TIMESTAMP = longPreferencesKey("last_scan_timestamp")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "DARK"
    }

    val autoScanInterval: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_SCAN_INTERVAL] ?: 0
    }

    val alertsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ALERTS_ENABLED] ?: true
    }

    val quotaAlertThreshold: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_QUOTA_ALERT_THRESHOLD] ?: 85
    }

    val lastScanTimestamp: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_SCAN_TIMESTAMP] ?: 0L
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setAutoScanInterval(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_SCAN_INTERVAL] = minutes
        }
    }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun setQuotaAlertThreshold(thresholdPercent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_QUOTA_ALERT_THRESHOLD] = thresholdPercent
        }
    }

    suspend fun setLastScanTimestamp(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_SCAN_TIMESTAMP] = timestamp
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
