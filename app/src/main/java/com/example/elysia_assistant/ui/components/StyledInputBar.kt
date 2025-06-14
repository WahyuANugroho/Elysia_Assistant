package com.example.elysia_assistant.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite // Contoh ikon
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StyledInputBar(
    modifier: Modifier = Modifier,
    onSendMessage: (String) -> Unit = {},
    onVoiceInputClicked: () -> Unit = {}
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVoiceInputClicked) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Voice Input",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Ketik pesan ke Elysia...", style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors( // Sesuaikan warna TextField
                // focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                // unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                // focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        IconButton(onClick = {
            if (text.isNotBlank()) {
                onSendMessage(text)
                text = "" // Kosongkan setelah dikirim
            }
        }) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send Message",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
