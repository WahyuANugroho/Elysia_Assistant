package com.example.elysia_assistant.ui.screen // Pastikan ini sesuai dengan package Anda

import android.Manifest
import android.app.Application
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.navigation.NavController
import com.example.elysia_assistant.NavDestinations
import com.example.elysia_assistant.R
import com.example.elysia_assistant.ui.components.ChatBubble
import com.example.elysia_assistant.ui.components.DrawerContent
import com.example.elysia_assistant.ui.components.ElysiaAvatarView
import com.example.elysia_assistant.ui.components.StyledInputBar
import com.example.elysia_assistant.ui.theme.NunitoFamily
import com.example.elysia_assistant.viewmodel.MainChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainChatScreen(
    navController: NavController,
    application: Application = LocalContext.current.applicationContext as Application,
    viewModel: MainChatViewModel = viewModel(factory = MainChatViewModel.Factory(application))
) {
    // State untuk mengontrol laci navigasi (drawer)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Mengamati semua state dari ViewModel
    val chatMessages by viewModel.chatMessages.collectAsState()
    val shouldRequestPermission by viewModel.shouldRequestLocationPermission.collectAsState()
    val locationName by viewModel.locationName.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val weatherCondition by viewModel.weatherCondition.collectAsState()
    val weatherIconRes by viewModel.weatherIconResId.collectAsState()
    val isLoadingWeather by viewModel.isLoadingWeather.collectAsState()
    val weatherError by viewModel.weatherError.collectAsState()

    val lazyListState = rememberLazyListState()

    // Launcher untuk meminta izin lokasi
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        viewModel.onLocationPermissionResult(isGranted)
    }

    // Memicu permintaan izin jika diperlukan oleh ViewModel
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
            scope.launch {
                lazyListState.animateScrollToItem(0)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                scope = scope,
                drawerState = drawerState,
                onNavigateToHome = { scope.launch { drawerState.close() } },
                onNavigateToSettings = { navController.navigate(NavDestinations.SETTINGS_SCREEN) },
                onNavigateToAppUsage = { navController.navigate(NavDestinations.APP_USAGE_SCREEN) }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Elysia",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = NunitoFamily,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Buka Menu")
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
                                    modifier = Modifier.size(48.dp).padding(end = 8.dp)
                                )
                            }
                            when {
                                isLoadingWeather -> Text("Memuat...", style = MaterialTheme.typography.bodySmall, fontFamily = NunitoFamily)
                                weatherError != null -> Column(horizontalAlignment = Alignment.End) {
                                    Text("Error", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error), fontFamily = NunitoFamily)
                                    Text(weatherError ?: "", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error), maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = NunitoFamily)
                                }
                                !locationName.isNullOrEmpty() && locationName != "Tunggu..." -> Column(horizontalAlignment = Alignment.End) {
                                    Text(text = locationName ?: "Lokasi", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = NunitoFamily, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = temperature ?: "--°C", style = MaterialTheme.typography.bodySmall, fontFamily = NunitoFamily)
                                    Text(text = weatherCondition ?: "...", style = MaterialTheme.typography.labelMedium, fontStyle = FontStyle.Italic, fontFamily = NunitoFamily)
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
            },
            bottomBar = {
                StyledInputBar(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(bottom = 8.dp),
                    onSendMessage = viewModel::sendMessage
                )
            }
        ) { innerPaddingScaffold ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPaddingScaffold).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ElysiaAvatarView(expressionResId = R.drawable.elysia_playful, modifier = Modifier.padding(vertical = 16.dp))
                if (chatMessages.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("Ketik sesuatu untuk memulai percakapan kita~ 💕", style = MaterialTheme.typography.bodyLarge, fontFamily = NunitoFamily)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        state = lazyListState,
                        reverseLayout = true,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(chatMessages, key = { it.id }) { message ->
                            ChatBubble(message = message)
                        }
                    }
                }
            }
        }
    }
}
