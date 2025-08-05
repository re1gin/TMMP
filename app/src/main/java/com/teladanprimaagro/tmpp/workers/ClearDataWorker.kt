
package com.teladanprimaagro.tmpp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.teladanprimaagro.tmpp.data.AppDatabase

class ClearDataWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val panenDao = AppDatabase.getDatabase(applicationContext).panenDao()
        val pengirimanDao = AppDatabase.getDatabase(applicationContext).pengirimanDao()
        return try {
            panenDao.clearAllPanen()
            pengirimanDao.clearAllPengiriman()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}