package com.example.elysia_assistant.widget // Sesuaikan dengan package Anda

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import com.example.elysia_assistant.MainActivity
import com.example.elysia_assistant.R
import com.example.elysia_assistant.services.WidgetUpdateWorker

class ElysiaWeatherWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "ElysiaWidgetProvider"
    }

    /**
     * Dipanggil saat widget pertama kali diletakkan di homescreen dan saat interval update
     * yang ditentukan di widget_info.xml (yang kita set ke 0) tercapai.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate dipanggil untuk widget ID(s): ${appWidgetIds.joinToString()}")
        // Kita tidak akan melakukan pekerjaan berat di sini.
        // Kita delegasikan semua pekerjaan update ke WidgetUpdateWorker kita.
        // Untuk sekarang, kita bisa mengupdate dengan tampilan 'Loading...' awal.
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /**
     * Dipanggil saat widget pertama kali ditambahkan ke homescreen.
     */
    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled: Widget pertama ditambahkan, menjadwalkan worker...")
        // Di sini kita akan menjadwalkan WidgetUpdateWorker secara periodik.
        // TODO: Tambahkan logika untuk menjadwalkan PeriodicWorkRequest untuk WidgetUpdateWorker.
    }

    /**
     * Dipanggil saat instance terakhir dari widget ini dihapus dari homescreen.
     */
    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled: Widget terakhir dihapus, membatalkan worker...")
        // Di sini kita akan membatalkan worker yang sudah dijadwalkan.
        // TODO: Tambahkan logika untuk membatalkan WorkManager.
    }
}

// Fungsi helper internal untuk menampilkan tampilan awal atau loading
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Log.d("ElysiaWidgetProvider", "updateAppWidget (initial): Memperbarui widget ID $appWidgetId dengan tampilan loading.")
    val views = RemoteViews(context.packageName, R.layout.elysia_weather_widget)
    views.setTextViewText(R.id.widget_city_name, "Memuat...")
    views.setTextViewText(R.id.widget_temperature, "--°C")
    views.setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_weather_unknown) // Ikon default

    // Setel PendingIntent agar widget bisa diklik
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
    val pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, pendingIntentFlags)
    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
