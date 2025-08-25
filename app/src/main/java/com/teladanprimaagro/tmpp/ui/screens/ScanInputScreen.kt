package com.teladanprimaagro.tmpp.ui.screens

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.SummaryBox
import com.teladanprimaagro.tmpp.viewmodels.NfcOperationState
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.viewmodels.ScanStatus
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel
import com.teladanprimaagro.tmpp.util.NfcReadDialog
import com.teladanprimaagro.tmpp.ui.components.SuccessScanDialog
import com.teladanprimaagro.tmpp.ui.components.DuplicateScanDialog
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanInputScreen(
    navController: NavController,
    pengirimanViewModel: PengirimanViewModel = viewModel(),
    sharedNfcViewModel: SharedNfcViewModel,
    nfcIntentFromActivity: State<Intent?>
) {
    val context = LocalContext.current
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val nfcState by sharedNfcViewModel.nfcState.collectAsState()
    var showNfcReadDialog by remember { mutableStateOf(false) }

    // Menggunakan scannedItems untuk SummaryBox, sesuai dengan alur yang sudah ada
    val scannedItems by pengirimanViewModel.scannedItems.collectAsState()
    val totalBuahCalculated by pengirimanViewModel.totalBuahCalculated

    val scanStatus by pengirimanViewModel.scanStatus.collectAsState()

    // NEW: States to control custom dialogs
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showSuccessScanDialog by remember { mutableStateOf(false) }

    LaunchedEffect(nfcState) {
        when (val state = nfcState) {
            is NfcOperationState.ReadSuccess -> {
                showNfcReadDialog = false
            }
            is NfcOperationState.ReadError -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            is NfcOperationState.GeneralStatus -> {
                if (!state.isEnabled) {
                    coroutineScope.launch { snackbarHostState.showSnackbar(state.message ?: "NFC tidak tersedia atau dinonaktifkan.") }
                }
            }
            else -> { /* Do nothing */ }
        }
    }

    LaunchedEffect(scanStatus) {
        when(val status = scanStatus) {
            is ScanStatus.Success -> {
                showSuccessScanDialog = true
                if (vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else if (vibrator != null) {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
            is ScanStatus.Duplicate -> {
                showDuplicateDialog = true
                if (vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1))
                } else if (vibrator != null) {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 200, 100, 200), -1)
                }
            }
            is ScanStatus.Idle -> {
                showSuccessScanDialog = false
                showDuplicateDialog = false
            }
            is ScanStatus.Finalized -> { /* Do nothing */ }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Scan N-Tag",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
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
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SummaryBox tetap menggunakan data dari alur utama (pengiriman)
                    SummaryBox(label = "Item Scan", value = scannedItems.size.toString())
                    SummaryBox(label = "Total Buah", value = totalBuahCalculated.toString())
                }
            }

            Spacer(modifier = Modifier.height(100.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val nfcStatusText = (nfcState as? NfcOperationState.GeneralStatus)?.message
                    ?: if ((nfcState as? NfcOperationState.GeneralStatus)?.isEnabled == false) "NFC tidak aktif." else "Siap untuk memindai."
                val nfcIconColor = if ((nfcState as? NfcOperationState.GeneralStatus)?.isEnabled == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary
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
                        showNfcReadDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
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

        NfcReadDialog(
            showDialog = showNfcReadDialog,
            onDismissRequest = {
                showNfcReadDialog = false
                sharedNfcViewModel.resetNfcState()
            },
            onReadComplete = { scannedItem ->
                // Panggil addScannedItem, ViewModel akan menangani validasi dan penyimpanan
                pengirimanViewModel.addScannedItem(scannedItem)
            },
            nfcIntentFromActivity = nfcIntentFromActivity,
            sharedNfcViewModel = sharedNfcViewModel
        )
    }

    if (showDuplicateDialog) {
        DuplicateScanDialog(
            onDismissRequest = {
                showDuplicateDialog = false
                pengirimanViewModel.resetScanStatus() // Reset status di ViewModel
            },
            uniqueNo = (scanStatus as? ScanStatus.Duplicate)?.uniqueNo ?: ""
        )
    }

    if (showSuccessScanDialog) {
        SuccessScanDialog(
            onDismissRequest = {
                showSuccessScanDialog = false
                pengirimanViewModel.resetScanStatus() // Reset status di ViewModel
            }
        )
    }
}