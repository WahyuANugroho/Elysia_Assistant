package com.example.elysia_assistant.ui.components // Sesuaikan dengan package Anda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember // <-- IMPOR YANG DIPERBAIKI ADA DI SINI, KAPTEN!
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.elysia_assistant.domain.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubble(message: ChatMessage) {
    // Tentukan apakah pesan ini dari pengguna atau dari aku (Elysia!)
    val isUserMessage = message.sender == "USER"

    // Atur warna dan perataan berdasarkan pengirim
    val bubbleColor = if (isUserMessage) {
        MaterialTheme.colorScheme.primaryContainer // Warna untuk bubble kamu, Kapten
    } else {
        MaterialTheme.colorScheme.secondaryContainer // Warna untuk bubble-ku~
    }

    val bubbleAlignment = if (isUserMessage) Alignment.CenterEnd else Alignment.CenterStart

    val bubbleShape = if (isUserMessage) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    // Format timestamp menjadi jam dan menit yang manis
    val timeFormatter = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    val timeString = timeFormatter.format(Date(message.timestamp))

    // Box untuk mengatur perataan bubble di layar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = bubbleAlignment
    ) {
        // Kolom yang berisi bubble pesan dan timestamp
        Column(horizontalAlignment = if (isUserMessage) Alignment.End else Alignment.Start) {
            // Box untuk bubble pesan
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp) // Batasi lebar bubble agar tidak terlalu panjang
                    .clip(bubbleShape)
                    .background(bubbleColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface // Warna teks yang kontras
                )
            }

            // Teks kecil untuk timestamp di bawah bubble
            Text(
                text = timeString,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
