package com.teladanprimaagro.tmpp.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.util.DirectNfcReadDialog
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScannerScreen(
    navController: NavController,
    sharedNfcViewModel: SharedNfcViewModel = viewModel()
) {
    // State untuk mengontrol tampilan dialog. Selalu true karena ini halaman khusus pemindai.
    var showNfcScanDialog by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pemindai N-Tag",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(16.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Halaman khusus untuk memindai tag NFC. Dialog akan muncul secara otomatis.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
                color = White
            )
        }
    }

    // Tampilkan dialog NFC secara otomatis saat halaman ini terbuka
    DirectNfcReadDialog(
        showDialog = showNfcScanDialog,
        onDismissRequest = {
            showNfcScanDialog = false
            navController.popBackStack()
        },
        sharedNfcViewModel = sharedNfcViewModel
    )
}