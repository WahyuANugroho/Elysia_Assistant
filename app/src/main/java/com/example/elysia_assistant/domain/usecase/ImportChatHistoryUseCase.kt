package com.example.elysia_assistant.domain.usecase // Pastikan package ini sesuai ya, Kapten!

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.elysia_assistant.data.repository.IChatRepository
import com.example.elysia_assistant.domain.model.ChatHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * UseCase ini bertugas untuk membaca file riwayat chat (JSON atau Excel)
 * dan menyimpannya ke dalam database melalui repository.
 */
class ImportChatHistoryUseCase(
    private val application: Application,
    private val chatRepository: IChatRepository
) {
    companion object { private const val TAG = "ImportChatUseCase" }

    suspend fun execute(uri: Uri): Boolean {
        // Mendeteksi tipe file dari ekstensinya
        val fileName = getFileName(uri)
        val extension = fileName?.substringAfterLast('.', "")?.lowercase()
        Log.d(TAG, "Memulai impor dari URI: $uri, Ekstensi: .$extension")

        return when (extension) {
            "json" -> importFromJson(uri)
            "xlsx", "xls" -> {
                Log.w(TAG, "Impor Excel belum aku dukung, Kapten.")
                false
            }
            else -> {
                Log.e(TAG, "Tipe file tidak didukung: .$extension")
                false
            }
        }
    }

    private suspend fun importFromJson(uri: Uri): Boolean {
        Log.d(TAG, "Mencoba mengimpor dari JSON...")
        return try {
            // 1. Baca isi file JSON menjadi string
            val jsonString = withContext(Dispatchers.IO) {
                application.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).readText()
                } ?: throw IOException("Tidak bisa membuka file dari URI.")
            }
            if (jsonString.isBlank()) {
                Log.e(TAG, "File JSON yang dipilih kosong, Kapten.")
                return false
            }

            // 2. Ubah string JSON menjadi objek data kita
            val chatHistory = withContext(Dispatchers.Default) {
                Json { ignoreUnknownKeys = true }.decodeFromString<ChatHistory>(jsonString)
            }

            // 3. Simpan ke database melalui repository, hapus yang lama dulu
            chatRepository.insertAllImportedMessages(chatHistory.messages, clearPrevious = true)

            Log.i(TAG, "Berhasil impor dan menyimpan ${chatHistory.messages.size} pesan dari JSON!")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Aduh, gagal saat impor dari JSON.", e)
            false
        }
    }

    // Fungsi helper untuk mendapatkan nama file dari URI
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            application.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
