package com.example.elysia_assistant.data.repository

import android.location.Location
import android.util.Log
// Pastikan BuildConfig diimpor dari package aplikasi Anda yang benar
import com.example.elysia_assistant.BuildConfig
import com.example.elysia_assistant.data.remote.WeatherApiService
import com.example.elysia_assistant.domain.model.WeatherApiResponse // Data class Anda
import com.example.elysia_assistant.util.LocationProvider // Atau path LocationProvider Anda
import kotlinx.coroutines.flow.firstOrNull

// Sealed class untuk membungkus hasil operasi jaringan
sealed class WeatherResult {
    data class Success(val data: WeatherApiResponse) : WeatherResult()
    data class Error(val message: String, val cause: Throwable? = null) : WeatherResult()
    // Anda bisa menambahkan state Loading di sini jika ingin dikelola dari Repository
    // object Loading : WeatherResult()
}

class WeatherRepository(
    private val weatherApiService: WeatherApiService, // Akan di-inject atau dibuat via NetworkModule
    private val locationProvider: LocationProvider    // Akan di-inject atau dibuat
) {

    companion object {
        private const val TAG = "WeatherRepository"
    }

    suspend fun getCurrentWeatherForCurrentLocation(): WeatherResult {
        Log.d(TAG, "getCurrentWeatherForCurrentLocation: Attempting to fetch weather.")

        // 1. Cek izin lokasi terlebih dahulu melalui LocationProvider
        if (!locationProvider.hasLocationPermission()) {
            Log.w(TAG, "getCurrentWeatherForCurrentLocation: Location permission not granted.")
            return WeatherResult.Error("Izin lokasi tidak diberikan oleh pengguna.")
        }

        // 2. Dapatkan lokasi saat ini
        Log.d(TAG, "getCurrentWeatherForCurrentLocation: Fetching current location.")
        val location: Location? = try {
            locationProvider.fetchCurrentLocation().firstOrNull() // Mengambil satu emisi lokasi
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentWeatherForCurrentLocation: Exception while fetching location.", e)
            return WeatherResult.Error("Gagal mendapatkan lokasi (exception): ${e.message}", e)
        }

        if (location == null) {
            Log.w(TAG, "getCurrentWeatherForCurrentLocation: Location data is null.")
            return WeatherResult.Error("Tidak bisa mendapatkan data lokasi saat ini (hasil null dari provider).")
        }
        Log.d(TAG, "getCurrentWeatherForCurrentLocation: Location acquired: Lat=${location.latitude}, Lon=${location.longitude}")

        // 3. Jika lokasi didapat, panggil API cuaca
        Log.d(TAG, "getCurrentWeatherForCurrentLocation: Fetching weather data from API. API Key: ${BuildConfig.OPEN_WEATHER_API_KEY.take(5)}...") // Hanya log sebagian kecil API Key
        return try {
            val response = weatherApiService.getCurrentWeather(
                latitude = location.latitude,
                longitude = location.longitude,
                apiKey = BuildConfig.OPEN_WEATHER_API_KEY, // Menggunakan API Key dari BuildConfig
                units = "metric", // Untuk Celcius
                language = "id"   // Untuk deskripsi Bahasa Indonesia
            )

            Log.d(TAG, "getCurrentWeatherForCurrentLocation: API response code: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                response.body()?.let { weatherData ->
                    Log.d(TAG, "getCurrentWeatherForCurrentLocation: API call successful. City: ${weatherData.cityName}")
                    WeatherResult.Success(weatherData)
                } ?: run {
                    Log.e(TAG, "getCurrentWeatherForCurrentLocation: API response body is null despite successful HTTP call.")
                    WeatherResult.Error("Respons API cuaca kosong meskipun berhasil (body null).")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "getCurrentWeatherForCurrentLocation: API call failed. Code: ${response.code()}, Message: ${response.message()}, ErrorBody: $errorBody")
                WeatherResult.Error("Gagal mengambil data cuaca: HTTP ${response.code()} - ${response.message()}. Detail: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentWeatherForCurrentLocation: Network exception or other error during API call.", e)
            WeatherResult.Error(
                "Error jaringan atau masalah lain saat mengambil cuaca: ${e.message}",
                e
            )
        }
    }
}
