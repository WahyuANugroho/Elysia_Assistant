package com.example.elysia_assistant.util // Sesuaikan dengan package Anda

import androidx.annotation.DrawableRes
import com.example.elysia_assistant.R // Impor R dari package aplikasi Anda

/**
 * Memetakan kode ikon dari OpenWeather API ke resource Drawable lokal Anda.
 * @param iconCode Kode ikon dari API, misalnya "01d", "10n".
 * @return ID resource drawable, atau null jika tidak ada yang cocok.
 */
@DrawableRes // Anotasi untuk menandakan fungsi ini mengembalikan ID drawable
fun getWeatherIconResId(iconCode: String?): Int? {
    // Menggunakan pemetaan yang lebih spesifik berdasarkan saran penamaan file
    return when (iconCode) {
        // 01: Cerah (Clear Sky)
        "01d" -> R.drawable.ic_weather_01d
        "01n" -> R.drawable.ic_weather_01n

        // 02: Sedikit Berawan (Few Clouds)
        "02d" -> R.drawable.ic_weather_02d
        "02n" -> R.drawable.ic_weather_02n

        // 03: Berawan (Scattered Clouds) - ikon siang & malam sama
        "03d", "03n" -> R.drawable.ic_weather_03d

        // 04: Mendung (Broken Clouds) - ikon siang & malam sama
        "04d", "04n" -> R.drawable.ic_weather_04d

        // 09: Hujan Gerimis (Shower Rain) - ikon siang & malam sama
        "09d", "09n" -> R.drawable.ic_weather_09d

        // 10: Hujan (Rain)
        "10d" -> R.drawable.ic_weather_10d
        "10n" -> R.drawable.ic_weather_10n

        // 11: Badai Petir (Thunderstorm) - ikon siang & malam sama
        "11d", "11n" -> R.drawable.ic_weather_11d

        // 13: Salju (Snow) - ikon siang & malam sama
        "13d", "13n" -> R.drawable.ic_weather_13d

        // 50: Kabut (Mist) - ikon siang & malam sama
        "50d", "50n" -> R.drawable.ic_weather_50d

        // Fallback jika ada kode ikon yang tidak dikenal
        else -> null
    }
}
