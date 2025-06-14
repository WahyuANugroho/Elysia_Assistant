package com.example.elysia_assistant.domain.usecase // Pastikan package ini sesuai ya, Kapten!

import android.app.Application
import android.net.Uri
import android.util.Log
import com.example.elysia_assistant.data.repository.IChatRepository // Menggunakan Interface
import com.example.elysia_assistant.domain.model.ChatHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileOutputStream
import java.io.IOException

/**
 * UseCase ini bertugas untuk mengambil semua riwayat chat dari repository
 * dan menuliskannya ke file yang dipilih pengguna (misalnya, JSON).
 */
class ExportChatHistoryUseCase(
    private val application: Application,
    private val chatRepository: IChatRepository // Dependensi ke ChatRepository
) {
    companion object { private const val TAG = "ExportChatUseCase" }

    suspend fun execute(uri: Uri, format: String): Boolean {
        Log.d(TAG, "Mengekspor ingatan ke format: $format")
        return try {
            // 1. Ambil semua pesan dari repository
            val allMessages = chatRepository.getAllMessagesForExport()

            if (allMessages.isEmpty()) {
                Log.w(TAG, "Tidak ada data untuk diekspor, Kapten!")
                return true // Dianggap sukses, tapi tidak ada file yang dibuat.
            }
            val chatHistory = ChatHistory(allMessages)

            // 2. Cek format yang diminta
            if (format.equals("json", ignoreCase = true)) {
                // 3. Ubah data menjadi string JSON
                val jsonString = withContext(Dispatchers.Default) {
                    Json { prettyPrint = true }.encodeToString(chatHistory)
                }
                // 4. Tulis string JSON ke file
                withContext(Dispatchers.IO) {
                    application.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { it.write(jsonString.toByteArray()) }
                    } ?: throw IOException("Tidak bisa membuka lokasi file untuk ditulis.")
                }
                Log.i(TAG, "Berhasil mengekspor ${allMessages.size} pesan ke JSON!")
                true
            } else {
                Log.w(TAG, "Format ekspor '$format' belum aku dukung, Kapten.")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Aduh, gagal saat mengekspor ingatan.", e)
            false
        }
    }
}
