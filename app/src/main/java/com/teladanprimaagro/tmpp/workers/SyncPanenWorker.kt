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
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class SyncPanenWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val panenDao = AppDatabase.getDatabase(appContext).panenDao()
    private val panenDbRef = FirebaseDatabase
        .getInstance("https://ineka-database.firebaseio.com/")
        .getReference("panenEntries")
    private val storageRef = FirebaseStorage.getInstance().reference.child("images")

    override suspend fun doWork(): Result {
        Log.d("SyncPanenWorker", "Starting sync work...")

        try {
            val unsyncedDataList = panenDao.getUnsyncedPanenData()

            if (unsyncedDataList.isEmpty()) {
                Log.d("SyncPanenWorker", "No unsynced data found. Sync complete.")
                return Result.success()
            }

            val firebaseUpdates = mutableMapOf<String, Any>()

            val totalItems = unsyncedDataList.size
            setProgress(
                Data.Builder()
                    .putFloat("progress", 0f)
                    .putInt("total", totalItems)
                    .build()
            )

            for ((index, panenData) in unsyncedDataList.withIndex()) {
                try {
                    var firebaseImageUrl: String? = panenData.firebaseImageUrl

                    // Upload gambar jika belum pernah diunggah
                    if (panenData.localImageUri != null && panenData.firebaseImageUrl.isNullOrEmpty()) {
                        val localFile = File(panenData.localImageUri.toUri().path ?: continue)

                        if (localFile.exists()) {
                            val imageName = UUID.randomUUID().toString()
                            val imageRef = storageRef.child("$imageName.jpg")

                            // Retry upload maksimal 3x
                            var attempts = 0
                            var uploaded = false
                            while (attempts < 100 && !uploaded) {
                                try {
                                    Log.d("SyncPanenWorker", "Uploading image (try ${attempts + 1}) for uniqueNo: ${panenData.uniqueNo}")
                                    imageRef.putFile(panenData.localImageUri.toUri()).await()
                                    firebaseImageUrl = imageRef.downloadUrl.await().toString()
                                    uploaded = true
                                } catch (e: Exception) {
                                    attempts++
                                    if (attempts < 3) {
                                        Log.w("SyncPanenWorker", "Upload failed, retrying...", e)
                                        delay(2000L * attempts) // exponential kecil
                                    } else {
                                        Log.e("SyncPanenWorker", "Image upload failed after 3 attempts: ${panenData.localImageUri}", e)
                                        firebaseImageUrl = null
                                    }
                                }
                            }
                        } else {
                            Log.e("SyncPanenWorker", "Local image file not found: ${panenData.localImageUri}")
                            firebaseImageUrl = null
                        }
                    }

                    // Data untuk Firebase
                    val firebaseData = panenData.copy(
                        firebaseImageUrl = firebaseImageUrl,
                        isSynced = true,
                        localImageUri = null
                    )

                    firebaseUpdates[panenData.uniqueNo] = firebaseData

                    // Update progress
                    val progress = ((index + 1).toFloat() / totalItems.toFloat())
                    val progressData = Data.Builder()
                        .putString("currentUniqueNo", panenData.uniqueNo)
                        .putFloat("progress", progress)
                        .build()
                    setProgress(progressData)

                    Log.d("SyncPanenWorker", "Progress for ${panenData.uniqueNo}: ${progress * 100}%")

                } catch (e: Exception) {
                    Log.e("SyncPanenWorker", "Error processing panen data for uniqueNo: ${panenData.uniqueNo}. Skipping...", e)
                    continue
                }
            }

            if (firebaseUpdates.isNotEmpty()) {
                Log.d("SyncPanenWorker", "Uploading ${firebaseUpdates.size} items in a single batch to Realtime DB.")
                panenDbRef.updateChildren(firebaseUpdates).await()
                Log.d("SyncPanenWorker", "Batch upload successful.")

                // Update status di Room setelah sukses
                for (panenData in unsyncedDataList) {
                    val updatedLocalData = panenData.copy(
                        isSynced = true,
                        firebaseImageUrl = (firebaseUpdates[panenData.uniqueNo] as? com.teladanprimaagro.tmpp.data.PanenData)?.firebaseImageUrl
                    )
                    panenDao.updatePanen(updatedLocalData)
                    Log.d("SyncPanenWorker", "Panen data updated in local DB: ${updatedLocalData.uniqueNo}")
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