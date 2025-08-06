package com.teladanprimaagro.tmpp

import android.app.Application
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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.teladanprimaagro.tmpp.ui.navigation.AppNavigation
import com.teladanprimaagro.tmpp.ui.theme.TeladanPrimaAgroTheme
import com.teladanprimaagro.tmpp.ui.viewmodels.PanenViewModel
import com.teladanprimaagro.tmpp.ui.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.ui.viewmodels.SharedNfcViewModel
import com.teladanprimaagro.tmpp.workers.PanenCleanupWorker
import com.teladanprimaagro.tmpp.workers.PengirimanCleanupWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    internal var _nfcIntent: MutableState<Intent?> = mutableStateOf(null)
    val nfcIntent: State<Intent?> = _nfcIntent

    private val panenViewModel: PanenViewModel by viewModels()
    private val pengirimanViewModel: PengirimanViewModel by viewModels()
    private val sharedNfcViewModel: SharedNfcViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scheduleDailyCleanupWorkers(application)

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
        handleInitialIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            sharedNfcViewModel.updateGeneralNfcStatus(false, "NFC tidak tersedia di perangkat ini.")
        } else if (!nfcAdapter.isEnabled) {
            sharedNfcViewModel.updateGeneralNfcStatus(false, "NFC dinonaktifkan. Harap aktifkan di pengaturan.")
        } else {
            sharedNfcViewModel.updateGeneralNfcStatus(true, null)
            sharedNfcViewModel.resetNfcState()
        }
        Log.d("MainActivity", "NFC general status updated onResume")
    }

    override fun onPause() {
        super.onPause()
        sharedNfcViewModel.resetNfcState()
        Log.d("MainActivity", "NFC state reset onPause")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called with action: ${intent.action}")
        _nfcIntent.value = intent
    }

    private fun handleInitialIntent(intent: Intent?) {
        if (intent != null && (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED || intent.action == NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Log.d("MainActivity", "Initial NFC Intent detected in onCreate: ${intent.action}")
            _nfcIntent.value = intent
            sharedNfcViewModel.setWriting()
        }
    }

    private fun scheduleDailyCleanupWorkers(application: Application) {
        val workManager = WorkManager.getInstance(application)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()

        val panenCleanupRequest = PeriodicWorkRequestBuilder<PanenCleanupWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("PanenCleanupWork")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "PanenCleanupWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            panenCleanupRequest
        )

        val pengirimanCleanupRequest = PeriodicWorkRequestBuilder<PengirimanCleanupWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("PengirimanCleanupWork")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "PengirimanCleanupWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            pengirimanCleanupRequest
        )
    }
}
