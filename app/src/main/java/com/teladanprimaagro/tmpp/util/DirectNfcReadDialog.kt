package com.teladanprimaagro.tmpp.util

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.teladanprimaagro.tmpp.ui.theme.BackgroundDarkGrey
import com.teladanprimaagro.tmpp.ui.theme.InfoBlue
import com.teladanprimaagro.tmpp.viewmodels.NfcOperationState
import com.teladanprimaagro.tmpp.viewmodels.ScannedItem
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.White

@Composable
fun DirectNfcReadDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    sharedNfcViewModel: SharedNfcViewModel
) {
    // Keluar dari Composable jika dialog tidak seharusnya ditampilkan
    if (!showDialog) return

    val context = LocalContext.current
    val view = LocalView.current
    remember(view) { view.findViewTreeLifecycleOwner() }

    val nfcState by sharedNfcViewModel.nfcState.collectAsState()

    // State internal untuk UI
    var nfcReadStatusMessage by remember { mutableStateOf("Dekatkan tag NFC ke perangkat Anda untuk memindai.") }
    var scannedNfcData by remember { mutableStateOf<ScannedItem?>(null) }

    // Perbarui status pesan dan data berdasarkan nfcState
    LaunchedEffect(nfcState) {
        scannedNfcData = null
        nfcReadStatusMessage = when (val state = nfcState) {
            is NfcOperationState.WaitingForRead -> state.message
            is NfcOperationState.Reading -> state.message
            is NfcOperationState.ReadSuccess -> {
                scannedNfcData = state.scannedItem
                state.message
            }
            is NfcOperationState.ReadError -> state.message
            else -> "Dekatkan tag NFC ke perangkat Anda untuk memindai."
        }
    }

    // Kode Foreground Dispatch
    val nfcAdapter: NfcAdapter? = remember { NfcAdapter.getDefaultAdapter(context) }
    val pendingIntent: PendingIntent? = remember {
        if (nfcAdapter != null) {
            PendingIntent.getActivity(
                context, 0,
                Intent(context, context.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            )
        } else {
            null
        }
    }
    val intentFilters = arrayOf(
        IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
        IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
    )
    val techLists = arrayOf(arrayOf(android.nfc.tech.Ndef::class.java.name))

    LaunchedEffect(showDialog) {
        if (context !is Activity) {
            Log.e("DirectNfcReader", "Context is not an Activity. Cannot enable/disable foreground dispatch.")
            return@LaunchedEffect
        }
        if (showDialog) {
            if (nfcAdapter == null || !nfcAdapter.isEnabled || pendingIntent == null) {
                val msg = when {
                    nfcAdapter == null -> "NFC tidak tersedia di perangkat ini."
                    !nfcAdapter.isEnabled -> "NFC dinonaktifkan. Harap aktifkan di pengaturan."
                    pendingIntent == null -> "Kesalahan internal: PendingIntent tidak dapat dibuat."
                    else -> "Terjadi kesalahan yang tidak diketahui dengan NFC."
                }
                sharedNfcViewModel.setReadError(msg)
                return@LaunchedEffect
            }
            nfcAdapter.enableForegroundDispatch(context, pendingIntent, intentFilters, techLists)
            Log.d("DirectNfcReader", "Foreground dispatch ENABLED.")
            sharedNfcViewModel.setWaitingForRead()
        } else {
            if (nfcAdapter != null) {
                nfcAdapter.disableForegroundDispatch(context)
                Log.d("DirectNfcReader", "Foreground dispatch DISABLED.")
                sharedNfcViewModel.resetNfcState()
            }
        }
    }

    // Mengamati NFC Intent dari ViewModel
    LaunchedEffect(sharedNfcViewModel.nfcIntent.collectAsState().value) {
        val currentIntent = sharedNfcViewModel.nfcIntent.value
        if (currentIntent != null && showDialog) {
            Log.d("DirectNfcReader", "Processing NFC Intent: ${currentIntent.action}")
            // Langsung memanggil readNfcData, hasilnya akan diurus oleh nfcState
            sharedNfcViewModel.readNfcData(currentIntent) { _, _ ->
                sharedNfcViewModel.resetNfcIntent()
            }
        }
    }

    // Tampilan Dialog
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (scannedNfcData == null) "Memindai Tag NFC!" else "Data Tag NFC",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = InfoBlue
                )
            }
        },
        text = {
            if (scannedNfcData == null) {
                Text(
                    text = nfcReadStatusMessage,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                )
            } else {
                // Tampilan saat data berhasil dibaca
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scannedNfcData?.let { data ->
                        DataRow("No Unik", data.uniqueNo)
                        DataRow("Tanggal", data.tanggal)
                        DataRow("Blok", data.blok)
                        DataRow("Total Buah", data.totalBuah.toString())
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                ) {
                    Text(if (scannedNfcData == null) "Batal" else "Tutup", color = Black)
                }
            }
        },
        containerColor = BackgroundDarkGrey,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DataRow(title: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value ?: "N/A",
            color = White,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}