// Pastikan package declaration ini adalah baris paling pertama dan sesuai dengan struktur folder Anda
// dan juga sama dengan 'namespace' di build.gradle.kts dan 'package' di AndroidManifest.xml
package com.example.elysia_assistant

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.elysia_assistant.ui.screen.MainChatScreen
import com.example.elysia_assistant.ui.screen.SettingsScreen
import com.example.elysia_assistant.ui.screen.SplashScreen
import com.example.elysia_assistant.ui.theme.ElysiaAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ElysiaAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// Objek untuk menyimpan rute navigasi sebagai konstanta agar konsisten dan menghindari typo.
object NavDestinations {
    const val SPLASH_SCREEN = "splash"
    const val MAIN_CHAT_SCREEN = "main_chat"
    const val SETTINGS_SCREEN = "settings"
    const val APP_USAGE_SCREEN = "app_usage"
}

@OptIn(ExperimentalMaterial3Api::class) // Anotasi untuk Composable yang masih eksperimental
@Composable // <-- ANOTASI PENTING YANG HILANG!
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NavDestinations.SPLASH_SCREEN // Kembali ke Splash Screen sebagai awal
    ) {
        composable(NavDestinations.SPLASH_SCREEN) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(NavDestinations.MAIN_CHAT_SCREEN) {
                        popUpTo(NavDestinations.SPLASH_SCREEN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(NavDestinations.MAIN_CHAT_SCREEN) {
            // Sekarang kita berikan NavController ke MainChatScreen
            MainChatScreen(
                navController = navController
            )
        }
        composable(NavDestinations.SETTINGS_SCREEN) {
            SettingsScreen(navController = navController)
        }
        composable(NavDestinations.APP_USAGE_SCREEN) {
            // Placeholder untuk layar statistik
            Scaffold(
                topBar = { TopAppBar(title = { Text("Statistik Penggunaan") }) }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Layar Statistik Aplikasi akan ada di sini, Kapten! 💕")
                }
            }
        }
    }
}
