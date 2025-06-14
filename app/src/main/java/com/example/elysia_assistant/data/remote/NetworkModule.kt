package com.example.elysia_assistant.data.remote

import com.example.elysia_assistant.BuildConfig as AppBuildConfig // Alias untuk BuildConfig aplikasi Anda
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    // Fungsi untuk menyediakan OkHttpClient yang sudah dikonfigurasi
    private fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Atur level logging berdasarkan apakah ini build DEBUG atau tidak
            level = if (AppBuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Tambahkan logging interceptor
            .connectTimeout(30, TimeUnit.SECONDS) // Batas waktu koneksi
            .readTimeout(30, TimeUnit.SECONDS)    // Batas waktu membaca data
            .writeTimeout(30, TimeUnit.SECONDS)   // Batas waktu menulis data
            .build()
    }

    // Fungsi untuk menyediakan instance Retrofit
    private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(WeatherApiService.BASE_URL) // BASE_URL dari interface WeatherApiService Anda
            .client(okHttpClient)                // Menggunakan OkHttpClient yang sudah dikonfigurasi
            .addConverterFactory(GsonConverterFactory.create()) // Menggunakan Gson untuk parsing JSON
            .build()
    }

    // Fungsi publik untuk menyediakan WeatherApiService yang siap pakai
    // OkHttpClient bisa di-inject jika menggunakan DI, atau default ke provideOkHttpClient()
    // untuk penggunaan sederhana.
    fun provideWeatherApiService(okHttpClient: OkHttpClient = provideOkHttpClient()): WeatherApiService {
        val retrofit = provideRetrofit(okHttpClient)
        return retrofit.create(WeatherApiService::class.java)
    }
}