package com.example.elysia_assistant.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.* // Pastikan dependensi play-services-location sudah ada
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class LocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        private const val TAG = "LocationProvider"
    }

    fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "hasLocationPermission: Fine granted: $fineLocationGranted, Coarse granted: $coarseLocationGranted")
        return fineLocationGranted || coarseLocationGranted
    }

    //SuppressLint("MissingPermission") karena permission akan (dan harus) dicek sebelum pemanggilan fungsi ini.
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(): Flow<Location?> = callbackFlow {
        Log.d(TAG, "fetchCurrentLocation: Attempting to get current location.")

        if (!hasLocationPermission()) {
            Log.w(TAG, "fetchCurrentLocation: Location permission not granted.")
            trySend(null) // Kirim null jika tidak ada izin
            close()       // Tutup flow
            return@callbackFlow
        }

        // Konfigurasi permintaan lokasi untuk akurasi tinggi dan hanya satu update.
        // Timeout bisa diatur di level FusedLocationProviderClient atau dengan coroutine timeout.
        // Di sini kita mengandalkan setMaxUpdates(1) dan penanganan di callback.
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L) // Interval yang diinginkan 10 detik
            .setMinUpdateIntervalMillis(5000L) // Interval minimal 5 detik
            .setMaxUpdates(1) // Hanya meminta satu update lokasi
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                Log.d(TAG, "onLocationResult: Received location: $lastLocation")
                if (lastLocation != null) {
                    launch { trySend(lastLocation).isSuccess } // Kirim lokasi yang valid
                } else {
                    Log.w(TAG, "onLocationResult: Last location is null.")
                    launch { trySend(null).isSuccess } // Kirim null jika lokasi tidak ditemukan
                }
                close() // Tutup flow setelah mendapatkan hasil (lokasi atau null)
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                Log.d(TAG, "onLocationAvailability: isLocationAvailable = ${locationAvailability.isLocationAvailable}")
                if (!locationAvailability.isLocationAvailable) {
                    Log.w(TAG, "Location is not available according to onLocationAvailability.")
                    // Pertimbangkan untuk mengirim null dan menutup flow jika ini terjadi sebelum onLocationResult
                    // Namun, biasanya kita menunggu onLocationResult atau kegagalan request.
                }
            }
        }

        Log.d(TAG, "Requesting location updates from FusedLocationProviderClient...")
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() // Callback akan dijalankan di main looper
        ).addOnFailureListener { e ->
            Log.e(TAG, "Failed to request location updates from FusedLocationProviderClient", e)
            launch { trySend(null).isSuccess } // Kirim null jika gagal memulai permintaan update
            close(e) // Tutup flow dengan error
        }.addOnCanceledListener {
            Log.w(TAG, "Location updates request was canceled.")
            launch { trySend(null).isSuccess } // Kirim null jika permintaan dibatalkan
            close() // Tutup flow
        }

        // Hentikan update lokasi saat flow ditutup/dibatalkan dari luar (misalnya, saat coroutine scope dibatalkan)
        awaitClose {
            Log.d(TAG, "fetchCurrentLocation: Flow is closing. Removing location updates.")
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
