package com.example.elysia_assistant.domain.model // Sesuaikan dengan package Anda

import kotlinx.serialization.Serializable

@Serializable // Anotasi untuk Kotlinx Serialization
data class ChatMessage(
    val id: String, // ID unik untuk setiap pesan
    val timestamp: Long, // Waktu pesan dikirim/diterima (Unix timestamp)
    val sender: String, // "USER" atau "ELYSIA"
    val text: String,
    // Anda bisa menambahkan field lain jika perlu, misalnya status terkirim, terbaaca, dll.
)

// Wrapper class jika Anda ingin mengekspor/mengimpor list dari ChatMessage
@Serializable
data class ChatHistory(
    val messages: List<ChatMessage>
)
