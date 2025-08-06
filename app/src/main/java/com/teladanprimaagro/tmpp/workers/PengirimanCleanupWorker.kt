package com.teladanprimaagro.tmpp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.teladanprimaagro.tmpp.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PengirimanCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val pengirimanDao = AppDatabase.getDatabase(applicationContext).pengirimanDao()
                pengirimanDao.clearAllPengiriman()
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}
