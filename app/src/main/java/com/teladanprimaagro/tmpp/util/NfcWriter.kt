@file:Suppress("DEPRECATION")

package com.teladanprimaagro.tmpp.util

import android.annotation.SuppressLint
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.theme.BackgroundDarkGrey
import com.teladanprimaagro.tmpp.ui.theme.InfoBlue
import com.teladanprimaagro.tmpp.ui.theme.WarningYellow
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale // Import Locale untuk bahasa


fun writeNdefMessageToTag(
    tag: Tag,
    message: NdefMessage,
    onResult: (Boolean, String) -> Unit
) {
    val ndef = Ndef.get(tag)
    if (ndef != null) {
        try {
            ndef.connect()
            if (ndef.isWritable) {
                if (ndef.maxSize < message.toByteArray().size) {
                    onResult(false, "Ukuran data terlalu besar untuk stiker ini.")
                } else {
                    ndef.writeNdefMessage(message)
                    onResult(true, "Data berhasil ditulis ke stiker NFC!")
                }
            } else {
                onResult(false, "Stiker NFC tidak dapat ditulisi.")
            }
        } catch (e: Exception) {
            onResult(false, "Gagal menulis ke stiker NFC: ${e.message}")
        } finally {
            try {
                ndef.close()
            } catch (e: Exception) {
                Log.e("NFCWriter", "Error closing NDEF connection: ${e.message}", e)
            }
        }
    } else {
        onResult(false, "Stiker NFC tidak mendukung format NDEF.")
    }
}

@SuppressLint("NewApi")
@Composable
fun NfcWriteDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    dataToWrite: PanenData?, // Ini adalah PanenData lengkap dari ViewModel
    onWriteComplete: (Boolean, String) -> Unit,
    nfcIntentFromActivity: State<Intent?>
) {
    if (!showDialog) return

    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = remember(view) { view.findViewTreeLifecycleOwner() }

    var nfcWriteStatusMessage by remember { mutableStateOf("Tempelkan stiker NFC ke perangkat Anda untuk menulis data.") }

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
        // Kita sekarang akan mendeteksi NDEF_DISCOVERED dengan type RTD_TEXT
        IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addDataScheme("ndef")
            addDataAuthority("text", null) // host "text"
        },
        IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) // Ini untuk mendeteksi tag kosong atau tag yang tidak NDEF
    )
    val techLists = arrayOf(arrayOf(Ndef::class.java.name))

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
                nfcWriteStatusMessage = msg
                onWriteComplete(false, msg)
                Log.w("NFCWriter", "Failed to enable foreground dispatch: $msg")
                return@LaunchedEffect
            }

            nfcAdapter.enableForegroundDispatch(context, pendingIntent, intentFilters, techLists)
            Log.d("NFCWriter", "Foreground dispatch ENABLED by LaunchedEffect (showDialog: true).")
        } else {
            if (nfcAdapter != null) {
                nfcAdapter.disableForegroundDispatch(context)
                Log.d("NFCWriter", "Foreground dispatch DISABLED by LaunchedEffect (showDialog: false).")
            }
        }
    }

    LaunchedEffect(nfcIntentFromActivity.value) {
        val currentIntent = nfcIntentFromActivity.value
        if (currentIntent != null && (context is Activity) && showDialog) {
            Log.d("NFCWriter", "Processing NFC Intent from Activity state: ${currentIntent.action}")

            val tag: Tag? = @Suppress("DEPRECATION") currentIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            if (tag != null && dataToWrite != null) {

                val formattedDate = try {
                    val dateTime = LocalDateTime.parse(dataToWrite.tanggalWaktu, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                    dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
                } catch (e: DateTimeParseException) {
                    Log.e("NFCWriter", "Error parsing date string '${dataToWrite.tanggalWaktu}' for NFC. Falling back to simple replacement.", e)
                    dataToWrite.tanggalWaktu.split(" ")[0].replace('-', '/')
                } catch (e: Exception) {
                    Log.e("NFCWriter", "Unexpected error processing date for NFC: ${dataToWrite.tanggalWaktu}", e)
                    dataToWrite.tanggalWaktu.split(" ")[0].replace('-', '/')
                }

                val nfcDataString = "${dataToWrite.uniqueNo},${formattedDate},${dataToWrite.blok},${dataToWrite.totalBuah}"
                Log.d("NFCWriter", "NFC data string prepared: $nfcDataString")

                val lang = Locale.getDefault().language.toByteArray(Charset.forName("US-ASCII"))
                val textBytes = nfcDataString.toByteArray(Charset.forName("UTF-8"))
                val payload = ByteArray(lang.size + textBytes.size + 1)
                payload[0] = lang.size.toByte()
                System.arraycopy(lang, 0, payload, 1, lang.size)
                System.arraycopy(textBytes, 0, payload, lang.size + 1, textBytes.size)

                val nfcRecord = NdefRecord(
                    NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_TEXT,
                    ByteArray(0),
                    payload
                )

                val nfcMessage = NdefMessage(nfcRecord)


                Log.d("NFCWriter", "Attempting to write NdefMessage. Size: ${nfcMessage.toByteArray().size} bytes.")
                writeNdefMessageToTag(tag, nfcMessage) { success, message ->
                    nfcWriteStatusMessage = message
                    onWriteComplete(success, message)
                    if (context is MainActivity) {
                        context._nfcIntent.value = null
                    } else {
                        Log.w("NFCWriter", "Context is not MainActivity, cannot reset its NFC intent state directly.")
                    }
                    Log.d("NFCWriter", "NFC write finished. Success: $success, Message: $message")
                }
            } else if (dataToWrite == null) {
                nfcWriteStatusMessage = "Data untuk ditulis tidak tersedia."
                onWriteComplete(false, nfcWriteStatusMessage)
            } else {
                nfcWriteStatusMessage = "Stiker NFC tidak terdeteksi dengan benar."
                onWriteComplete(false, nfcWriteStatusMessage)
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
                text =nfcWriteStatusMessage,
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
