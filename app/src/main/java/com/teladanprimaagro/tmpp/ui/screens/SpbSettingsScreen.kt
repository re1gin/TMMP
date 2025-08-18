package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.DropdownInputField
import com.teladanprimaagro.tmpp.ui.theme.BackgroundDarkGrey
import com.teladanprimaagro.tmpp.ui.theme.SuccessGreen
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel

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

    // Ambil opsi mandor loading dari ViewModel
    val mandorLoadingOptions = settingsViewModel.mandorLoadingOptions
    val selectedMandorLoadingState by settingsViewModel.selectedMandorLoading.collectAsState()
    var selectedMandorLoading by remember { mutableStateOf(selectedMandorLoadingState) }

    var mandorLoadingExpanded by remember { mutableStateOf(false) }

    // Gunakan LaunchedEffect untuk memperbarui state lokal jika nilai dari ViewModel berubah
    LaunchedEffect(selectedMandorLoadingState) {
        selectedMandorLoading = selectedMandorLoadingState
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan Format SPB",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    color = BackgroundDarkGrey,
                )
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Pengaturan Format SPB
            Text(
                "Format Nomor SPB",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            OutlinedTextField(
                value = newSpbFormat,
                onValueChange = { newSpbFormat = it },
                label = { Text("Contoh: E005/ESPB atau AME/TPA", color = Color.White) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Pengaturan Kode Afdeling
            Text(
                "Kode Afdeling",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            OutlinedTextField(
                value = newAfdCode,
                onValueChange = { newAfdCode = it },
                label = { Text("Contoh: AFD1 atau AFD8", color = Color.White) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Pengaturan Pilihan Mandor Loading
            Text(
                "Pilihan Mandor Loading",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            DropdownInputField(
                label = "Mandor Loading",
                options = mandorLoadingOptions,
                selectedOption = selectedMandorLoading,
                onOptionSelected = {
                    selectedMandorLoading = it
                },
                expanded = mandorLoadingExpanded,
                onExpandedChange = { mandorLoadingExpanded = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    settingsViewModel.setSpbFormat(newSpbFormat)
                    settingsViewModel.setAfdCode(newAfdCode)
                    settingsViewModel.setMandorLoading(selectedMandorLoading)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = Color.Black
                )
            ) {
                Text("Simpan Pengaturan", fontWeight = FontWeight.Bold)
            }
        }
    }
}