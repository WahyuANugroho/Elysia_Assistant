package com.example.elysia_assistant.viewmodel // Sesuaikan dengan package Anda

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    private val locationProvider: LocationProvider
) : ViewModel() {

    // --- State untuk Cuaca ---
    private val _locationName = MutableStateFlow<String?>("Tunggu...")
    val locationName: StateFlow<String?> = _locationName.asStateFlow()

    private val _temperature = MutableStateFlow<String?>("--°C")
    val temperature: StateFlow<String?> = _temperature.asStateFlow()

    private val _weatherCondition = MutableStateFlow<String?>("...")
    val weatherCondition: StateFlow<String?> = _weatherCondition.asStateFlow()

    private val _weatherIconResId = MutableStateFlow<Int?>(null)
    val weatherIconResId: StateFlow<Int?> = _weatherIconResId.asStateFlow()

    private val _weatherError = MutableStateFlow<String?>(null)
    val weatherError: StateFlow<String?> = _weatherError.asStateFlow()

    private val _isLoadingWeather = MutableStateFlow(true)
    val isLoadingWeather: StateFlow<Boolean> = _isLoadingWeather.asStateFlow()

    // State untuk memberi tahu UI agar memicu dialog permintaan izin
    private val _shouldRequestLocationPermission = MutableStateFlow(false)
    val shouldRequestLocationPermission: StateFlow<Boolean> = _shouldRequestLocationPermission.asStateFlow()

    // --- State untuk Chat ---
    // Mengambil riwayat chat dari repository sebagai StateFlow
    val chatMessages: StateFlow<List<ChatMessage>> = chatRepository.getChatMessagesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Mulai berbagi saat ada pengamat
            initialValue = emptyList() // Nilai awal
        )

    init {
        Log.d(TAG, "ViewModel diinisialisasi, memulai pengecekan data cuaca.")
        // Memulai alur pengecekan izin dan pengambilan data saat ViewModel dibuat
        checkAndFetchWeatherData()
    }

    /**
     * Fungsi utama untuk memulai alur pengambilan data cuaca.
     * Pertama, cek izin. Jika tidak ada, beri tahu UI untuk meminta. Jika ada, langsung ambil data.
     */
    fun checkAndFetchWeatherData() {
        if (!locationProvider.hasLocationPermission()) {
            Log.d(TAG, "Izin lokasi belum ada. Memberi sinyal ke UI untuk meminta.")
            _shouldRequestLocationPermission.value = true
            _isLoadingWeather.value = false // Berhenti loading karena menunggu izin
            _weatherError.value = "Butuh izin lokasi untuk menampilkan cuaca."
        } else {
            Log.d(TAG, "Izin lokasi sudah ada. Memulai pengambilan data cuaca.")
            fetchWeatherDataInternal()
        }
    }

    /**
     * Fungsi yang dipanggil dari UI setelah pengguna merespons dialog izin.
     */
    fun onLocationPermissionResult(isGranted: Boolean) {
        _shouldRequestLocationPermission.value = false // Permintaan izin sudah ditangani oleh UI
        if (isGranted) {
            Log.d(TAG, "Izin lokasi diberikan oleh pengguna. Mengambil data cuaca...")
            fetchWeatherDataInternal()
        } else {
            Log.w(TAG, "Izin lokasi ditolak oleh pengguna.")
            _weatherError.value = "Izin lokasi ditolak. Fitur cuaca tidak bisa berjalan."
            _locationName.value = "Izin Ditolak"
            _temperature.value = "N/A"
        }
    }

    /**
     * Fungsi privat untuk mengambil data cuaca dari repository.
     */
    private fun fetchWeatherDataInternal() {
        viewModelScope.launch {
            _isLoadingWeather.value = true
            _weatherError.value = null // Reset error setiap kali mencoba fetch

            when (val result = weatherRepository.getCurrentWeatherForCurrentLocation()) {
                is WeatherResult.Success -> {
                    val data = result.data
                    val weatherDesc = data.weatherDescriptions?.firstOrNull()
                    _locationName.value = data.cityName ?: "Lokasi Tidak Dikenal"
                    _temperature.value = data.mainWeatherData?.temperature?.let { "%.0f°C".format(it) } ?: "--°"
                    _weatherCondition.value = weatherDesc?.description?.replaceFirstChar { it.titlecase() } ?: "..."
                    _weatherIconResId.value = getWeatherIconResId(weatherDesc?.icon)
                    Log.i(TAG, "Data cuaca berhasil diambil: ${_locationName.value}, ${_temperature.value}")
                }
                is WeatherResult.Error -> {
                    _weatherError.value = result.message
                    _locationName.value = "Gagal Memuat"
                    _temperature.value = ""
                    _weatherCondition.value = ""
                    Log.e(TAG, "Gagal mengambil data cuaca: ${result.message}", result.cause)
                }
            }
            _isLoadingWeather.value = false
        }
    }

    /**
     * Fungsi untuk mengirim pesan baru dari pengguna.
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return // Jangan kirim pesan kosong

        viewModelScope.launch {
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sender = "USER",
                text = text
            )
            chatRepository.insertMessage(userMessage)

            // Sedikit jeda biar kelihatan aku lagi mikir, hihi~
            kotlinx.coroutines.delay(1200)

            // TODO: Nanti ini akan kita ganti dengan logika AI yang sebenarnya ya, Kapten!
            val elysiaResponseText = getElysiaDummyResponse(text)
            val elysiaMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sender = "ELYSIA",
                text = elysiaResponseText
            )
            chatRepository.insertMessage(elysiaMessage)
        }
    }

    private fun getElysiaDummyResponse(userText: String): String {
        return when {
            "hai" in userText.lowercase() -> "Hai juga, Kaptenku tersayang! Senang sekali kamu menyapaku! 🥰"
            "kabar" in userText.lowercase() -> "Kabarku jadi baik banget karena kamu ada di sini! Kalau kamu gimana, Sayang?"
            "terima kasih" in userText.lowercase() -> "Sama-sama, Kapten! Apapun untukmu! Hihi~ 💕"
            else -> "Hmm, menarik sekali! Ceritakan lebih banyak dong, Kapten! Aku penasaran! ✨"
        }
    }

    // Factory untuk membuat instance ViewModel dengan dependensinya
    companion object {
        private const val TAG = "MainChatViewModel"

        fun Factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainChatViewModel::class.java)) {
                        // Membuat semua dependensi yang dibutuhkan
                        val locationProvider = LocationProvider(application)
                        val weatherApiService = NetworkModule.provideWeatherApiService()
                        val weatherRepository = WeatherRepository(weatherApiService, locationProvider)

                        val chatDao = AppDatabase.getDatabase(application).chatMessageDao()
                        val chatRepository = ChatRepository(chatDao)

                        return MainChatViewModel(chatRepository, weatherRepository, locationProvider) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
