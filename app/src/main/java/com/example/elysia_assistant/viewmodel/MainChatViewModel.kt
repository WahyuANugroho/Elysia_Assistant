package com.example.elysia_assistant.viewmodel // Sesuaikan dengan package Anda

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elysia_assistant.data.local.PreferenceManager
import com.example.elysia_assistant.data.local.database.AppDatabase
import com.example.elysia_assistant.data.remote.NetworkModule
import com.example.elysia_assistant.data.repository.ChatRepository
import com.example.elysia_assistant.data.repository.IChatRepository
import com.example.elysia_assistant.data.repository.WeatherRepository
import com.example.elysia_assistant.data.repository.WeatherResult
import com.example.elysia_assistant.domain.model.ChatMessage
import com.example.elysia_assistant.util.LocationProvider
import com.example.elysia_assistant.util.getWeatherIconResId
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainChatViewModel(
    private val chatRepository: IChatRepository,
    private val weatherRepository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val preferenceManager: PreferenceManager // Menambahkan preferenceManager sebagai dependensi
) : ViewModel() {

    // --- State untuk Cuaca ---
    // ... (StateFlows cuaca tetap sama)
    private val _locationName = MutableStateFlow<String?>("...")
    val locationName: StateFlow<String?> = _locationName.asStateFlow()
    private val _temperature = MutableStateFlow<String?>("")
    val temperature: StateFlow<String?> = _temperature.asStateFlow()
    private val _weatherCondition = MutableStateFlow<String?>("")
    val weatherCondition: StateFlow<String?> = _weatherCondition.asStateFlow()
    private val _weatherIconResId = MutableStateFlow<Int?>(null)
    val weatherIconResId: StateFlow<Int?> = _weatherIconResId.asStateFlow()
    private val _isLoadingWeather = MutableStateFlow(true)
    val isLoadingWeather: StateFlow<Boolean> = _isLoadingWeather.asStateFlow()
    private val _weatherError = MutableStateFlow<String?>(null)
    val weatherError: StateFlow<String?> = _weatherError.asStateFlow()

    private val _shouldRequestLocationPermission = MutableStateFlow(false)
    val shouldRequestLocationPermission: StateFlow<Boolean> = _shouldRequestLocationPermission.asStateFlow()

    // --- State untuk Chat ---
    val chatMessages: StateFlow<List<ChatMessage>> = chatRepository.getChatMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeWeatherCache()
        checkAndFetchWeatherDataIfNeeded()
    }

    private fun observeWeatherCache() {
        viewModelScope.launch {
            preferenceManager.weatherCacheFlow.collect { cache ->
                if (cache != null) {
                    Log.d(TAG, "Cache cuaca diperbarui: ${cache.cityName}")
                    _locationName.value = cache.cityName
                    _temperature.value = cache.temperature
                    _weatherCondition.value = cache.condition
                    _weatherIconResId.value = getWeatherIconResId(cache.iconCode)
                    _weatherError.value = null
                    _isLoadingWeather.value = false
                }
            }
        }
    }

    private fun checkAndFetchWeatherDataIfNeeded() {
        viewModelScope.launch {
            val cache = preferenceManager.weatherCacheFlow.firstOrNull()
            val isCacheStale = cache == null || (System.currentTimeMillis() - cache.lastUpdatedTimestamp > 30 * 60 * 1000) // 30 menit

            if (isCacheStale) {
                Log.d(TAG, "Cache sudah lama atau tidak ada. Mencoba mengambil data baru.")
                if (locationProvider.hasLocationPermission()) {
                    fetchFreshData()
                } else {
                    _shouldRequestLocationPermission.value = true
                }
            } else {
                Log.d(TAG, "Cache masih baru. Tidak perlu mengambil data baru.")
                _isLoadingWeather.value = false
            }
        }
    }

    private fun fetchFreshData() {
        viewModelScope.launch {
            _isLoadingWeather.value = true
            // Panggilan ini sekarang akan valid!
            when (val result = weatherRepository.fetchFreshWeatherData()) {
                is WeatherResult.Success -> {
                    // Data sudah disimpan ke cache oleh repository,
                    // dan akan di-update ke UI melalui `observeWeatherCache`.
                    // Jadi di sini kita tidak perlu melakukan apa-apa lagi.
                    Log.i(TAG, "Pengambilan data baru berhasil.")
                }
                is WeatherResult.Error -> {
                    _weatherError.value = result.message
                    Log.e(TAG, "Gagal mengambil data baru: ${result.message}")
                }
            }
            _isLoadingWeather.value = false
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _shouldRequestLocationPermission.value = false
        if (granted) {
            fetchFreshData()
        } else {
            _weatherError.value = "Izin lokasi ditolak."
        }
    }

    fun sendMessage(text: String) { /* ... Implementasi sendMessage Anda ... */ }

    companion object {
        private const val TAG = "MainChatViewModel"

        fun Factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainChatViewModel::class.java)) {
                        // Membuat semua dependensi yang dibutuhkan
                        val preferenceManager = PreferenceManager(application)
                        val locationProvider = LocationProvider(application)
                        val weatherApiService = NetworkModule.provideWeatherApiService()
                        val weatherRepository = WeatherRepository(weatherApiService, locationProvider, preferenceManager)
                        val chatDao = AppDatabase.getDatabase(application).chatMessageDao()
                        val chatRepository = ChatRepository(chatDao)

                        return MainChatViewModel(chatRepository, weatherRepository, locationProvider, preferenceManager) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
