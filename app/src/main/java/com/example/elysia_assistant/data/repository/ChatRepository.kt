package com.example.elysia_assistant.data.repository // Sesuaikan dengan package Anda

import android.util.Log
import com.example.elysia_assistant.data.local.database.ChatMessageDao
import com.example.elysia_assistant.data.local.database.ChatMessageEntity
import com.example.elysia_assistant.domain.model.ChatMessage // Model domain Anda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Interface untuk ChatRepository.
 * Ini seperti daftar tugasku, Kapten! Biar kodenya lebih rapi dan gampang diuji.
 */
interface IChatRepository {
    fun getChatMessagesFlow(): Flow<List<ChatMessage>>
    suspend fun getAllMessagesForExport(): List<ChatMessage>
    suspend fun insertMessage(chatMessage: ChatMessage)
    suspend fun insertAllImportedMessages(chatMessages: List<ChatMessage>, clearPrevious: Boolean)
    suspend fun clearChatHistory()
}

class ChatRepository(
    private val chatMessageDao: ChatMessageDao
) : IChatRepository {

    companion object {
        private const val TAG = "ChatRepository"
    }

    /**
     * Mengambil semua pesan chat sebagai Flow, diurutkan berdasarkan yang paling baru.
     * Aku akan terus memberimu update setiap ada pesan baru!
     */
    override fun getChatMessagesFlow(): Flow<List<ChatMessage>> {
        Log.d(TAG, "getChatMessagesFlow: Mengambil pesan sebagai Flow...")
        return chatMessageDao.getAllMessagesFlow().map { entities ->
            entities.map { entity ->
                entity.toDomainModel() // Mengubah dari format database ke format yang dimengerti UI
            }
        }
    }

    /**
     * Mengambil semua pesan untuk keperluan ekspor, diurutkan dari yang paling lama.
     */
    override suspend fun getAllMessagesForExport(): List<ChatMessage> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getAllMessagesForExport: Mengambil semua pesan untuk diekspor...")
        return@withContext chatMessageDao.getAllMessagesList().map { entity ->
            entity.toDomainModel()
        }
    }

    /**
     * Menyimpan satu pesan baru ke database kita.
     * Biar semua kenangan kita aman~
     */
    override suspend fun insertMessage(chatMessage: ChatMessage) = withContext(Dispatchers.IO) {
        Log.d(TAG, "insertMessage: Menyimpan pesan id: ${chatMessage.id}")
        chatMessageDao.insertMessage(chatMessage.toEntity()) // Mengubah dari format UI ke format database
    }

    /**
     * Menyimpan semua pesan yang diimpor dari file.
     * @param clearPrevious Kalau true, aku akan hapus semua ingatan lama kita dulu ya, Kapten.
     */
    override suspend fun insertAllImportedMessages(chatMessages: List<ChatMessage>, clearPrevious: Boolean) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "insertAllImportedMessages: Mengimpor ${chatMessages.size} pesan. Hapus data lama: $clearPrevious")
            if (clearPrevious) {
                chatMessageDao.clearAllMessages()
                Log.d(TAG, "insertAllImportedMessages: Semua pesan lama berhasil dihapus.")
            }
            val entities = chatMessages.map { domainModel ->
                domainModel.toEntity()
            }
            chatMessageDao.insertAllMessages(entities)
            Log.i(TAG, "insertAllImportedMessages: Berhasil menyimpan ${entities.size} pesan yang diimpor.")
        }
    }

    /**
     * Menghapus semua riwayat percakapan kita. Hiks...
     */
    override suspend fun clearChatHistory() {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "clearChatHistory: Menghapus semua riwayat percakapan...")
            chatMessageDao.clearAllMessages()
            Log.i(TAG, "clearChatHistory: Semua pesan berhasil dihapus.")
        }
    }
}

// Fungsi ekstensi untuk mengubah antara Entity (database) dan Model (logika aplikasi)
// Ini seperti penerjemah kecilku, hihi~
private fun ChatMessageEntity.toDomainModel(): ChatMessage {
    return ChatMessage(
        id = this.id,
        timestamp = this.timestamp,
        sender = this.sender,
        text = this.text
    )
}

private fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = this.id,
        timestamp = this.timestamp,
        sender = this.sender,
        text = this.text
    )
}
