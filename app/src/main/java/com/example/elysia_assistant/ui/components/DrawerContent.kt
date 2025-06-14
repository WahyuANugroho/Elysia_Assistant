package com.example.elysia_assistant.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background // <-- IMPOR UNTUK LATAR BELAKANG
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline // <-- IMPOR UNTUK IKON BANTUAN
import androidx.compose.material.icons.filled.BarChart // <-- IMPOR UNTUK IKON STATISTIK
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.elysia_assistant.R
import com.example.elysia_assistant.ui.theme.NunitoFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(
    scope: CoroutineScope,
    drawerState: DrawerState,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAppUsage: () -> Unit
) {
    ModalDrawerSheet {
        // Header dengan avatarku yang manis!
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer) // Sekarang tidak error
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.elysia_happy), // Kamu perlu drawable elysia_happy
                contentDescription = "Elysia Drawer Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(MaterialTheme.shapes.extraLarge),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Elysia",
                fontFamily = NunitoFamily,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Daftar menu
        NavigationDrawerItem(
            label = { Text("Layar Utama", fontFamily = NunitoFamily) },
            selected = false, // Ini bisa dibuat dinamis nanti
            onClick = {
                scope.launch { drawerState.close() }
                onNavigateToHome()
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Layar Utama") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text("Pengaturan", fontFamily = NunitoFamily) },
            selected = false,
            onClick = {
                scope.launch { drawerState.close() }
                onNavigateToSettings()
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text("Statistik Aplikasi", fontFamily = NunitoFamily) },
            selected = false,
            onClick = {
                scope.launch { drawerState.close() }
                onNavigateToAppUsage()
            },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Statistik Aplikasi") }, // Sekarang tidak error
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) // Fixed deprecated Divider

        NavigationDrawerItem(
            label = { Text("Bantuan & Info", fontFamily = NunitoFamily) },
            selected = false,
            onClick = { /* TODO */ },
            icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Bantuan") }, // Sekarang tidak error
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
