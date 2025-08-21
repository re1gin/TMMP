// SyncPanenWorker.kt

package com.teladanprimaagro.tmpp.workers

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.teladanprimaagro.tmpp.data.AppDatabase
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class SyncPanenWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val panenDao = AppDatabase.getDatabase(appContext).panenDao()
    private val panenDbRef = FirebaseDatabase.getInstance("https://ineka-database.firebaseio.com/").getReference("panenEntries")
    private val storageRef = FirebaseStorage.getInstance().reference.child("images")

    override suspend fun doWork(): Result {
        Log.d("SyncPanenWorker", "Starting sync work...")

        try {
            val unsyncedDataList = panenDao.getUnsyncedPanenData()

            if (unsyncedDataList.isEmpty()) {
                Log.d("SyncPanenWorker", "No unsynced data found. Sync complete.")
                return Result.success()
            }

            val totalItems = unsyncedDataList.size
            for (index in unsyncedDataList.indices) {
                val panenData = unsyncedDataList[index]

                try {
                    // Perbarui progres sebelum memproses setiap item
                    val progress = ((index + 1).toFloat() / totalItems.toFloat())
                    val progressData = Data.Builder()
                        .putFloat("progress", progress)
                        .putInt("total", totalItems)
                        .build()
                    setProgress(progressData)

                    var firebaseImageUrl: String? = panenData.firebaseImageUrl

                    // Cek apakah ada gambar lokal yang belum diunggah
                    if (panenData.localImageUri != null && panenData.firebaseImageUrl.isNullOrEmpty()) {
                        val localFile = File(panenData.localImageUri.toUri().path ?: continue)

                        if (localFile.exists()) {
                            val imageName = UUID.randomUUID().toString()
                            val imageRef = storageRef.child("$imageName.jpg")

                            Log.d("SyncPanenWorker", "Uploading image for uniqueNo: ${panenData.uniqueNo}")
                            imageRef.putFile(panenData.localImageUri.toUri()).await()
                            firebaseImageUrl = imageRef.downloadUrl.await().toString()

                        } else {
                            Log.e("SyncPanenWorker", "Local image file not found: ${panenData.localImageUri}")
                            // Jika file lokal tidak ada, lewati unggahan gambar
                            firebaseImageUrl = null
                        }
                    }

                    // Buat objek data yang akan diunggah ke Firebase Realtime Database
                    // localImageUri di sini dihapus agar tidak terkirim ke Firebase
                    val firebaseData = panenData.copy(
                        firebaseImageUrl = firebaseImageUrl,
                        isSynced = true,
                        localImageUri = null
                    )

                    // Unggah data ke Firebase Realtime Database
                    Log.d("SyncPanenWorker", "Uploading panen data to Realtime DB: ${firebaseData.uniqueNo}")
                    panenDbRef.child(firebaseData.uniqueNo).setValue(firebaseData).await()

                    // Perbarui status di database Room lokal
                    val updatedLocalData = panenData.copy(
                        firebaseImageUrl = firebaseImageUrl,
                        isSynced = true,
                    )
                    panenDao.updatePanen(updatedLocalData)
                    Log.d("SyncPanenWorker", "Panen data updated in local DB: ${updatedLocalData.uniqueNo}")

                } catch (e: Exception) {
                    Log.e("SyncPanenWorker", "Error processing panen data for uniqueNo: ${panenData.uniqueNo}. Skipping...", e)
                    continue
                }
            }
            Log.d("SyncPanenWorker", "Sync process finished successfully.")
            return Result.success()
        } catch (e: Exception) {
            Log.e("SyncPanenWorker", "Major error during sync process. Retrying...", e)
            return Result.retry()
        }
    }
}