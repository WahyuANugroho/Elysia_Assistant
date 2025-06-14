package com.example.elysia_assistant.viewmodel // Sesuaikan dengan package Anda

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elysia_assistant.data.local.database.AppDatabase // Impor AppDatabase
import com.example.elysia_assistant.data.repository.ChatRepository // Impor ChatRepository
import com.example.elysia_assistant.data.repository.IChatRepository // Impor Interface jika digunakan
import com.example.elysia_assistant.domain.usecase.ExportChatHistoryUseCase
import com.example.elysia_assistant.domain.usecase.ImportChatHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val application: Application,
    private val exportChatHistoryUseCase: ExportChatHistoryUseCase,
    private val importChatHistoryUseCase: ImportChatHistoryUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"

        fun Factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                        // Membuat instance dependensi
                        val chatDao = AppDatabase.getDatabase(application).chatMessageDao()
                        val chatRepository: IChatRepository = ChatRepository(chatDao)

                        val exportUseCase = ExportChatHistoryUseCase(application, chatRepository)
                        val importUseCase = ImportChatHistoryUseCase(application, chatRepository)

                        return SettingsViewModel(application, exportUseCase, importUseCase) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }

    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus.asStateFlow()

    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus: StateFlow<String?> = _importStatus.asStateFlow()

    fun onExportChatHistory(uri: Uri?, format: String = "json") {
        if (uri == null) {
            _exportStatus.value = "Ekspor dibatalkan atau gagal."
            Log.w(TAG, "Export URI is null.")
            return
        }
        _exportStatus.value = "Mengekspor ingatan ($format)..."
        viewModelScope.launch {
            val success = exportChatHistoryUseCase.execute(uri, format)
            if (success) {
                _exportStatus.value = "Ingatan berhasil diekspor!"
            } else {
                _exportStatus.value = "Gagal mengekspor ingatan."
            }
        }
    }

    fun onImportChatHistory(uri: Uri?) {
        if (uri == null) {
            _importStatus.value = "Impor dibatalkan."
            Log.w(TAG, "Import URI is null.")
            return
        }
        _importStatus.value = "Mengimpor ingatan..."
        viewModelScope.launch {
            val success = importChatHistoryUseCase.execute(uri)
            if (success) {
                _importStatus.value = "Ingatan berhasil diimpor!"
            } else {
                _importStatus.value = "Gagal mengimpor ingatan. Pastikan format file benar ya, Kapten."
            }
        }
    }

    // Fungsi untuk mereset status setelah ditampilkan di UI (misalnya di Snackbar)
    fun clearExportStatus() {
        _exportStatus.value = null
    }

    fun clearImportStatus() {
        _importStatus.value = null
    }
}
