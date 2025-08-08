package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbahFormatUniqueNoScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    // Mengambil nilai format saat ini dari ViewModel
    val currentFormat = settingsViewModel.getUniqueNoFormat()
    var newFormat by remember { mutableStateOf(currentFormat) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ubah Format Nomor Unik") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Masukkan format baru untuk nomor unik panen.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = newFormat,
                onValueChange = { newFormat = it },
                label = { Text("Format Baru (misal: AME2)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    settingsViewModel.setUniqueNoFormat(newFormat)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = newFormat.isNotBlank() && newFormat != currentFormat
            ) {
                Text("Simpan")
            }
        }
    }
}