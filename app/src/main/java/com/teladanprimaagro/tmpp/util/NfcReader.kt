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

@Composable
fun NfcReadDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onReadComplete: (ScannedItem) -> Unit,
    sharedNfcViewModel: SharedNfcViewModel
) {
    if (!showDialog) return

    val context = LocalContext.current
    val view = LocalView.current
    remember(view) { view.findViewTreeLifecycleOwner() }

    val nfcState by sharedNfcViewModel.nfcState.collectAsState()
    var nfcReadStatusMessage by remember { mutableStateOf("Dekatkan tag NFC ke perangkat Anda untuk memindai.") }

    LaunchedEffect(nfcState) {
        nfcReadStatusMessage = when (nfcState) {
            is NfcOperationState.WaitingForRead -> (nfcState as NfcOperationState.WaitingForRead).message
            is NfcOperationState.Reading -> (nfcState as NfcOperationState.Reading).message
            is NfcOperationState.ReadSuccess -> (nfcState as NfcOperationState.ReadSuccess).message
            is NfcOperationState.ReadError -> (nfcState as NfcOperationState.ReadError).message
            else -> "Dekatkan tag NFC ke perangkat Anda untuk memindai."
        }
    }

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

    LaunchedEffect(true) {
        if (context !is Activity) {
            Log.e("NFCReader", "Context is not an Activity. Cannot enable/disable foreground dispatch.")
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
            Log.d("NFCReader", "Foreground dispatch ENABLED by LaunchedEffect (showDialog: true).")
            sharedNfcViewModel.setWaitingForRead()
        } else {
            if (nfcAdapter != null) {
                nfcAdapter.disableForegroundDispatch(context)
                Log.d("NFCReader", "Foreground dispatch DISABLED by LaunchedEffect (showDialog: false).")
                sharedNfcViewModel.resetNfcState()
            }
        }
    }

    LaunchedEffect(sharedNfcViewModel.nfcIntent.collectAsState().value) {
        val currentIntent = sharedNfcViewModel.nfcIntent.value
        if (currentIntent != null && showDialog) {
            Log.d("NFCReader", "Processing NFC Intent from SharedNfcViewModel: ${currentIntent.action}")
            sharedNfcViewModel.readNfcData(currentIntent) { scannedData, errorMessage ->
                if (scannedData != null) {
                    onReadComplete(scannedData)
                }
                sharedNfcViewModel.resetNfcIntent()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Memindai Tag NFC!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = InfoBlue
                )
            }
        },
        text = {
            Text(
                text = nfcReadStatusMessage,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            )
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
                    Text("Batal", color = Color.Black)
                }
            }
        },
        containerColor = BackgroundDarkGrey,
        shape = RoundedCornerShape(16.dp)
    )
}