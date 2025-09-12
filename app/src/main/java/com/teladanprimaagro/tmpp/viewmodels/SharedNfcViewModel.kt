@file:Suppress("DEPRECATION")
package com.teladanprimaagro.tmpp.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import com.teladanprimaagro.tmpp.data.PanenData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

sealed class NfcOperationState {
    object Idle : NfcOperationState() // Default state, no NFC operation in progress

    data class GeneralStatus(val isEnabled: Boolean, val message: String?) : NfcOperationState()

    data class WaitingForRead(val message: String = "Dekatkan tag NFC untuk memindai.") : NfcOperationState()

    data class Reading(val message: String = "Sedang membaca tag...") : NfcOperationState()

    data class ReadSuccess(val scannedItem: ScannedItem, val message: String = "Tag berhasil dibaca!") : NfcOperationState()

    data class ReadError(val message: String) : NfcOperationState()

    // --- States for Write Operations ---
    data class WaitingForWrite(val dataToWrite: PanenData?, val message: String = "Tempelkan stiker NFC untuk menulis data.") : NfcOperationState()

    data class Writing(val message: String = "Sedang menulis ke stiker...") : NfcOperationState()

    data class WriteSuccess(val message: String) : NfcOperationState()

    data class WriteError(val message: String) : NfcOperationState()
}

class SharedNfcViewModel : ViewModel() {

    private val _nfcState = MutableStateFlow<NfcOperationState>(NfcOperationState.Idle)
    val nfcState: StateFlow<NfcOperationState> = _nfcState.asStateFlow()

    private val _nfcIntent = MutableStateFlow<Intent?>(null)
    val nfcIntent: StateFlow<Intent?> = _nfcIntent.asStateFlow()

    fun updateGeneralNfcStatus(isEnabled: Boolean, message: String?) {
        _nfcState.value = NfcOperationState.GeneralStatus(isEnabled, message)
    }

    fun checkNfcAdapterStatus(context: Context) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        if (nfcAdapter == null) {
            updateGeneralNfcStatus(false, "NFC tidak tersedia di perangkat ini.")
        } else if (!nfcAdapter.isEnabled) {
            updateGeneralNfcStatus(false, "NFC dinonaktifkan. Harap aktifkan di pengaturan.")
        } else {
            updateGeneralNfcStatus(true, null)
            resetNfcState() // Reset state to prepare for a new scan
        }
        Log.d("SharedNfcViewModel", "NFC general status updated")
    }

    fun setWaitingForRead() {
        _nfcState.value = NfcOperationState.WaitingForRead()
    }

    fun setReading() {
        _nfcState.value = NfcOperationState.Reading()
    }

    fun setReadSuccess(scannedItem: ScannedItem, message: String = "Tag berhasil dibaca!") {
        _nfcState.value = NfcOperationState.ReadSuccess(scannedItem, message)
    }

    fun setReadError(message: String) {
        _nfcState.value = NfcOperationState.ReadError(message)
    }

    fun setWaitingForWrite(data: PanenData) {
        _nfcState.value = NfcOperationState.WaitingForWrite(data)
    }

    fun setWriting() {
        _nfcState.value = NfcOperationState.Writing()
    }

    fun setWriteSuccess(message: String) {
        _nfcState.value = NfcOperationState.WriteSuccess(message)
        _nfcState.value = NfcOperationState.Idle
    }

    fun setWriteError(message: String) {
        val currentData = (_nfcState.value as? NfcOperationState.WaitingForWrite)?.dataToWrite
        _nfcState.value = NfcOperationState.WriteError(message)
        _nfcState.value = NfcOperationState.WaitingForWrite(currentData)
    }

    fun resetNfcState() {
        _nfcState.value = NfcOperationState.Idle
    }

    fun resetNfcIntent() {
        _nfcIntent.value = null
        Log.d("SharedNfcViewModel", "NFC intent state reset")
    }

    fun handleNfcIntent(intent: Intent?) {
        _nfcIntent.value = intent
        if (intent != null && (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED || intent.action == NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Log.d("SharedNfcViewModel", "NFC Intent detected: ${intent.action}")
        }
    }

    @SuppressLint("NewApi")
    fun writeNfcData(tag: Tag, data: PanenData, onResult: (Boolean, String) -> Unit) {
        setWriting()
        val formattedDate = try {
            val dateTime = LocalDateTime.parse(data.tanggalWaktu, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
            dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
        } catch (e: DateTimeParseException) {
            Log.e("SharedNfcViewModel", "Error parsing date string '${data.tanggalWaktu}' for NFC. Falling back to simple replacement.", e)
            data.tanggalWaktu.split(" ")[0].replace('-', '/')
        } catch (e: Exception) {
            Log.e("SharedNfcViewModel", "Unexpected error processing date for NFC: ${data.tanggalWaktu}", e)
            data.tanggalWaktu.split(" ")[0].replace('-', '/')
        }

        val nfcDataString = "${data.uniqueNo},${formattedDate},${data.blok},${data.totalBuah}"
        Log.d("SharedNfcViewModel", "NFC data string prepared: $nfcDataString")

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

        Log.d("SharedNfcViewModel", "Attempting to write NdefMessage. Size: ${nfcMessage.toByteArray().size} bytes.")
        writeNdefMessageToTag(tag, nfcMessage) { success, message ->
            if (success) {
                setWriteSuccess(message)
            } else {
                setWriteError(message)
            }
            onResult(success, message)
        }
    }

    @SuppressLint("NewApi")
    fun readNfcData(intent: Intent, onResult: (ScannedItem?, String?) -> Unit) {
        setReading()
        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        val rawMessages: Array<out NdefMessage>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.mapNotNull { it as? NdefMessage }?.toTypedArray()
        }

        if (tag != null) {
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

                        Log.d("SharedNfcViewModel", "Raw NFC text: $text")

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
                    Log.e("SharedNfcViewModel", "Error processing NDEF message: ${e.message}", e)
                }
            } else if (ndef != null) {
                errorMessage = "Tidak ada pesan NDEF yang ditemukan pada tag."
            } else {
                errorMessage = "Tag tidak mendukung NDEF atau tidak dapat dibaca."
            }

            if (scannedData != null) {
                setReadSuccess(scannedData)
                onResult(scannedData, null)
            } else {
                setReadError(errorMessage ?: "Gagal membaca tag.")
                onResult(null, errorMessage)
            }
        } else {
            onResult(null, "Stiker NFC tidak terdeteksi dengan benar.")
        }
    }

    /**
     * Helper function to write NDEF message to a tag.
     * (Moved from NfcWriteDialog to avoid duplication)
     */
    private fun writeNdefMessageToTag(
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
                    Log.e("SharedNfcViewModel", "Error closing NDEF connection: ${e.message}", e)
                }
            }
        } else {
            onResult(false, "Stiker NFC tidak mendukung format NDEF.")
        }
    }
}