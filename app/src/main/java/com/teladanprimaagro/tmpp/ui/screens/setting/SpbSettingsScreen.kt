package com.teladanprimaagro.tmpp.ui.screens.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.DropdownInputField
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.SuccessGreen
import com.teladanprimaagro.tmpp.ui.theme.White
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Format SPB",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

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
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = MainColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MainColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    selectionColors = TextSelectionColors(
                        handleColor = MainColor,
                        backgroundColor = MainColor.copy(alpha = 0.4f)
                    )
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
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = MainColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MainColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    selectionColors = TextSelectionColors(
                        handleColor = MainColor,
                        backgroundColor = MainColor.copy(alpha = 0.4f)
                    )
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