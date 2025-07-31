package com.teladanprimaagro.tmpp

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.teladanprimaagro.tmpp.ui.navigation.AppNavigation
import com.teladanprimaagro.tmpp.ui.theme.TeladanPrimaAgroTheme
import com.teladanprimaagro.tmpp.ui.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.ui.viewmodels.SharedNfcViewModel

class MainActivity : ComponentActivity() {
    // Properti untuk NFC Intent, akan digunakan oleh NfcWriteDialog DAN NfcReadDialog
    internal var _nfcIntent: MutableState<Intent?> = mutableStateOf(null)
    val nfcIntent: State<Intent?> = _nfcIntent

    private val pengirimanViewModel: PengirimanViewModel by viewModels()
    private val sharedNfcViewModel: SharedNfcViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TeladanPrimaAgroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        nfcIntent = nfcIntent,
                        pengirimanViewModel = pengirimanViewModel,
                        sharedNfcViewModel = sharedNfcViewModel
                    )
                }
            }
        }
        // Handle intent awal jika aplikasi diluncurkan oleh NFC
        handleInitialIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Saat resume, perbarui status umum NFC
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            sharedNfcViewModel.updateGeneralNfcStatus(false, "NFC tidak tersedia di perangkat ini.")
        } else if (!nfcAdapter.isEnabled) {
            sharedNfcViewModel.updateGeneralNfcStatus(false, "NFC dinonaktifkan. Harap aktifkan di pengaturan.")
        } else {
            sharedNfcViewModel.updateGeneralNfcStatus(true, null)
            // Jangan langsung set WaitingForRead/Write di onResume, biarkan dialog yang memintanya
            sharedNfcViewModel.resetNfcState() // Reset state saat resume agar dialog bisa memulainya
        }
        Log.d("MainActivity", "NFC general status updated onResume")
    }

    override fun onPause() {
        super.onPause()
        // Penting: Pastikan tidak ada foreground dispatch yang aktif di sini,
        // karena itu adalah tanggung jawab dialog.
        // Cukup reset state NFC secara umum.
        sharedNfcViewModel.resetNfcState()
        Log.d("MainActivity", "NFC state reset onPause")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called with action: ${intent.action}")
        // Set intent baru ke state mutable. Dialog-dialog akan mengamatinya.
        _nfcIntent.value = intent
    }

    private fun handleInitialIntent(intent: Intent?) {
        if (intent != null && (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED || intent.action == NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Log.d("MainActivity", "Initial NFC Intent detected in onCreate: ${intent.action}")
            _nfcIntent.value = intent
            // Jika aplikasi diluncurkan oleh intent NFC, asumsikan itu untuk penulisan
            // (karena PanenInputScreen adalah satu-satunya yang memicu dialog dengan intent awal ini)
            sharedNfcViewModel.setWriting() // Atau bisa lebih spesifik jika ada skenario lain
        }
    }
}