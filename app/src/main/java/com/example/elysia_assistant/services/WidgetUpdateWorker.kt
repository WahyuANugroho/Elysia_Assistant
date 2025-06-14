package com.example.elysia_assistant.services // Sesuaikan dengan package Anda

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.elysia_assistant.MainActivity // Pastikan MainActivity diimpor
import com.example.elysia_assistant.R // Impor R dari package aplikasi Anda
import com.example.elysia_assistant.data.local.PreferenceManager
import com.example.elysia_assistant.data.local.WeatherCacheData
import com.example.elysia_assistant.data.remote.NetworkModule
import com.example.elysia_assistant.data.repository.WeatherRepository
import com.example.elysia_assistant.data.repository.WeatherResult
import com.example.elysia_assistant.util.LocationProvider
import com.example.elysia_assistant.util.getWeatherIconResId // Impor fungsi utilitas pemetaan ikon
import com.example.elysia_assistant.widget.ElysiaWeatherWidgetProvider // Impor Widget Provider Anda
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class WidgetUpdateWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "WidgetUpdateWorker"
        const val ACTION_WIDGET_CLICK = "com.example.elysia_assistant.ACTION_WIDGET_CLICK"
    }

    // Dependensi ini idealnya di-inject menggunakan Hilt untuk Worker agar lebih mudah di-test.
    // Untuk sekarang, kita buat instance manual.
    private val locationProvider: LocationProvider by lazy { LocationProvider(appContext) }
    private val weatherApiService by lazy { NetworkModule.provideWeatherApiService() }
    private val weatherRepository: WeatherRepository by lazy {
        WeatherRepository(weatherApiService, locationProvider)
    }
    private val preferenceManager: PreferenceManager by lazy { PreferenceManager(appContext) }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: Pekerja widget-ku mulai beraksi, Kapten! Hihi~")

        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        val thisWidgetProvider = ComponentName(appContext, ElysiaWeatherWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidgetProvider)

        if (appWidgetIds.isEmpty()) {
            Log.d(TAG, "doWork: Tidak ada widget aktif, aku istirahat dulu ya~")
            return Result.success()
        }

        Log.d(TAG, "doWork: Menemukan ${appWidgetIds.size} widget! Saatnya mencari info cuaca!")

        // Ambil data cuaca dari repository
        when (val weatherResult = weatherRepository.getCurrentWeatherForCurrentLocation()) {
            is WeatherResult.Success -> {
                val weatherData = weatherResult.data
                val cityName = weatherData.cityName ?: "Lokasi ?"
                val temperature = weatherData.mainWeatherData?.temperature?.let { "%.0f°C".format(it) } ?: "--°"
                val condition = weatherData.weatherDescriptions?.firstOrNull()?.description?.replaceFirstChar { it.titlecase(Locale.getDefault()) } ?: "" // Deskripsi dengan huruf kapital di awal
                val iconCode = weatherData.weatherDescriptions?.firstOrNull()?.icon
                val lastUpdatedTimestamp = System.currentTimeMillis()

                // Simpan data ke cache DataStore
                val cacheData = WeatherCacheData(
                    cityName = cityName,
                    temperature = "$temperature, $condition",
                    iconCode = iconCode,
                    lastUpdatedTimestamp = lastUpdatedTimestamp
                )
                preferenceManager.saveWeatherCache(cacheData)
                Log.d(TAG, "doWork: Data cuaca berhasil disimpan: $cacheData")

                // Update semua widget yang aktif dengan data baru
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(appContext, appWidgetManager, appWidgetId, cacheData)
                }
                Log.i(TAG, "doWork: Semua widget berhasil diperbarui dengan info baru!")
                return Result.success()
            }
            is WeatherResult.Error -> {
                Log.e(TAG, "doWork: Aduh, gagal mengambil info cuaca: ${weatherResult.message}")
                // Jika gagal, coba update widget dengan data cache terakhir yang valid
                val lastCache = preferenceManager.weatherCacheFlow.firstOrNull()
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(appContext, appWidgetManager, appWidgetId, lastCache, true, weatherResult.message)
                }
                return Result.retry() // Coba lagi nanti
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        weatherCache: WeatherCacheData?,
        isError: Boolean = false,
        errorMessage: String? = "Gagal memuat"
    ) {
        Log.d(TAG, "updateAppWidget: Memperbarui widget ID $appWidgetId. Status Error: $isError")
        // Menggunakan layout XML untuk widget
        val views = RemoteViews(context.packageName, R.layout.elysia_weather_widget)

        if (!isError && weatherCache != null) {
            // Mengisi data jika berhasil
            views.setTextViewText(R.id.widget_city_name, weatherCache.cityName ?: "Lokasi ?")
            views.setTextViewText(R.id.widget_temperature, weatherCache.temperature ?: "--°C")

            // Menggunakan fungsi utilitas untuk mendapatkan ikon yang benar
            getWeatherIconResId(weatherCache.iconCode)?.let { iconRes ->
                views.setImageViewResource(R.id.widget_weather_icon, iconRes)
            } ?: views.setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_weather_unknown) // Pastikan drawable ini ada

            val sdf = SimpleDateFormat("HH:mm, dd MMM", Locale("id", "ID")) // Format waktu Indonesia
            val lastUpdatedText = "Update: ${sdf.format(Date(weatherCache.lastUpdatedTimestamp))}"
            views.setTextViewText(R.id.widget_last_updated, lastUpdatedText)
        } else {
            // Tampilan jika terjadi error atau tidak ada data
            views.setTextViewText(R.id.widget_city_name, errorMessage ?: "Gagal memuat")
            views.setTextViewText(R.id.widget_temperature, "N/A")
            views.setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_weather_error) // Pastikan drawable ini ada
            views.setTextViewText(R.id.widget_last_updated, "Coba lagi nanti")
        }

        // Setel PendingIntent agar widget bisa diklik untuk membuka aplikasi
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_WIDGET_CLICK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, pendingIntentFlags)
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent) // ID dari layout root widget

        // Instruksikan AppWidgetManager untuk melakukan update
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "updateAppWidget: Tampilan widget ID $appWidgetId selesai diperbarui!")
    }
}
