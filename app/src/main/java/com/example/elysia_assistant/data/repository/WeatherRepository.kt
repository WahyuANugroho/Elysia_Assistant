package com.example.elysia_assistant.data.repository // Pastikan package ini sesuai ya, Kapten!

import android.location.Location
import android.util.Log
import com.example.elysia_assistant.BuildConfig
import com.example.elysia_assistant.data.local.PreferenceManager
import com.example.elysia_assistant.data.local.WeatherCacheData
import com.example.elysia_assistant.data.remote.WeatherApiService
import com.example.elysia_assistant.domain.model.WeatherApiResponse
import com.example.elysia_assistant.util.LocationProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

sealed class WeatherResult {
    data class Success(val data: WeatherApiResponse) : WeatherResult()
    data class Error(val message: String, val cause: Throwable? = null) : WeatherResult()
}

class WeatherRepository(
    private val weatherApiService: WeatherApiService,
    private val locationProvider: LocationProvider,
    private val preferenceManager: PreferenceManager
) {

    companion object {
        private const val TAG = "WeatherRepository"
        private const val LOCATION_FETCH_TIMEOUT_MS = 15000L
    }

    /**
     * Menyediakan Flow untuk data cuaca yang tersimpan di cache.
     * ViewModel akan mengamati ini untuk update UI yang instan.
     */
    fun getCachedWeather(): Flow<WeatherCacheData?> {
        return preferenceManager.weatherCacheFlow
    }

    /**
     * Mengambil data cuaca baru dari jaringan.
     * Fungsi ini akan mengambil lokasi, memanggil API, dan menyimpan hasilnya ke cache.
     */
    suspend fun fetchFreshWeatherData(): WeatherResult {
        if (!locationProvider.hasLocationPermission()) {
            return WeatherResult.Error("Izin lokasi tidak diberikan.")
        }
        val location: Location? = withTimeoutOrNull(LOCATION_FETCH_TIMEOUT_MS) {
            locationProvider.fetchCurrentLocation().firstOrNull()
        }
        if (location == null) {
            return WeatherResult.Error("Tidak bisa mendapatkan lokasi. Pastikan GPS aktif ya, Kapten!")
        }

        return try {
            val response = weatherApiService.getCurrentWeather(
                latitude = location.latitude,
                longitude = location.longitude,
                apiKey = BuildConfig.OPEN_WEATHER_API_KEY
            )

            if (response.isSuccessful && response.body() != null) {
                val weatherData = response.body()!!
                Log.i(TAG, "Berhasil mengambil data cuaca untuk ${weatherData.cityName}")

                // Simpan ke cache setelah berhasil dapat data baru
                saveWeatherDataToCache(weatherData)

                WeatherResult.Success(weatherData)
            } else {
                WeatherResult.Error("Gagal mengambil data cuaca: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            WeatherResult.Error("Error jaringan: Periksa koneksi internetmu ya, Kapten.")
        }
    }

    /**
     * Fungsi privat untuk mengubah data dari API menjadi format cache dan menyimpannya.
     */
    private suspend fun saveWeatherDataToCache(weatherData: WeatherApiResponse) {
        val cacheData = WeatherCacheData(
            cityName = weatherData.cityName,
            temperature = weatherData.mainWeatherData?.temperature?.let { "%.0f°C".format(it) },
            condition = weatherData.weatherDescriptions?.firstOrNull()?.description?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            iconCode = weatherData.weatherDescriptions?.firstOrNull()?.icon,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        preferenceManager.saveWeatherCache(cacheData)
        Log.i(TAG, "Data cuaca baru berhasil disimpan di cache.")
    }
}
