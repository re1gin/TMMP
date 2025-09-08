package com.teladanprimaagro.tmpp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.teladanprimaagro.tmpp.navigation.AppNavigation
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainTheme
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel


class MainActivity : ComponentActivity() {

    private val sharedNfcViewModel: SharedNfcViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        scheduleMonthlyCleanupWorkers(application)

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

//    fun scheduleMonthlyCleanupWorkers(context: Context) {
//        val workManager = WorkManager.getInstance(context)
//
//        val targetTime = LocalTime.of(5, 0, 0)
//        val now = LocalDateTime.now()
//
//        var dueTime = now.with(TemporalAdjusters.firstDayOfNextMonth()).with(targetTime)
//
//        if (now.isAfter(dueTime)) {
//            dueTime = dueTime.plusMonths(1)
//        }
//
//        val duration = java.time.Duration.between(now, dueTime)
//        val initialDelay = duration.toMillis()
//
//        Log.d("Scheduler", "Initial delay for next 1st of month at 5 AM is: $initialDelay ms")
//
//        val panenCleanupRequest = PeriodicWorkRequestBuilder<PanenCleanupWorker>(
//            repeatInterval = 30, TimeUnit.DAYS
//        )
//            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
//            .addTag("PanenCleanupWork")
//            .build()
//
//        workManager.enqueueUniquePeriodicWork(
//            "PanenCleanupWork",
//            ExistingPeriodicWorkPolicy.KEEP,
//            panenCleanupRequest
//        )
//
//        val pengirimanCleanupRequest = PeriodicWorkRequestBuilder<PengirimanCleanupWorker>(
//            repeatInterval = 30, TimeUnit.DAYS
//        )
//            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
//            .addTag("PengirimanCleanupWork")
//            .build()
//
//        workManager.enqueueUniquePeriodicWork(
//            "PengirimanCleanupWork",
//            ExistingPeriodicWorkPolicy.KEEP,
//            pengirimanCleanupRequest
//        )
//    }
}