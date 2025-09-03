package com.teladanprimaagro.tmpp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.viewmodels.SimplePengirimanData
import kotlinx.coroutines.tasks.await

class SyncPengirimanWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val pengirimanDao = AppDatabase.getDatabase(appContext).pengirimanDao()
    private val pengirimanDbRef = FirebaseDatabase.getInstance("https://ineka-database.firebaseio.com/").getReference("pengirimanEntries")
    private val finalizedUniqueNoDbRef = FirebaseDatabase.getInstance("https://ineka-database.firebaseio.com/").getReference("finalizedUniqueNos")
    private val gson = Gson()

    override suspend fun doWork(): Result {
        Log.d("SyncPengirimanWorker", "Starting background data synchronization...")

        try {
            val unuploadedPengirimanList = pengirimanDao.getUnuploadedPengirimanData()
            val unuploadedFinalizedList = pengirimanDao.getUnuploadedFinalizedUniqueNos()

            if (unuploadedPengirimanList.isEmpty() && unuploadedFinalizedList.isEmpty()) {
                Log.d("SyncPengirimanWorker", "No data to sync. Sync complete.")
                return Result.success()
            }

            val totalItems = unuploadedPengirimanList.size + unuploadedFinalizedList.size
            setProgress(
                Data.Builder()
                    .putFloat("progress", 0f)
                    .putInt("total", totalItems)
                    .build()
            )

            var processedItems = 0

            // Bagian 1: Unggah data PengirimanData
            if (unuploadedPengirimanList.isNotEmpty()) {
                val batchUpdates = mutableMapOf<String, Any>()
                for (data in unuploadedPengirimanList) {
                    try {
                        val simplePengirimanData = mapToSimplePengirimanData(data)
                        val firebaseKey = data.spbNumber.replace('/', '-')
                        batchUpdates[firebaseKey] = simplePengirimanData
                    } catch (e: Exception) {
                        Log.e("SyncPengirimanWorker", "Error processing pengiriman data for SPB: ${data.spbNumber}. Skipping...", e)
                        continue
                    }
                }

                if (batchUpdates.isNotEmpty()) {
                    Log.d("SyncPengirimanWorker", "Uploading ${batchUpdates.size} pengiriman data in a single batch.")
                    pengirimanDbRef.updateChildren(batchUpdates).await()

                    // Perbarui status di database Room setelah unggahan batch berhasil
                    for (data in unuploadedPengirimanList) {
                        pengirimanDao.updatePengiriman(data.copy(isUploaded = true))
                    }
                    Log.d("SyncPengirimanWorker", "Batch upload of pengiriman data successful and local status updated.")
                }

                processedItems += unuploadedPengirimanList.size
                updateProgress(processedItems, totalItems)
            }

            // Bagian 2: Unggah data FinalizedUniqueNos
            if (unuploadedFinalizedList.isNotEmpty()) {
                val batchUpdates = mutableMapOf<String, Any>()
                for (item in unuploadedFinalizedList) {
                    try {
                        batchUpdates[item.uniqueNo] = item
                    } catch (e: Exception) {
                        Log.e("SyncPengirimanWorker", "Error processing finalized uniqueNo: ${item.uniqueNo}. Skipping...", e)
                        continue
                    }
                }

                if (batchUpdates.isNotEmpty()) {
                    Log.d("SyncPengirimanWorker", "Uploading ${batchUpdates.size} finalized unique numbers in a single batch.")
                    finalizedUniqueNoDbRef.updateChildren(batchUpdates).await()

                    // Perbarui status di database Room
                    for (item in unuploadedFinalizedList) {
                        pengirimanDao.updateFinalizedUniqueNo(item.copy(isUploaded = true))
                    }
                    Log.d("SyncPengirimanWorker", "Batch upload of finalized unique numbers successful.")
                }

                processedItems += unuploadedFinalizedList.size
                updateProgress(processedItems, totalItems)
            }

            Log.d("SyncPengirimanWorker", "Data synchronization finished successfully.")
            return Result.success()

        } catch (e: Exception) {
            Log.e("SyncPengirimanWorker", "Major error during sync process. Retrying...", e)
            return Result.retry()
        }
    }

    private suspend fun updateProgress(processed: Int, total: Int) {
        val progress = (processed.toFloat() / total.toFloat())
        setProgress(
            Data.Builder()
                .putFloat("progress", progress)
                .putInt("total", total)
                .build()
        )
    }

    private fun mapToSimplePengirimanData(pengirimanData: PengirimanData): SimplePengirimanData {
        val scannedItemsType = object : com.google.gson.reflect.TypeToken<List<com.teladanprimaagro.tmpp.viewmodels.ScannedItem>>() {}.type
        val rawDetailScannedItems: List<com.teladanprimaagro.tmpp.viewmodels.ScannedItem> = gson.fromJson(pengirimanData.detailScannedItemsJson, scannedItemsType) ?: emptyList()

        val ringkasanPerBlok = rawDetailScannedItems
            .groupBy { it.blok }
            .mapValues { (_, items) -> items.sumOf { it.totalBuah } }

        return SimplePengirimanData(
            spbNumber = pengirimanData.spbNumber,
            waktuPengiriman = pengirimanData.waktuPengiriman,
            namaSupir = pengirimanData.namaSupir,
            noPolisi = pengirimanData.noPolisi,
            mandorLoading = pengirimanData.mandorLoading,
            totalBuah = pengirimanData.totalBuah,
            ringkasanPerBlok = ringkasanPerBlok
        )
    }
}