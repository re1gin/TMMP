package com.teladanprimaagro.tmpp.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.teladanprimaagro.tmpp.data.PanenData

/**
 * Sealed class yang merepresentasikan semua kemungkinan status dan hasil dari operasi NFC.
 * Ini menyediakan interface yang seragam untuk UI dalam bereaksi terhadap interaksi NFC.
 */
sealed class NfcOperationState {
    object Idle : NfcOperationState() // Kondisi default, tidak ada operasi NFC yang sedang berlangsung

    /**
     * Menyediakan status umum ketersediaan dan aktivasi NFC di perangkat.
     * @param isEnabled True jika NFC diaktifkan, false jika dinonaktifkan atau tidak tersedia.
     * @param message Pesan opsional tentang status (misalnya, "NFC dinonaktifkan").
     */
    data class GeneralStatus(val isEnabled: Boolean, val message: String?) : NfcOperationState()

    // --- State untuk Operasi Baca (Read) ---
    /**
     * Menandakan aplikasi sedang menunggu tag NFC untuk dibaca.
     * @param message Pesan yang ditampilkan kepada pengguna.
     */
    data class WaitingForRead(val message: String = "Dekatkan tag NFC untuk memindai.") : NfcOperationState()

    /**
     * Menandakan aplikasi sedang dalam proses membaca tag NFC.
     * @param message Pesan yang ditampilkan kepada pengguna.
     */
    data class Reading(val message: String = "Sedang membaca tag...") : NfcOperationState()

    /**
     * Menandakan operasi pembacaan NFC berhasil.
     * @param scannedItem Objek ScannedItem yang berisi data yang berhasil dibaca.
     * @param message Pesan sukses.
     */
    data class ReadSuccess(val scannedItem: ScannedItem, val message: String = "Tag berhasil dibaca!") : NfcOperationState()

    /**
     * Menandakan operasi pembacaan NFC gagal.
     * @param message Pesan error.
     */
    data class ReadError(val message: String) : NfcOperationState()

    // --- State untuk Operasi Tulis (Write) ---
    /**
     * Menandakan aplikasi sedang menunggu tag NFC untuk ditulis.
     * @param dataToWrite Objek PanenData yang akan ditulis ke tag.
     * @param message Pesan yang ditampilkan kepada pengguna.
     */
    data class WaitingForWrite(val dataToWrite: PanenData?, val message: String = "Tempelkan stiker NFC untuk menulis data.") : NfcOperationState()

    /**
     * Menandakan aplikasi sedang dalam proses menulis ke tag NFC.
     * @param message Pesan yang ditampilkan kepada pengguna.
     */
    data class Writing(val message: String = "Sedang menulis ke stiker...") : NfcOperationState()

    /**
     * Menandakan operasi penulisan NFC berhasil.
     * @param message Pesan sukses.
     */
    data class WriteSuccess(val message: String) : NfcOperationState()

    /**
     * Menandakan operasi penulisan NFC gagal.
     * @param message Pesan error.
     */
    data class WriteError(val message: String) : NfcOperationState()
}

/**
 * ViewModel bersama untuk mengelola dan menyediakan status interaksi NFC di seluruh aplikasi.
 * Ini memungkinkan berbagai komponen UI (layar baca dan tulis) untuk mengamati satu sumber kebenaran
 * mengenai status operasi NFC.
 */
class SharedNfcViewModel : ViewModel() {

    // MutableStateFlow yang menyimpan status NFC saat ini
    private val _nfcState = MutableStateFlow<NfcOperationState>(NfcOperationState.Idle)

    // StateFlow publik yang dapat diamati oleh Composable
    val nfcState: StateFlow<NfcOperationState> = _nfcState.asStateFlow()

    // Tambahan: Properti untuk menampung NFC Intent dari Activity
    private val _nfcIntent = MutableStateFlow<Intent?>(null)
    val nfcIntent: StateFlow<Intent?> = _nfcIntent.asStateFlow()

    /**
     * Mengupdate status umum NFC di perangkat (misalnya, NFC diaktifkan/dinonaktifkan).
     * @param isEnabled Status aktivasi NFC.
     * @param message Pesan status opsional.
     */
    fun updateGeneralNfcStatus(isEnabled: Boolean, message: String?) {
        _nfcState.value = NfcOperationState.GeneralStatus(isEnabled, message)
    }

    /**
     * Mengatur state ke menunggu tag untuk operasi baca.
     */
    fun setWaitingForRead() {
        _nfcState.value = NfcOperationState.WaitingForRead()
    }

    /**
     * Mengatur state ke sedang membaca tag.
     */
    fun setReading() {
        _nfcState.value = NfcOperationState.Reading()
    }

    /**
     * Mengatur state ke sukses membaca tag.
     * @param scannedItem Data yang berhasil dibaca dari tag.
     * @param message Pesan sukses.
     */
    fun setReadSuccess(scannedItem: ScannedItem, message: String = "Tag berhasil dibaca!") {
        _nfcState.value = NfcOperationState.ReadSuccess(scannedItem, message)
    }

    /**
     * Mengatur state ke gagal membaca tag.
     * @param message Pesan error.
     */
    fun setReadError(message: String) {
        _nfcState.value = NfcOperationState.ReadError(message)
    }

    /**
     * Mengatur state ke menunggu tag untuk operasi tulis.
     * @param data Objek PanenData yang akan ditulis.
     */
    fun setWaitingForWrite(data: PanenData) {
        _nfcState.value = NfcOperationState.WaitingForWrite(data)
    }

    /**
     * Mengatur state ke sedang menulis ke tag.
     */
    fun setWriting() {
        _nfcState.value = NfcOperationState.Writing()
    }

    /**
     * Mengatur state ke sukses menulis ke tag.
     * @param message Pesan sukses.
     */
    fun setWriteSuccess(message: String) {
        _nfcState.value = NfcOperationState.WriteSuccess(message)
        // Setelah operasi tulis selesai (sukses), kembali ke Idle
        _nfcState.value = NfcOperationState.Idle
    }

    /**
     * Mengatur state ke gagal menulis ke tag.
     * @param message Pesan error.
     */
    fun setWriteError(message: String) {
        _nfcState.value = NfcOperationState.WriteError(message)
        // Setelah error, bisa memilih untuk kembali ke WaitingForWrite
        // agar user bisa mencoba lagi dengan data yang sama
        val currentData = (_nfcState.value as? NfcOperationState.WaitingForWrite)?.dataToWrite
        _nfcState.value = NfcOperationState.WaitingForWrite(currentData) // Pertahankan data untuk penulisan ulang
    }

    /**
     * Mengatur ulang state NFC ke Idle. Berguna saat dialog ditutup
     * atau ketika operasi NFC tidak lagi relevan untuk layar saat ini.
     */
    fun resetNfcState() {
        _nfcState.value = NfcOperationState.Idle
    }

    /**
     * Menerima dan memproses Intent NFC yang datang dari Activity.
     * @param intent Intent yang berisi data NFC.
     */
    fun handleNfcIntent(intent: Intent?) {
        _nfcIntent.value = intent
    }
}
