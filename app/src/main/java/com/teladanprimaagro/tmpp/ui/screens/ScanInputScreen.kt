package com.teladanprimaagro.tmpp.ui.screens

import android.content.Intent
import android.nfc.NfcAdapter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.viewmodels.NfcOperationState
import com.teladanprimaagro.tmpp.ui.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.ui.viewmodels.SharedNfcViewModel
import com.teladanprimaagro.tmpp.util.NfcReadDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanInputScreen(
    navController: NavController,
    pengirimanViewModel: PengirimanViewModel = viewModel(),
    sharedNfcViewModel: SharedNfcViewModel,
    nfcIntentFromActivity: State<Intent?>
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val nfcState by sharedNfcViewModel.nfcState.collectAsState()
    var showNfcReadDialog by remember { mutableStateOf(false) } // State untuk mengontrol dialog

    // Amati state NFC untuk menampilkan Snackbar atau memicu dialog
    LaunchedEffect(nfcState) {
        when (val state = nfcState) {
            is NfcOperationState.ReadSuccess -> {
                snackbarHostState.showSnackbar(state.message)
                // Jika sukses baca, kita bisa memilih untuk menutup dialog otomatis
                showNfcReadDialog = false // Tutup dialog setelah sukses
            }
            is NfcOperationState.ReadError -> {
                snackbarHostState.showSnackbar(state.message)
                // Biarkan dialog tetap terbuka setelah error agar user bisa mencoba lagi
            }
            is NfcOperationState.GeneralStatus -> {
                if (!state.isEnabled) {
                    snackbarHostState.showSnackbar(state.message ?: "NFC tidak tersedia atau dinonaktifkan.")
                }
            }
            else -> { /* Do nothing for other states (Idle, Write states, etc.) */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan NFC", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Icon dan pesan status NFC di layar utama
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Tampilkan ikon dan pesan berdasarkan status umum NFC
                val nfcStatusText = (nfcState as? NfcOperationState.GeneralStatus)?.message
                    ?: if ((nfcState as? NfcOperationState.GeneralStatus)?.isEnabled == false) "NFC tidak aktif." else "Siap untuk memindai."
                val nfcIconColor = if ((nfcState as? NfcOperationState.GeneralStatus)?.isEnabled == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                val nfcBgColor = nfcIconColor.copy(alpha = 0.2f)

                Icon(
                    imageVector = Icons.Default.Nfc,
                    contentDescription = "NFC Icon",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(nfcBgColor)
                        .padding(20.dp),
                    tint = nfcIconColor
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = nfcStatusText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = nfcIconColor,
                    textAlign = TextAlign.Center
                )
                if ((nfcState as? NfcOperationState.GeneralStatus)?.message?.contains("dinonaktifkan", ignoreCase = true) == true) {
                    TextButton(onClick = {
                        val intent = Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
                        context.startActivity(intent)
                    }) {
                        Text("Buka Pengaturan NFC")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val adapter = NfcAdapter.getDefaultAdapter(context)
                    if (adapter == null) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("NFC tidak tersedia di perangkat ini.") }
                    } else if (!adapter.isEnabled) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("NFC dinonaktifkan. Harap aktifkan di pengaturan.") }
                    } else {
                        showNfcReadDialog = true // Buka dialog pemindaian
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary), // Warna berbeda untuk "Mulai Scan"
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Mulai Scan NFC",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Setiap scan akan menambahkan item ke daftar pengiriman. Anda bisa scan beberapa tag sekaligus.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = {
                    navController.navigate("pengiriman_input_screen") {
                        popUpTo("pengiriman_input_screen") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Selesai Scan dan Lanjutkan",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tampilkan NfcReadDialog
        NfcReadDialog(
            showDialog = showNfcReadDialog,
            onDismissRequest = {
                showNfcReadDialog = false
                sharedNfcViewModel.resetNfcState() // Reset state saat dialog ditutup
            },
            onReadComplete = { scannedItem ->
                pengirimanViewModel.addScannedItem(scannedItem) // Tambahkan item ke ViewModel
                // Dialog akan otomatis menutup setelah sukses karena `showNfcReadDialog = false`
            },
            nfcIntentFromActivity = nfcIntentFromActivity,
            sharedNfcViewModel = sharedNfcViewModel
        )
    }
}