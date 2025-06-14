// Pastikan package declaration ini adalah baris paling pertama dan sesuai dengan struktur folder Anda
// dan juga sama dengan 'namespace' di build.gradle.kts dan 'package' di AndroidManifest.xml
package com.example.elysia_assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme // Pastikan ini dari material3
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat // Impor untuk setDecorFitsSystemWindows
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

        // 1. Aktifkan edge-to-edge display.
        // Ini memungkinkan konten digambar di seluruh layar, termasuk di area system bars.
        WindowCompat.setDecorFitsSystemWindows(window, false) // Disarankan 'false' untuk kontrol penuh insets & immersive

        // 2. Siapkan controller untuk WindowInsets dan atur mode imersif.
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        // Sembunyikan status bar dan navigation bar.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        // Atur agar system bar muncul kembali sementara saat pengguna melakukan swipe dari tepi layar.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

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
    const val SETTINGS_SCREEN = "settings" // <-- RUTE BARU
}

@Composable
fun AppNavigation(
    // Jika SettingsScreen butuh Application context untuk ViewModel-nya,
    // Anda mungkin perlu meneruskannya atau memastikan ViewModelFactory-nya bisa mengaksesnya.
    // Untuk sekarang, kita buat sederhana dulu.
    // application: Application = LocalContext.current.applicationContext as Application
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NavDestinations.SPLASH_SCREEN
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
            // Meneruskan navController ke MainChatScreen agar bisa navigasi ke Settings
            MainChatScreen(
                // application = application, // Jika ViewModel di MainChatScreen butuh
                onNavigateToSettings = {
                    navController.navigate(NavDestinations.SETTINGS_SCREEN)
                }
            )
        }
        composable(NavDestinations.SETTINGS_SCREEN) {
            SettingsScreen(
                navController = navController
                // application = application // Aktifkan jika SettingsViewModel.Factory Anda membutuhkannya
            )
        }

    }
}
