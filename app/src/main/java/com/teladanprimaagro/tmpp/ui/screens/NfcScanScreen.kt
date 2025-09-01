package com.teladanprimaagro.tmpp.ui.screens

import android.content.Intent
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
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.SuccessGreen
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.util.NfcReadDialog
import com.teladanprimaagro.tmpp.viewmodels.ScannedItem
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScanScreen(
    navController: NavController,
    sharedNfcViewModel: SharedNfcViewModel,
    nfcIntentFromActivity: State<Intent?>
) {
    var showNfcDialog by remember { mutableStateOf(false) }
    var scannedData by remember { mutableStateOf<ScannedItem?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pindai NFC",
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tombol untuk memicu dialog pembacaan NFC
            Button(
                onClick = { showNfcDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen
                )
            ) {
                Text("Mulai Pindai NFC", color = Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            scannedData?.let { data ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MainColor.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Hasil Pemindaian:", fontWeight = FontWeight.Bold, color = White, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No. Unik: ${data.uniqueNo}", color = White, fontSize = 16.sp)
                        Text("Tanggal: ${data.tanggal}", color = White, fontSize = 16.sp)
                        Text("Blok: ${data.blok}", color = White, fontSize = 16.sp)
                        Text("Total Buah: ${data.totalBuah}", color = White, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // Menggunakan NfcReadDialog
    NfcReadDialog(
        showDialog = showNfcDialog,
        onDismissRequest = {
            showNfcDialog = false
            scannedData = null // Bersihkan data saat dialog ditutup
        },
        onReadComplete = { item ->
            scannedData = item
            showNfcDialog = false
        },
        nfcIntentFromActivity = nfcIntentFromActivity,
        sharedNfcViewModel = sharedNfcViewModel
    )
}