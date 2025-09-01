package com.teladanprimaagro.tmpp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
            val unuploadedPengirimanList = pengirimanDao. getUnuploadedPengirimanData()
            val unuploadedFinalizedList = pengirimanDao.getUnuploadedFinalizedUniqueNos()

            if (unuploadedPengirimanList.isEmpty() && unuploadedFinalizedList.isEmpty()) {
                Log.d("SyncPengirimanWorker", "No data to sync. Sync complete.")
                return Result.success()
            }

            if (unuploadedPengirimanList.isNotEmpty()) {
                val batchUpdates = mutableMapOf<String, Any>()
                for (data in unuploadedPengirimanList) {
                    val simplePengirimanData = mapToSimplePengirimanData(data)
                    val firebaseKey = data.spbNumber.replace('/', '-')
                    batchUpdates[firebaseKey] = simplePengirimanData
                }

                // Lakukan unggahan batch ke Firebase Realtime Database
                Log.d("SyncPengirimanWorker", "Uploading ${batchUpdates.size} pengiriman data in a single batch.")
                pengirimanDbRef.updateChildren(batchUpdates).await()

                // Perbarui status di database Room setelah unggahan batch berhasil
                for (data in unuploadedPengirimanList) {
                    pengirimanDao.updatePengiriman(data.copy(isUploaded = true))
                }
                Log.d("SyncPengirimanWorker", "Batch upload of pengiriman data successful and local status updated.")
            }

            // Bagian 2: Unggah data FinalizedUniqueNos
            if (unuploadedFinalizedList.isNotEmpty()) {
                val batchUpdates = mutableMapOf<String, Any>()
                for (item in unuploadedFinalizedList) {
                    batchUpdates[item.uniqueNo] = item
                }

                Log.d("SyncPengirimanWorker", "Uploading ${batchUpdates.size} finalized unique numbers in a single batch.")
                finalizedUniqueNoDbRef.updateChildren(batchUpdates).await()

                // Perbarui status di database Room
                for (item in unuploadedFinalizedList) {
                    pengirimanDao.updateFinalizedUniqueNo(item.copy(isUploaded = true))
                }
                Log.d("SyncPengirimanWorker", "Batch upload of finalized unique numbers successful.")
            }

            Log.d("SyncPengirimanWorker", "Data synchronization finished successfully.")
            return Result.success()

        } catch (e: Exception) {
            Log.e("SyncPengirimanWorker", "Error during synchronization: ${e.message}", e)
            // Jika terjadi kesalahan, minta WorkManager untuk mencoba lagi
            return Result.retry()
        }
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
