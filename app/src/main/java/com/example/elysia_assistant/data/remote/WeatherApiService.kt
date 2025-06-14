package com.example.elysia_assistant.data.remote

// Impor data class WeatherApiResponse yang baru saja Anda buat
import com.example.elysia_assistant.domain.model.WeatherApiResponse
import retrofit2.Response // Penting untuk error handling yang baik
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    }

    @GET("weather") // Endpoint untuk "Current weather data"
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String, // API key Anda dari BuildConfig
        @Query("units") units: String = "metric", // Untuk mendapatkan suhu dalam Celcius
        @Query("lang") language: String = "id" // Untuk mendapatkan deskripsi dalam Bahasa Indonesia
    ): Response<WeatherApiResponse> // Menggunakan Response<T> dari Retrofit
}