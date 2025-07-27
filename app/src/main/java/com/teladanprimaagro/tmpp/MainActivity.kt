package com.teladanprimaagro.tmpp

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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


class MainActivity : ComponentActivity() {
    // Gunakan _nfcIntent sebagai private MutableState dan expose nfcIntent sebagai public State
    // Ini memungkinkan pembaruan hanya dari dalam MainActivity
    internal var _nfcIntent: MutableState<Intent?> = mutableStateOf(null)
    val nfcIntent: State<Intent?> = _nfcIntent

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TeladanPrimaAgroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // PENTING: Teruskan nfcIntent ke AppNavigation
                    AppNavigation(nfcIntent = nfcIntent)
                }
            }
        }
        // Panggil handleIntent saat onCreate untuk menangani intent yang meluncurkan Activity ini
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called with action: ${intent.action}")
        // Perbarui state _nfcIntent agar LaunchedEffect di Composable bisa bereaksi
        _nfcIntent.value = intent
    }

    // Fungsi helper untuk menangani intent awal saat onCreate
    private fun handleIntent(intent: Intent?) {
        if (intent != null && (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED || intent.action == NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Log.d("MainActivity", "Initial NFC Intent detected in onCreate: ${intent.action}")
            _nfcIntent.value = intent
        }
    }
}