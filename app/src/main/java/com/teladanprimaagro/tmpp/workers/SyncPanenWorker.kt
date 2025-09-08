package com.teladanprimaagro.tmpp.workers

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.PanenData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val unsyncedDataList = panenDao.getUnsyncedPanenData()
            if (unsyncedDataList.isEmpty()) {
                Log.d("SyncPanenWorker", "No unsynced data found. Returning success.")
                return@withContext Result.success()
            }

            val totalItems = unsyncedDataList.size
            Log.d("SyncPanenWorker", "Found $totalItems unsynced items.")
            setProgress(Data.Builder().putFloat("progress", 0f).putInt("total", totalItems).build())

            // Unggah gambar secara paralel
            val deferredUpdates = unsyncedDataList.map { panenData ->
                async {
                    var firebaseImageUrl: String? = panenData.firebaseImageUrl
                    try {
                        if (!panenData.localImageUri.isNullOrEmpty() && panenData.firebaseImageUrl.isNullOrEmpty()) {
                            val localFile = File(panenData.localImageUri.toUri().path ?: throw IllegalArgumentException("Invalid local image URI path"))
                            if (localFile.exists()) {
                                val imageName = UUID.randomUUID().toString()
                                val imageRef = storageRef.child("$imageName.jpg")
                                imageRef.putFile(panenData.localImageUri.toUri()).await()
                                firebaseImageUrl = imageRef.downloadUrl.await().toString()
                                Log.d("SyncPanenWorker", "Image uploaded successfully for uniqueNo: ${panenData.uniqueNo}")
                            } else {
                                Log.e("SyncPanenWorker", "Local image file not found: ${panenData.localImageUri}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SyncPanenWorker", "Error uploading image for uniqueNo: ${panenData.uniqueNo}", e)
                        // Jika gagal upload gambar, tetap lanjutkan sinkronisasi data tanpa gambar
                        firebaseImageUrl = null
                    }
                    panenData to firebaseImageUrl
                }
            }

            val results = deferredUpdates.awaitAll()

            val firebaseUpdates = mutableMapOf<String, Any>()
            val updatedPanenDataList = mutableListOf<PanenData>()

            // Proses hasil unggahan dan persiapkan data untuk Firebase
            for ((panenData, firebaseImageUrl) in results) {
                val updatedPanenData = panenData.copy(
                    firebaseImageUrl = firebaseImageUrl,
                    isSynced = true,
                    localImageUri = null
                )
                firebaseUpdates[updatedPanenData.uniqueNo] = updatedPanenData.toFirebaseMap()
                updatedPanenDataList.add(updatedPanenData)
            }

            if (firebaseUpdates.isNotEmpty()) {
                Log.d("SyncPanenWorker", "Uploading ${firebaseUpdates.size} items in a single batch to Realtime DB.")
                panenDbRef.updateChildren(firebaseUpdates).await()
                Log.d("SyncPanenWorker", "Batch uploaded ${firebaseUpdates.size} items to Firebase.")

                for (panenData in unsyncedDataList) {
                    val updatedLocalData = panenData.copy(
                        isSynced = true,
                        firebaseImageUrl = (firebaseUpdates[panenData.uniqueNo] as? PanenData)?.firebaseImageUrl
                    )
                    panenDao.updatePanen(updatedLocalData)
                    Log.d("SyncPanenWorker", "Panen data updated in local DB: ${updatedLocalData.uniqueNo}")
                }
            }

            Log.d("SyncPanenWorker", "Updated local DB for all synced items.")

            setProgress(Data.Builder().putFloat("progress", 1.0f).build())
            Log.d("SyncPanenWorker", "Sync completed successfully.")
            return@withContext Result.success()
        } catch (e: Exception) {
            Log.e("SyncPanenWorker", "Sync failed with exception.", e)
            return@withContext Result.retry()
        }
    }

    private fun PanenData.toFirebaseMap(): Map<String, Any?> {
        return mapOf(
            "tanggalWaktu" to tanggalWaktu,
            "uniqueNo" to uniqueNo,
            "locationPart1" to locationPart1,
            "locationPart2" to locationPart2,
            "kemandoran" to kemandoran,
            "namaPemanen" to namaPemanen,
            "blok" to blok,
            "noTph" to noTph,
            "totalBuah" to totalBuah,
            "buahN" to buahN,
            "buahA" to buahA,
            "buahOR" to buahOR,
            "buahE" to buahE,
            "buahAB" to buahAB,
            "buahBL" to buahBL,
            "firebaseImageUrl" to firebaseImageUrl
        )
    }
}