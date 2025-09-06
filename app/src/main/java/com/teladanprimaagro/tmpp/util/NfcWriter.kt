@file:Suppress("DEPRECATION")

package com.teladanprimaagro.tmpp.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.theme.BackgroundDarkGrey
import com.teladanprimaagro.tmpp.ui.theme.InfoBlue
import com.teladanprimaagro.tmpp.viewmodels.NfcOperationState
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel

@SuppressLint("NewApi")
@Composable
fun NfcWriteDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    dataToWrite: PanenData?,
    onWriteComplete: (Boolean, String) -> Unit,
    sharedNfcViewModel: SharedNfcViewModel
) {
    if (!showDialog) return

    val context = LocalContext.current
    val view = LocalView.current
    remember(view) { view.findViewTreeLifecycleOwner() }

    val nfcState by sharedNfcViewModel.nfcState.collectAsState()
    var nfcWriteStatusMessage by remember { mutableStateOf("Tempelkan stiker NFC ke perangkat Anda untuk menulis data.") }

    // Update status message based on nfcState
    LaunchedEffect(nfcState) {
        nfcWriteStatusMessage = when (nfcState) {
            is NfcOperationState.WaitingForWrite -> (nfcState as NfcOperationState.WaitingForWrite).message
            is NfcOperationState.Writing -> (nfcState as NfcOperationState.Writing).message
            is NfcOperationState.WriteSuccess -> (nfcState as NfcOperationState.WriteSuccess).message
            is NfcOperationState.WriteError -> (nfcState as NfcOperationState.WriteError).message
            else -> "Tempelkan stiker NFC ke perangkat Anda untuk menulis data."
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
        IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addDataScheme("ndef")
            addDataAuthority("text", null)
        },
        IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
    )
    val techLists = arrayOf(arrayOf(android.nfc.tech.Ndef::class.java.name))

    LaunchedEffect(true) {
        if (context !is Activity) {
            Log.e("NFCWriter", "Context is not an Activity. Cannot enable/disable foreground dispatch.")
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
                sharedNfcViewModel.setWriteError(msg)
                onWriteComplete(false, msg)
                Log.w("NFCWriter", "Failed to enable foreground dispatch: $msg")
                return@LaunchedEffect
            }

            nfcAdapter.enableForegroundDispatch(context, pendingIntent, intentFilters, techLists)
            Log.d("NFCWriter", "Foreground dispatch ENABLED by LaunchedEffect (showDialog: true).")
            if (dataToWrite != null) {
                sharedNfcViewModel.setWaitingForWrite(dataToWrite)
            } else {
                sharedNfcViewModel.setWriteError("Data untuk ditulis tidak tersedia.")
                onWriteComplete(false, "Data untuk ditulis tidak tersedia.")
            }
        } else {
            if (nfcAdapter != null) {
                nfcAdapter.disableForegroundDispatch(context)
                Log.d("NFCWriter", "Foreground dispatch DISABLED by LaunchedEffect (showDialog: false).")
                sharedNfcViewModel.resetNfcState()
            }
        }
    }

    LaunchedEffect(sharedNfcViewModel.nfcIntent.collectAsState().value) {
        val currentIntent = sharedNfcViewModel.nfcIntent.value
        if (currentIntent != null && showDialog && dataToWrite != null) {
            Log.d("NFCWriter", "Processing NFC Intent from SharedNfcViewModel: ${currentIntent.action}")

            val tag: Tag? = currentIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                sharedNfcViewModel.writeNfcData(tag, dataToWrite) { success, message ->
                    onWriteComplete(success, message)
                    sharedNfcViewModel.resetNfcIntent()
                }
            } else {
                sharedNfcViewModel.setWriteError("Stiker NFC tidak terdeteksi dengan benar.")
                onWriteComplete(false, "Stiker NFC tidak terdeteksi dengan benar.")
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
                    text = "Menginput Tag NFC!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = InfoBlue
                )
            }
        },
        text = {
            Text(
                text = nfcWriteStatusMessage,
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