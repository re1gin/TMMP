package com.teladanprimaagro.tmpp.ui.screens.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.SuccessGreen
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel

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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Format Nomor Unik",
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
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
                .background(color = MainBackground,)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Masukkan format baru untuk nomor unik panen.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = newFormat,
                    onValueChange = { newFormat = it.uppercase() },
                    label = { Text("Format Baru (misal: AME2)", color = Color.White) },
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

                Button(
                    onClick = {
                        settingsViewModel.setUniqueNoFormat(newFormat.trim())
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = newFormat.isNotBlank() && newFormat.trim() != currentFormat,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.White
                    )
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}