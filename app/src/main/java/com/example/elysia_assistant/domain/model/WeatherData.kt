package com.example.elysia_assistant.domain.model // Atau data.remote.model

import com.google.gson.annotations.SerializedName

// Kelas utama untuk respons API
data class WeatherApiResponse(
    @SerializedName("coord") val coordinates: Coordinates?,
    @SerializedName("weather") val weatherDescriptions: List<WeatherDescription>?,
    @SerializedName("main") val mainWeatherData: MainWeatherData?,
    @SerializedName("wind") val windData: WindData?,
    @SerializedName("sys") val systemData: SystemData?,
    @SerializedName("name") val cityName: String?, // Nama kota
    @SerializedName("cod") val responseCode: Int? // Kode respons HTTP, e.g., 200
)

data class Coordinates(
    @SerializedName("lon") val longitude: Double?,
    @SerializedName("lat") val latitude: Double?
)

data class WeatherDescription(
    @SerializedName("id") val id: Int?,
    @SerializedName("main") val main: String?, // Misal: "Rain", "Clouds", "Clear"
    @SerializedName("description") val description: String?, // Deskripsi lebih detail
    @SerializedName("icon") val icon: String? // Kode ikon cuaca (misal: "01d", "10n")
)

data class MainWeatherData(
    @SerializedName("temp") val temperature: Double?, // Suhu (default Kelvin, bisa diubah dengan &units=metric untuk Celcius)
    @SerializedName("feels_like") val feelsLike: Double?,
    @SerializedName("temp_min") val tempMin: Double?,
    @SerializedName("temp_max") val tempMax: Double?,
    @SerializedName("pressure") val pressure: Int?,
    @SerializedName("humidity") val humidity: Int?
)

data class WindData(
    @SerializedName("speed") val speed: Double?, // Kecepatan angin
    @SerializedName("deg") val degree: Int? // Arah angin dalam derajat
)

data class SystemData(
    @SerializedName("country") val countryCode: String?, // Kode negara, misal "ID"
    @SerializedName("sunrise") val sunriseTimestamp: Long?, // Waktu matahari terbit (Unix timestamp)
    @SerializedName("sunset") val sunsetTimestamp: Long? // Waktu matahari terbenam (Unix timestamp)
)