package com.nexuzstudios.qrcodescanner_quickscan.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qr_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val VIBRATE_ON_SCAN = booleanPreferencesKey("vibrate_on_scan")
        val BEEP_ON_SCAN = booleanPreferencesKey("beep_on_scan")
        val AUTO_OPEN_URL = booleanPreferencesKey("auto_open_url")
        val IS_PRO = booleanPreferencesKey("is_pro")
        val SCAN_COUNT = intPreferencesKey("scan_count")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val COPY_ON_SCAN = booleanPreferencesKey("copy_on_scan")
    }

    val vibrateOnScan: Flow<Boolean> = context.dataStore.data
        .map { it[VIBRATE_ON_SCAN] ?: true }

    val beepOnScan: Flow<Boolean> = context.dataStore.data
        .map { it[BEEP_ON_SCAN] ?: false }

    val autoOpenUrl: Flow<Boolean> = context.dataStore.data
        .map { it[AUTO_OPEN_URL] ?: false }

    val isPro: Flow<Boolean> = context.dataStore.data
        .map { it[IS_PRO] ?: false }

    val scanCount: Flow<Int> = context.dataStore.data
        .map { it[SCAN_COUNT] ?: 0 }

    val copyOnScan: Flow<Boolean> = context.dataStore.data
        .map { it[COPY_ON_SCAN] ?: false }

    suspend fun setVibrateOnScan(value: Boolean) {
        context.dataStore.edit { it[VIBRATE_ON_SCAN] = value }
    }

    suspend fun setBeepOnScan(value: Boolean) {
        context.dataStore.edit { it[BEEP_ON_SCAN] = value }
    }

    suspend fun setAutoOpenUrl(value: Boolean) {
        context.dataStore.edit { it[AUTO_OPEN_URL] = value }
    }

    suspend fun setIsPro(value: Boolean) {
        context.dataStore.edit { it[IS_PRO] = value }
    }

    suspend fun incrementScanCount() {
        context.dataStore.edit { prefs ->
            prefs[SCAN_COUNT] = (prefs[SCAN_COUNT] ?: 0) + 1
        }
    }

    suspend fun setCopyOnScan(value: Boolean) {
        context.dataStore.edit { it[COPY_ON_SCAN] = value }
    }
}
