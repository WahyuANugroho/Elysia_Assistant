package com.example.elysia_assistant.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.elysia_assistant.R // Pastikan R diimport
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000L) // Tunda selama 2 detik
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Ini akan menjaga gambar tetap di tengah
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_screen_elysia), // Ganti dengan nama aset splash Anda
            contentDescription = "Elysia Assistant Splash Screen",
            modifier = Modifier.size(300.dp), // <--- UBAH NILAI INI. Coba 150.dp, 120.dp, 100.dp, atau lainnya.
            contentScale = ContentScale.Fit // Menggunakan Fit biasanya bagus untuk logo agar tidak terpotong/distorsi
        )
    }
}