package com.example.elysia_assistant.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Definisikan Light Color Scheme dengan warna HoHE Anda
private val LightColorScheme = lightColorScheme(
    primary = HoHEPinkPrimary,
    secondary = HoHELightBlue,
    tertiary = HoHEGold,
    background = HoHEOffWhite,
    surface = HoHEWhite,
    onPrimary = HoHEWhite,
    onSecondary = HoHEDarkText,
    onTertiary = HoHEDarkText,
    onBackground = HoHEDarkText,
    onSurface = HoHEDarkText,
    // Anda bisa override warna lain jika perlu
    // primaryContainer = HoHEPinkLight,
    // onPrimaryContainer = HoHEDarkText,
)

// Definisikan Dark Color Scheme jika Anda mau (opsional)
/*
private val DarkColorScheme = darkColorScheme(
    primary = HoHEPinkDarkPrimary, // Warna pink yang disesuaikan untuk dark mode
    secondary = HoHELightBlue,
    tertiary = HoHEGold,
    background = HoHEDarkSurface, // Latar belakang gelap
    surface = Color(0xFF4A3F45),    // Surface yang sedikit lebih terang dari background
    onPrimary = HoHEDarkText,       // Teks di atas primary (mungkin butuh penyesuaian)
    onSecondary = HoHEWhite,
    onTertiary = HoHEWhite,
    onBackground = HoHEOffWhite,
    onSurface = HoHEOffWhite
)
*/

@Composable
fun ElysiaAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Untuk saat ini, kita bisa fokus ke light theme dulu
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Ganti dengan 'if (darkTheme) DarkColorScheme else LightColorScheme' jika mendukung dark theme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Atau warna lain yang sesuai
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme // Sesuaikan ini
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Typography yang sudah kita definisikan dengan Nunito
        shapes = Shapes(), // Anda bisa mendefinisikan Shapes.kt jika ingin kustomisasi bentuk komponen
        content = content
    )
}