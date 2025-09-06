package com.teladanprimaagro.tmpp.ui.screens

import android.nfc.NfcAdapter
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.DuplicateScanDialog
import com.teladanprimaagro.tmpp.ui.components.SuccessScanDialog
import com.teladanprimaagro.tmpp.ui.components.SummaryBox
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.util.NfcReadDialog
import com.teladanprimaagro.tmpp.viewmodels.NfcOperationState
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.viewmodels.ScanStatus
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanInputScreen(
    navController: NavController,
    pengirimanViewModel: PengirimanViewModel = viewModel(),
    sharedNfcViewModel: SharedNfcViewModel,
) {
    val context = LocalContext.current
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val nfcState by sharedNfcViewModel.nfcState.collectAsState()
    var showNfcReadDialog by remember { mutableStateOf(false) }

    val scannedItems by pengirimanViewModel.scannedItems.collectAsState()
    val totalBuahCalculated by pengirimanViewModel.totalBuahCalculated

    val scanStatus by pengirimanViewModel.scanStatus.collectAsState()

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
        when(scanStatus) {
            is ScanStatus.Success -> {
                showSuccessScanDialog = true
                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            is ScanStatus.Duplicate -> {
                showDuplicateDialog = true
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1))
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(16.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                SummaryBox(label = "Item Scan", value = scannedItems.size.toString())
                SummaryBox(label = "Total Buah", value = totalBuahCalculated.toString())
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
                val isNfcEnabled = nfcAdapter != null && nfcAdapter.isEnabled
                val iconColor = if (isNfcEnabled) Black else DangerRed
                val textColor = if (isNfcEnabled) MainColor else DangerRed
                val circleBackgroundColor = if (isNfcEnabled) MainColor else DangerRed.copy(0.4f)

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(circleBackgroundColor)
                        .clickable {
                            if (nfcAdapter == null) {
                                coroutineScope.launch { snackbarHostState.showSnackbar("NFC tidak tersedia di perangkat ini.") }
                            } else if (!isNfcEnabled) {
                                coroutineScope.launch { snackbarHostState.showSnackbar("NFC dinonaktifkan. Harap aktifkan di pengaturan.") }
                            } else {
                                showNfcReadDialog = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "NFC Icon",
                        modifier = Modifier.size(70.dp),
                        tint = iconColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                // Text below the circle
                Text(
                    text = "Tekan untuk Scan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Setiap scan akan menambahkan item ke daftar pengiriman. Anda bisa scan beberapa tag sekaligus.",
                style = MaterialTheme.typography.bodySmall,
                color = White.copy(alpha = 0.8f),
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
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainColor),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Selesai Scan dan Lanjutkan",
                    style = MaterialTheme.typography.titleMedium,
                    color = Black
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
                pengirimanViewModel.addScannedItem(scannedItem)
            },
            sharedNfcViewModel = sharedNfcViewModel
        )
    }

    if (showDuplicateDialog) {
        DuplicateScanDialog(
            onDismissRequest = {
                showDuplicateDialog = false
                pengirimanViewModel.resetScanStatus()
            },
            uniqueNo = (scanStatus as? ScanStatus.Duplicate)?.uniqueNo ?: ""
        )
    }

    if (showSuccessScanDialog) {
        SuccessScanDialog(
            onDismissRequest = {
                showSuccessScanDialog = false
                pengirimanViewModel.resetScanStatus()
            }
        )
    }
}