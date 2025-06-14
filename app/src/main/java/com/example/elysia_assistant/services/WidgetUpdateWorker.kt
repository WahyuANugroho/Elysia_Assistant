package com.example.elysia_assistant.services

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
import com.example.elysia_assistant.MainActivity
import com.example.elysia_assistant.R
import com.example.elysia_assistant.data.local.PreferenceManager
import com.example.elysia_assistant.data.local.WeatherCacheData
import com.example.elysia_assistant.data.remote.NetworkModule
import com.example.elysia_assistant.data.repository.WeatherRepository
import com.example.elysia_assistant.data.repository.WeatherResult
import com.example.elysia_assistant.util.LocationProvider
import com.example.elysia_assistant.util.getWeatherIconResId
import com.example.elysia_assistant.widget.ElysiaWeatherWidgetProvider
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class WidgetUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "WidgetUpdateWorker"
        // --- PERBAIKAN DI SINI, KAPTEN! ---
        // Mendefinisikan konstanta yang hilang
        const val ACTION_WIDGET_CLICK = "com.example.elysia_assistant.ACTION_WIDGET_CLICK"
    }

    // Dependensi
    private val locationProvider: LocationProvider by lazy { LocationProvider(applicationContext) }
    private val weatherApiService by lazy { NetworkModule.provideWeatherApiService() }
    private val preferenceManager: PreferenceManager by lazy { PreferenceManager(applicationContext) }
    private val weatherRepository: WeatherRepository by lazy {
        WeatherRepository(weatherApiService, locationProvider, preferenceManager)
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: Pekerja widget-ku mulai beraksi, Kapten! Hihi~")
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val thisWidgetProvider = ComponentName(applicationContext, ElysiaWeatherWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidgetProvider)

        if (appWidgetIds.isEmpty()) {
            return Result.success()
        }

        when (val weatherResult = weatherRepository.fetchFreshWeatherData()) {
            is WeatherResult.Success -> {
                val latestCache = preferenceManager.weatherCacheFlow.firstOrNull()
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(appWidgetManager, appWidgetId, latestCache)
                }
                Log.i(TAG, "doWork: Semua widget berhasil diperbarui dengan info baru!")
                return Result.success()
            }
            is WeatherResult.Error -> {
                Log.e(TAG, "doWork: Aduh, gagal mengambil info cuaca: ${weatherResult.message}")
                val lastCache = preferenceManager.weatherCacheFlow.firstOrNull()
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(appWidgetManager, appWidgetId, lastCache, true, weatherResult.message)
                }
                return Result.retry()
            }
        }
    }

    private fun updateAppWidget(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        weatherCache: WeatherCacheData?,
        isError: Boolean = false,
        errorMessage: String? = "Gagal memuat"
    ) {
        val views = RemoteViews(applicationContext.packageName, R.layout.elysia_weather_widget)

        if (!isError && weatherCache != null) {
            views.setTextViewText(R.id.widget_city_name, weatherCache.cityName ?: "Lokasi ?")
            views.setTextViewText(R.id.widget_temperature, "${weatherCache.temperature ?: "--°"} / ${weatherCache.condition ?: "..."}")
            getWeatherIconResId(weatherCache.iconCode)?.let { iconRes ->
                views.setImageViewResource(R.id.widget_weather_icon, iconRes)
            } ?: views.setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_weather_unknown)

            val sdf = SimpleDateFormat("HH:mm, dd MMM", Locale("id", "ID"))
            val lastUpdatedText = "Update: ${sdf.format(Date(weatherCache.lastUpdatedTimestamp))}"
            views.setTextViewText(R.id.widget_last_updated, lastUpdatedText)
        } else {
            views.setTextViewText(R.id.widget_city_name, errorMessage ?: "Gagal memuat")
            views.setTextViewText(R.id.widget_temperature, "N/A")
            views.setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_weather_error)
            views.setTextViewText(R.id.widget_last_updated, "Coba lagi nanti")
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            action = ACTION_WIDGET_CLICK // Sekarang referensi ini sudah benar!
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, appWidgetId, intent, pendingIntentFlags)
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
