package com.example.elysia_assistant.data.local.database // Pastikan package ini sesuai ya, Kapten!

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<ChatMessageEntity>)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>> // Untuk observasi realtime, pesan terbaru di atas

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC") // ASC untuk export agar urutan kronologis
    suspend fun getAllMessagesList(): List<ChatMessageEntity> // Untuk export

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages() // Fungsi ini mengembalikan Unit secara implisit
}
