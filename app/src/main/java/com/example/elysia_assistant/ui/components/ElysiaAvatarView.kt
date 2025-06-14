// Di ui/components/ElysiaAvatarView.kt
package com.example.elysia_assistant.ui.components // Sesuaikan package

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape // Jika Anda ingin tetap lingkaran
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // Jika Anda ingin tetap lingkaran
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
// import com.mity.elysia_assistant.R // Tidak perlu jika @DrawableRes digunakan dengan benar

@Composable
fun ElysiaAvatarView(@DrawableRes expressionResId: Int, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = expressionResId),
        contentDescription = "Elysia Avatar",
        modifier = modifier
            .size(120.dp) // Anda bisa sesuaikan ukurannya jika mau
            .clip(CircleShape), // Hapus .clip(CircleShape) jika tidak ingin bentuk lingkaran
        contentScale = ContentScale.Fit // <--- UBAH KE SINI (dari Crop atau FillBounds)
    )
}