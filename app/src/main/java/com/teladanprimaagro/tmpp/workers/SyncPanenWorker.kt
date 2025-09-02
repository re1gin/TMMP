package com.teladanprimaagro.tmpp.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.PanenData
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

            // Map untuk menampung semua data yang akan diunggah dalam satu batch
            val firebaseUpdates = mutableMapOf<String, Any>()

            // Perbarui progres sebelum memulai proses
            val totalItems = unsyncedDataList.size
            val progressData = Data.Builder()
                .putFloat("progress", 0f)
                .putInt("total", totalItems)
                .build()
            setProgress(progressData)

            // Step 1: Unggah gambar satu per satu (tidak bisa di-batch)
            for ((index, panenData) in unsyncedDataList.withIndex()) {
                try {
                    var firebaseImageUrl: String? = panenData.firebaseImageUrl
                    var localFile: File?

                    if (panenData.localImageUri != null && panenData.firebaseImageUrl.isNullOrEmpty()) {
                        localFile = File(panenData.localImageUri.toUri().path ?: continue)

                        if (localFile.exists()) {
                            val imageName = UUID.randomUUID().toString()
                            val imageRef = storageRef.child("$imageName.jpg")

                            Log.d("SyncPanenWorker", "Uploading image for uniqueNo: ${panenData.uniqueNo}")
                            imageRef.putFile(panenData.localImageUri.toUri()).await()
                            firebaseImageUrl = imageRef.downloadUrl.await().toString()
                        } else {
                            Log.e("SyncPanenWorker", "Local image file not found: ${panenData.localImageUri}")
                            firebaseImageUrl = null
                        }
                    }

                    // Buat objek data yang akan diunggah ke Firebase Realtime Database
                    val firebaseData = panenData.copy(
                        firebaseImageUrl = firebaseImageUrl,
                        isSynced = true,
                        localImageUri = null
                    )

                    // Tambahkan data ke map untuk unggahan batch
                    firebaseUpdates[panenData.uniqueNo] = firebaseData

                    // Perbarui progress
                    val progress = ((index + 1).toFloat() / totalItems.toFloat())
                    val updatedProgressData = Data.Builder()
                        .putFloat("progress", progress)
                        .putInt("total", totalItems)
                        .build()
                    setProgress(updatedProgressData)

                } catch (e: Exception) {
                    Log.e("SyncPanenWorker", "Error processing panen data for uniqueNo: ${panenData.uniqueNo}. Skipping...", e)
                    continue
                }
            }

            // Step 2: Unggah semua data ke Firebase dalam satu panggilan batch
            if (firebaseUpdates.isNotEmpty()) {
                Log.d("SyncPanenWorker", "Uploading ${firebaseUpdates.size} items in a single batch to Realtime DB.")
                panenDbRef.updateChildren(firebaseUpdates).await()
                Log.d("SyncPanenWorker", "Batch upload successful.")

                // Step 3: Setelah berhasil, perbarui status di Room dan hapus file lokal
                for (panenData in unsyncedDataList) {
                    val updatedLocalData = panenData.copy(
                        isSynced = true,
                        firebaseImageUrl = firebaseUpdates[panenData.uniqueNo]?.let { (it as PanenData).firebaseImageUrl }
                    )
                    panenDao.updatePanen(updatedLocalData)
                    Log.d("SyncPanenWorker", "Panen data updated in local DB: ${updatedLocalData.uniqueNo}")

                    // Hapus file lokal setelah berhasil disinkronkan
                    if (panenData.localImageUri != null) {
                        deleteLocalImageFile(panenData.localImageUri.toUri())
                    }
                }
            }

            Log.d("SyncPanenWorker", "Sync process finished successfully.")
            return Result.success()
        } catch (e: Exception) {
            Log.e("SyncPanenWorker", "Major error during sync process. Retrying...", e)
            return Result.retry()
        }
    }

    private fun deleteLocalImageFile(uri: Uri) {
        try {
            val file = uri.path?.let { File(it) }
            if (file != null && file.exists()) {
                val isDeleted = file.delete()
                if (isDeleted) {
                    Log.d("SyncPanenWorker", "Local image file deleted successfully: ${file.path}")
                } else {
                    Log.w("SyncPanenWorker", "Failed to delete local image file: ${file.path}")
                }
            }
        } catch (e: Exception) {
            Log.e("SyncPanenWorker", "Error deleting local image file: ${uri.path}", e)
        }
    }
}