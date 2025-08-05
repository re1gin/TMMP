package com.teladanprimaagro.tmpp


import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.teladanprimaagro.tmpp.workers.ClearDataWorker
import java.util.concurrent.TimeUnit
import java.util.Calendar

class TmpApp : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleDailyDataReset()
    }

    private fun scheduleDailyDataReset() {
        // Tentukan waktu target (misalnya, 00:00)
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Jika waktu sekarang sudah melewati tengah malam,
            // jadwalkan untuk tengah malam berikutnya
            if (this.before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val initialDelay = midnight.timeInMillis - now.timeInMillis

        val clearDataRequest = PeriodicWorkRequestBuilder<ClearDataWorker>(
            repeatInterval = 1, // Ulangi setiap 1 hari
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            // Jadwalkan untuk berjalan pertama kali pada tengah malam
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ClearPanenDataDaily",
            ExistingPeriodicWorkPolicy.KEEP, // Pertahankan jadwal yang sudah ada jika Worker sudah dijadwalkan sebelumnya
            clearDataRequest
        )
    }
}