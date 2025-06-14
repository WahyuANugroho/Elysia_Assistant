package com.example.elysia_assistant.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elysia_settings")

// Pastikan data class ini punya semua field yang dibutuhkan
data class WeatherCacheData(
    val cityName: String?,
    val temperature: String?, // Contoh: "28°C"
    val condition: String?,   // Pastikan field ini ada
    val iconCode: String?,    // Contoh: "01d"
    val lastUpdatedTimestamp: Long
)

class PreferenceManager(private val context: Context) {

    companion object {
        // Kunci untuk menyimpan data cuaca
        val CACHED_WEATHER_CITY_NAME = stringPreferencesKey("cached_weather_city_name")
        val CACHED_WEATHER_TEMPERATURE = stringPreferencesKey("cached_weather_temperature")
        val CACHED_WEATHER_CONDITION = stringPreferencesKey("cached_weather_condition") // Kunci untuk kondisi cuaca
        val CACHED_WEATHER_ICON_CODE = stringPreferencesKey("cached_weather_icon_code")
        val CACHED_WEATHER_LAST_UPDATED = longPreferencesKey("cached_weather_last_updated")
    }

    suspend fun saveWeatherCache(weatherData: WeatherCacheData) {
        context.dataStore.edit { preferences ->
            weatherData.cityName?.let { preferences[CACHED_WEATHER_CITY_NAME] = it }
            weatherData.temperature?.let { preferences[CACHED_WEATHER_TEMPERATURE] = it }
            weatherData.condition?.let { preferences[CACHED_WEATHER_CONDITION] = it } // Simpan kondisi
            weatherData.iconCode?.let { preferences[CACHED_WEATHER_ICON_CODE] = it }
            preferences[CACHED_WEATHER_LAST_UPDATED] = weatherData.lastUpdatedTimestamp
        }
    }

    val weatherCacheFlow: Flow<WeatherCacheData?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val lastUpdated = preferences[CACHED_WEATHER_LAST_UPDATED]
            if (lastUpdated != null) {
                WeatherCacheData(
                    cityName = preferences[CACHED_WEATHER_CITY_NAME],
                    temperature = preferences[CACHED_WEATHER_TEMPERATURE],
                    condition = preferences[CACHED_WEATHER_CONDITION], // Baca kondisi
                    iconCode = preferences[CACHED_WEATHER_ICON_CODE],
                    lastUpdatedTimestamp = lastUpdated
                )
            } else {
                null
            }
        }
}
