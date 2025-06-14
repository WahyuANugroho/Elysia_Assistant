package com.example.elysia_assistant.ui.screen // Pastikan ini sesuai dengan package Anda

import android.Manifest
import android.app.Application
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elysia_assistant.R
import com.example.elysia_assistant.ui.components.ChatBubble
import com.example.elysia_assistant.ui.components.ElysiaAvatarView
import com.example.elysia_assistant.ui.components.StyledInputBar
import com.example.elysia_assistant.ui.theme.NunitoFamily
import com.example.elysia_assistant.viewmodel.MainChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainChatScreen(
    application: Application = LocalContext.current.applicationContext as Application,
    viewModel: MainChatViewModel = viewModel(factory = MainChatViewModel.Factory(application)),
    onNavigateToSettings: () -> Unit
) {
    // Mengamati state dari ViewModel
    val shouldRequestPermission by viewModel.shouldRequestLocationPermission.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Launcher untuk meminta izin lokasi
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onLocationPermissionResult(isGranted)
    }

    // Memicu permintaan izin saat Composable pertama kali masuk komposisi jika ViewModel menandakannya
    LaunchedEffect(shouldRequestPermission) {
        if (shouldRequestPermission) {
            Log.d("MainChatScreen", "ViewModel meminta UI untuk meluncurkan dialog izin.")
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Auto-scroll ke pesan terbaru
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(0) // Scroll ke item paling atas (karena reverseLayout=true)
            }
        }
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                viewModel = viewModel,
                onNavigateToSettings = onNavigateToSettings
            )
        },
        bottomBar = {
            StyledInputBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // Untuk jarak dari system navigation bar
//                    .imePadding()          // Agar naik di atas keyboard
                    .padding(bottom = 8.dp), // Jarak tambahan dari keyboard
                onSendMessage = viewModel::sendMessage
            )
        }
    ) { innerPaddingScaffold ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingScaffold) // Padding untuk TopAppBar dan BottomAppBar
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Elysia yang statis
            ElysiaAvatarView(
                expressionResId = R.drawable.elysia_playful,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Daftar Chat yang bisa di-scroll
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = lazyListState,
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (chatMessages.isEmpty()) {
                    item {
                        // Tampilkan pesan ini jika tidak ada riwayat chat
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Ketik sesuatu untuk memulai percakapan kita~ 💕",
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = NunitoFamily
                            )
                        }
                    }
                } else {
                    items(chatMessages, key = { it.id }) { message ->
                        ChatBubble(message = message)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopAppBar(viewModel: MainChatViewModel, onNavigateToSettings: () -> Unit) {
    val locationName by viewModel.locationName.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val weatherCondition by viewModel.weatherCondition.collectAsState()
    val weatherIconRes by viewModel.weatherIconResId.collectAsState()
    val isLoadingWeather by viewModel.isLoadingWeather.collectAsState()
    val weatherError by viewModel.weatherError.collectAsState()

    TopAppBar(
        title = {
            Text(
                "Elysia",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = NunitoFamily, // <-- Menegaskan penggunaan font Nunito
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Pengaturan")
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                weatherIconRes?.let { iconRes ->
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Ikon Cuaca",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 8.dp)
                    )
                }

                when {
                    isLoadingWeather -> {
                        Text(
                            "Memuat...",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = NunitoFamily // Menambahkan font family
                        )
                    }
                    weatherError != null -> {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Error",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error),
                                fontFamily = NunitoFamily
                            )
                            Text(
                                weatherError ?: "",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = NunitoFamily
                            )
                        }
                    }
                    !locationName.isNullOrEmpty() && locationName != "Tunggu..." -> {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = locationName ?: "Lokasi",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = NunitoFamily, // Menambahkan font family
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = temperature ?: "--°C",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = NunitoFamily // Menambahkan font family
                            )
                            Text(
                                text = weatherCondition ?: "...",
                                style = MaterialTheme.typography.labelMedium,
                                fontStyle = FontStyle.Italic,
                                fontFamily = NunitoFamily // Menambahkan font family
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
