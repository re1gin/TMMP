package com.teladanprimaagro.tmpp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.teladanprimaagro.tmpp.navigation.AppNavigation
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainTheme
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel
import com.teladanprimaagro.tmpp.workers.PanenCleanupWorker
import com.teladanprimaagro.tmpp.workers.PengirimanCleanupWorker
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {

    private val sharedNfcViewModel: SharedNfcViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scheduleDailyCleanupWorkers(application)

        sharedNfcViewModel.checkNfcAdapterStatus(this)

        setContent {
            MainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MainBackground
                ) {
                    AppNavigation(
                        sharedNfcViewModel = sharedNfcViewModel
                    )
                }
            }
        }

        // Handle the initial NFC intent
        sharedNfcViewModel.handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Check NFC status when the app becomes active
        sharedNfcViewModel.checkNfcAdapterStatus(this)
        Log.d("MainActivity", "onResume called, NFC status checked")
    }

    override fun onPause() {
        super.onPause()
        // Reset the NFC state when the app is paused
        sharedNfcViewModel.resetNfcState()
        Log.d("MainActivity", "NFC state reset onPause")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called with action: ${intent.action}")
        sharedNfcViewModel.handleNfcIntent(intent)
    }

    fun scheduleDailyCleanupWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Tentukan waktu target (05:00 pagi)
        val targetTime = LocalTime.of(5, 0, 0)

        val now = LocalDateTime.now()

        // Tentukan waktu target penuh (tanggal + waktu)
        var dueTime = now.with(targetTime)

        // Jika waktu target sudah lewat hari ini, majukan ke hari berikutnya
        if (now.isAfter(dueTime)) {
            dueTime = dueTime.plusDays(1)
        }

        val duration = java.time.Duration.between(now, dueTime)
        val initialDelay = duration.toMillis()

        Log.d("Scheduler", "Initial delay for next 5 AM is: $initialDelay ms")

        // Buat dan jadwalkan PanenCleanupWorker
        val panenCleanupRequest = PeriodicWorkRequestBuilder<PanenCleanupWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("PanenCleanupWork")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "PanenCleanupWork",
            ExistingPeriodicWorkPolicy.KEEP,
            panenCleanupRequest
        )

        // Buat dan jadwalkan PengirimanCleanupWorker
        val pengirimanCleanupRequest = PeriodicWorkRequestBuilder<PengirimanCleanupWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("PengirimanCleanupWork")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "PengirimanCleanupWork",
            ExistingPeriodicWorkPolicy.KEEP,
            pengirimanCleanupRequest
        )
    }
}