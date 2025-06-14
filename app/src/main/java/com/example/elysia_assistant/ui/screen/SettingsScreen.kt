package com.example.elysia_assistant.ui.screen // Sesuaikan dengan package Anda

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.elysia_assistant.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    application: Application = LocalContext.current.applicationContext as Application,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(application))
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Mengamati status dari ViewModel
    val exportStatus by viewModel.exportStatus.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()

    // Menampilkan Snackbar untuk status
    LaunchedEffect(exportStatus) {
        exportStatus?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
                viewModel.clearExportStatus() // Reset status setelah ditampilkan
            }
        }
    }
    LaunchedEffect(importStatus) {
        importStatus?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
                viewModel.clearImportStatus() // Reset status setelah ditampilkan
            }
        }
    }

    // Launcher untuk memilih lokasi penyimpanan file ekspor
    val exportChatLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json") // Tipe MIME untuk JSON
    ) { uri: Uri? ->
        Log.d("SettingsScreen", "Export URI selected: $uri")
        viewModel.onExportChatHistory(uri) // Panggil fungsi di ViewModel
    }

    // Launcher untuk export Excel
    val exportChatToExcelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        viewModel.onExportChatHistory(uri, "excel")
    }

    // Launcher untuk memilih file impor
    val importChatLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent() // Atau OpenDocument jika ingin kontrol lebih
    ) { uri: Uri? ->
        Log.d("SettingsScreen", "Import URI selected: $uri")
        viewModel.onImportChatHistory(uri) // Panggil fungsi di ViewModel
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pengaturan", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start // Ubah ke Start untuk tampilan daftar
        ) {
            Text(
                "Ingatan Percakapan",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = {
                    Log.d("SettingsScreen", "Tombol Export Ingatan (JSON) diklik")
                    exportChatLauncher.launch("ElysiaIngatanBackup.json") // Launcher JSON yang sudah ada
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Ingatan ke File (JSON)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    Log.d("SettingsScreen", "Tombol Export Ingatan (Excel) diklik")
                    exportChatToExcelLauncher.launch("ElysiaIngatanBackup.xlsx") // Launcher baru untuk Excel
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Ingatan ke File (Excel)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d("SettingsScreen", "Tombol Import Ingatan diklik")
                    // Filter untuk hanya menampilkan file JSON saat memilih
                    importChatLauncher.launch("application/json")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Ingatan dari File")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Notifikasi",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // TODO: Tambahkan item-item pengaturan notifikasi di sini (dengan Switch)
            // SettingItem(title = "Notifikasi Imut", onClick = { /* ... */ }) { Switch(...) }

            Text(
                "Fitur pengaturan lainnya akan ada di sini.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
