package com.example.elysia_assistant.data.local // Sesuaikan dengan package Anda

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Definisikan DataStore instance (biasanya satu per aplikasi)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elysia_settings")

data class WeatherCacheData(
    val cityName: String?,
    val temperature: String?, // Simpan sebagai String yang sudah diformat, misal "28°C, Cerah"
    val iconCode: String?,    // Simpan kode ikon dari API, misal "01d"
    val lastUpdatedTimestamp: Long
)

class PreferenceManager(private val context: Context) {

    companion object {
        val LAST_APP_OPEN_TIMESTAMP = longPreferencesKey("last_app_open_timestamp")
        val NOTIFICATIONS_MUTED_RANDOM_ENABLED = booleanPreferencesKey("notifications_muted_random_enabled")
        // Tambahkan kunci lain jika perlu untuk preferensi notifikasi

        // Kunci untuk menyimpan data cuaca
        val CACHED_WEATHER_CITY_NAME = stringPreferencesKey("cached_weather_city_name")
        val CACHED_WEATHER_TEMPERATURE_CONDITION = stringPreferencesKey("cached_weather_temperature_condition")
        val CACHED_WEATHER_ICON_CODE = stringPreferencesKey("cached_weather_icon_code")
        val CACHED_WEATHER_LAST_UPDATED = longPreferencesKey("cached_weather_last_updated")
    }

    // Fungsi untuk menyimpan timestamp terakhir aplikasi dibuka
    suspend fun setLastAppOpenTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_APP_OPEN_TIMESTAMP] = timestamp
        }
    }

    // Fungsi untuk mendapatkan timestamp terakhir aplikasi dibuka
    val lastAppOpenTimestampFlow: Flow<Long?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LAST_APP_OPEN_TIMESTAMP]
        }

    // Fungsi untuk menyimpan data cuaca yang di-cache
    suspend fun saveWeatherCache(weatherData: WeatherCacheData) {
        context.dataStore.edit { preferences ->
            weatherData.cityName?.let { preferences[CACHED_WEATHER_CITY_NAME] = it }
                ?: preferences.remove(CACHED_WEATHER_CITY_NAME)
            weatherData.temperature?.let { preferences[CACHED_WEATHER_TEMPERATURE_CONDITION] = it }
                ?: preferences.remove(CACHED_WEATHER_TEMPERATURE_CONDITION)
            weatherData.iconCode?.let { preferences[CACHED_WEATHER_ICON_CODE] = it }
                ?: preferences.remove(CACHED_WEATHER_ICON_CODE)
            preferences[CACHED_WEATHER_LAST_UPDATED] = weatherData.lastUpdatedTimestamp
        }
    }

    // Fungsi untuk mendapatkan data cuaca yang di-cache sebagai Flow
    val weatherCacheFlow: Flow<WeatherCacheData?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val cityName = preferences[CACHED_WEATHER_CITY_NAME]
            val tempCondition = preferences[CACHED_WEATHER_TEMPERATURE_CONDITION]
            val iconCode = preferences[CACHED_WEATHER_ICON_CODE]
            val lastUpdated = preferences[CACHED_WEATHER_LAST_UPDATED]

            if (lastUpdated != null) { // Hanya buat objek jika ada data timestamp
                WeatherCacheData(cityName, tempCondition, iconCode, lastUpdated)
            } else {
                null
            }
        }

    // Tambahkan fungsi lain untuk preferensi notifikasi di sini
}
