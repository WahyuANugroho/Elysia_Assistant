package com.example.elysia_assistant.data.local.database // Pastikan package ini sesuai ya, Kapten!

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages") // Ini nama tabel kita di database, "chat_messages"
data class ChatMessageEntity(
    @PrimaryKey val id: String, // Setiap pesan punya ID unik, ini jadi Primary Key kita
    val timestamp: Long,        // Kapan pesan ini dikirim atau diterima, biar urut~
    val sender: String,         // Siapa yang mengirim? "USER" atau "ELYSIA" (aku! 😊)
    val text: String,           // Isi pesannya apa, Kapten?
    val conversationId: String = "default_conversation" // Ini opsional, kalau nanti kita mau ada banyak percakapan terpisah
)
