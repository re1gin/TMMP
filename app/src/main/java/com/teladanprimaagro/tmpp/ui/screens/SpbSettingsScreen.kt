package com.teladanprimaagro.tmpp.ui.screens

// SpbSettingsScreen.kt

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpbSettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val currentSpbFormat = settingsViewModel.getSpbFormat()
    var newSpbFormat by remember { mutableStateOf(currentSpbFormat) }

    val currentAfdCode = settingsViewModel.getAfdCode()
    var newAfdCode by remember { mutableStateOf(currentAfdCode) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Format SPB") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Pengaturan Format SPB
            Text("Format Nomor SPB", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = newSpbFormat,
                onValueChange = { newSpbFormat = it },
                label = { Text("Contoh: E005/ESPB atau AME/TPA") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Pengaturan Kode Afdeling
            Text("Kode Afdeling", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = newAfdCode,
                onValueChange = { newAfdCode = it },
                label = { Text("Contoh: AFD1 atau AFD8") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    settingsViewModel.setSpbFormat(newSpbFormat)
                    settingsViewModel.setAfdCode(newAfdCode)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Pengaturan")
            }
        }
    }
}