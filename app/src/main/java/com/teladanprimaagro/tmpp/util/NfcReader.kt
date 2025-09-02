package com.teladanprimaagro.tmpp.util

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import com.teladanprimaagro.tmpp.MainActivity
import com.teladanprimaagro.tmpp.ui.theme.BackgroundDarkGrey
import com.teladanprimaagro.tmpp.ui.theme.InfoBlue
import com.teladanprimaagro.tmpp.viewmodels.NfcOperationState
import com.teladanprimaagro.tmpp.viewmodels.ScannedItem
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel
import java.nio.charset.Charset

@Composable
fun NfcReadDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onReadComplete: (ScannedItem) -> Unit, // Callback untuk mengirim data yang dibaca
    nfcIntentFromActivity: State<Intent?>,
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
            else -> "Dekatkan tag NFC ke perangkat Anda untuk memindai." // Fallback
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
    val techLists = arrayOf(arrayOf(Ndef::class.java.name))

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
                sharedNfcViewModel.setReadError(msg) // Update SharedNfcViewModel
                Log.w("NFCReader", "Failed to enable foreground dispatch: $msg")
                return@LaunchedEffect
            }

            nfcAdapter.enableForegroundDispatch(context, pendingIntent, intentFilters, techLists)
            Log.d("NFCReader", "Foreground dispatch ENABLED by LaunchedEffect (showDialog: true).")
            // Set state ViewModel ke waiting for read
            sharedNfcViewModel.setWaitingForRead()
        } else {
            if (nfcAdapter != null) {
                nfcAdapter.disableForegroundDispatch(context)
                Log.d("NFCReader", "Foreground dispatch DISABLED by LaunchedEffect (showDialog: false).")
                sharedNfcViewModel.resetNfcState() // Reset state saat dialog ditutup
            }
        }
    }

    LaunchedEffect(nfcIntentFromActivity.value) {
        val currentIntent = nfcIntentFromActivity.value
        if (currentIntent != null && showDialog) { // Hanya proses jika dialog aktif
            Log.d("NFCReader", "Processing NFC Intent from Activity state: ${currentIntent.action}")

            val tag: Tag? = @Suppress("DEPRECATION") currentIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

            // BARIS INI YANG PERLU DIUBAH UNTUK MENGHINDARI ClassCastException
            val rawMessages: Array<out NdefMessage>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Untuk Android 13 (API 33) ke atas, gunakan metode yang lebih aman:
                currentIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
            } else {
                // Untuk versi Android di bawah 13, ambil sebagai Parcelable[] lalu konversi dengan aman:
                @Suppress("DEPRECATION")
                currentIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.mapNotNull { it as? NdefMessage }?.toTypedArray()
            }

            if (tag != null) {
                sharedNfcViewModel.setReading() // Update SharedNfcViewModel ke status "membaca"

                val ndef = Ndef.get(tag)
                var scannedData: ScannedItem? = null
                var errorMessage: String? = null

                if (ndef != null && rawMessages != null && rawMessages.isNotEmpty()) {
                    try {
                        val nfcMessage = rawMessages[0]
                        val ndefRecord = nfcMessage.records[0]

                        if (ndefRecord.tnf == NdefRecord.TNF_WELL_KNOWN &&
                            ndefRecord.type.contentEquals(NdefRecord.RTD_TEXT)
                        ) {

                            val payload = ndefRecord.payload
                            val textEncoding = if ((payload[0].toInt() and 0x80) == 0) Charset.forName("UTF-8") else Charset.forName("UTF-16")
                            val languageCodeLength = (payload[0].toInt() and 0x3F)
                            val text = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, textEncoding)

                            Log.d("NFCReader", "Raw NFC text: $text")

                            val parts = text.split(",")
                            if (parts.size == 4) {
                                val uniqueNo = parts[0].trim()
                                val tanggal = parts[1].trim()
                                val blok = parts[2].trim()
                                val totalBuah = parts[3].trim().toIntOrNull()

                                if (totalBuah != null) {
                                    scannedData = ScannedItem(uniqueNo, tanggal, blok, totalBuah)
                                } else {
                                    errorMessage = "Gagal memparsing total buah: ${parts[3]}"
                                }
                            } else {
                                errorMessage = "Format data NFC tidak valid. Ditemukan ${parts.size} bagian, diharapkan 4."
                            }
                        } else {
                            errorMessage = "Tipe record NFC tidak didukung (bukan TEXT)."
                        }
                    } catch (e: Exception) {
                        errorMessage = "Kesalahan saat memproses data NFC: ${e.message}"
                        Log.e("NFCReader", "Error processing NDEF message: ${e.message}", e)
                    }
                } else if (ndef != null) {
                    errorMessage = "Tidak ada pesan NDEF yang ditemukan pada tag."
                } else {
                    errorMessage = "Tag tidak mendukung NDEF atau tidak dapat dibaca."
                }

                if (scannedData != null) {
                    sharedNfcViewModel.setReadSuccess(scannedData)
                    onReadComplete(scannedData)
                } else {
                    sharedNfcViewModel.setReadError(errorMessage ?: "Gagal membaca tag.")
                }
            } else {
                sharedNfcViewModel.setReadError("Stiker NFC tidak terdeteksi dengan benar.")
            }
            if (context is MainActivity) {
                context._nfcIntent.value = null
            } else {
                Log.w("NFCReader", "Context is not MainActivity, cannot reset its NFC intent state directly.")
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