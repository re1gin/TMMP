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
import com.teladanprimaagro.tmpp.viewmodels.ScannedItem
import com.teladanprimaagro.tmpp.viewmodels.SimplePengirimanData
import kotlinx.coroutines.tasks.await

class SyncPengirimanWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val pengirimanDao = AppDatabase.getDatabase(appContext).pengirimanDao()
    private val pengirimanDbRef = FirebaseDatabase
        .getInstance("https://ineka-database.firebaseio.com/")
        .getReference("pengirimanEntries")
    private val finalizedUniqueNoDbRef = FirebaseDatabase
        .getInstance("https://ineka-database.firebaseio.com/")
        .getReference("finalizedUniqueNos")
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

            val firebasePengirimanUpdates = mutableMapOf<String, Any>()
            val firebaseFinalizedUpdates = mutableMapOf<String, Any>()

            for ((index, data) in unuploadedPengirimanList.withIndex()) {
                try {
                    val simplePengirimanData = mapToSimplePengirimanData(data)
                    val firebaseKey = data.spbNumber.replace('/', '-')
                    firebasePengirimanUpdates[firebaseKey] = simplePengirimanData

                    // Update progress untuk item ini
                    val progress = ((index + 1).toFloat() / totalItems.toFloat())
                    val progressData = Data.Builder()
                        .putString("currentWorkerId", data.spbNumber)
                        .putFloat("progress", progress)
                        .build()
                    setProgress(progressData)

                    Log.d("SyncPengirimanWorker", "Progress for ${data.spbNumber}: ${progress * 100}%")

                } catch (e: Exception) {
                    Log.e("SyncPengirimanWorker", "Error processing pengiriman data for SPB: ${data.spbNumber}. Skipping...", e)
                    continue
                }
            }

            for ((index, item) in unuploadedFinalizedList.withIndex()) {
                try {
                    firebaseFinalizedUpdates[item.uniqueNo] = item

                    val offsetIndex = unuploadedPengirimanList.size + index
                    val progress = ((offsetIndex + 1).toFloat() / totalItems.toFloat())
                    val progressData = Data.Builder()
                        .putString("currentWorkerId", item.uniqueNo)
                        .putFloat("progress", progress)
                        .build()
                    setProgress(progressData)

                    Log.d("SyncPengirimanWorker", "Progress for ${item.uniqueNo}: ${progress * 100}%")

                } catch (e: Exception) {
                    Log.e("SyncPengirimanWorker", "Error processing finalized uniqueNo: ${item.uniqueNo}. Skipping...", e)
                    continue
                }
            }

            // Batch upload PengirimanData
            if (firebasePengirimanUpdates.isNotEmpty()) {
                Log.d("SyncPengirimanWorker", "Uploading ${firebasePengirimanUpdates.size} pengiriman data in a single batch.")
                pengirimanDbRef.updateChildren(firebasePengirimanUpdates).await()
                Log.d("SyncPengirimanWorker", "Batch upload of pengiriman data successful.")

                // Perbarui status di database Room setelah unggahan batch berhasil
                for (data in unuploadedPengirimanList) {
                    pengirimanDao.updatePengiriman(data.copy(isUploaded = true))
                }
                Log.d("SyncPengirimanWorker", "Local status updated for pengiriman data.")
            }

            // Batch upload FinalizedUniqueNos
            if (firebaseFinalizedUpdates.isNotEmpty()) {
                Log.d("SyncPengirimanWorker", "Uploading ${firebaseFinalizedUpdates.size} finalized unique numbers in a single batch.")
                finalizedUniqueNoDbRef.updateChildren(firebaseFinalizedUpdates).await()
                Log.d("SyncPengirimanWorker", "Batch upload of finalized unique numbers successful.")

                // Perbarui status di database Room
                for (item in unuploadedFinalizedList) {
                    pengirimanDao.updateFinalizedUniqueNo(item.copy(isUploaded = true))
                }
                Log.d("SyncPengirimanWorker", "Local status updated for finalized unique numbers.")
            }

            Log.d("SyncPengirimanWorker", "Data synchronization finished successfully.")
            return Result.success()

        } catch (e: Exception) {
            Log.e("SyncPengirimanWorker", "Major error during sync process. Retrying...", e)
            return Result.retry()
        }
    }

    private fun mapToSimplePengirimanData(pengirimanData: PengirimanData): SimplePengirimanData {
        val scannedItemsType = object : com.google.gson.reflect.TypeToken<List<ScannedItem>>() {}.type
        val rawDetailScannedItems: List<ScannedItem> = gson.fromJson(pengirimanData.detailScannedItemsJson, scannedItemsType) ?: emptyList()

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