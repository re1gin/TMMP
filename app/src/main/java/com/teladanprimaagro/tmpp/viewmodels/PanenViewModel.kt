package com.teladanprimaagro.tmpp.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.PanenDao
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.workers.SyncPanenWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("StaticFieldLeak")
class PanenViewModel(application: Application) : AndroidViewModel(application) {

    private val panenDao: PanenDao = AppDatabase.getDatabase(application).panenDao()
    private val panenDbRef = FirebaseDatabase.getInstance("https://ineka-database.firebaseio.com/").getReference("panenEntries")
    private val storage = FirebaseStorage.getInstance()
    private val context = application.applicationContext

    // ==================== UI State and Filters ====================
    private val _panenDataToEdit = MutableStateFlow<PanenData?>(null)
    val panenDataToEdit: StateFlow<PanenData?> = _panenDataToEdit.asStateFlow()

    private val _sortBy = MutableStateFlow("Nama")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortOrderAscending = MutableStateFlow(true)
    val sortOrderAscending: StateFlow<Boolean> = _sortOrderAscending.asStateFlow()

    private val _selectedPemanenFilter = MutableStateFlow("Semua")
    val selectedPemanenFilter: StateFlow<String> = _selectedPemanenFilter.asStateFlow()

    private val _selectedBlokFilter = MutableStateFlow("Semua")
    val selectedBlokFilter: StateFlow<String> = _selectedBlokFilter.asStateFlow()

    // ==================== Data Flows ====================
    val panenList: StateFlow<List<PanenData>> =
        panenDao.getAllPanen()
            .combine(_sortBy) { list, sortBy -> sortPanenList(list, sortBy) }
            .combine(_sortOrderAscending) { list, isAscending -> sortPanenListByOrder(list, isAscending) }
            .combine(_selectedPemanenFilter) { list, pemanenFilter -> filterByPemanen(list, pemanenFilter) }
            .combine(_selectedBlokFilter) { list, blokFilter -> filterByBlok(list, blokFilter) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDataMasuk: StateFlow<Int> = panenList.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSemuaBuah: StateFlow<Int> = panenList.map { list -> list.sumOf { it.totalBuah } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val statistikPerPemanen: StateFlow<Map<String, Int>> = panenDao.getAllPanen()
        .map { list -> list.groupBy { it.namaPemanen }.mapValues { (_, panenList) -> panenList.sumOf { it.totalBuah } } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val statistikPerBlok: StateFlow<Map<String, Int>> = panenDao.getAllPanen()
        .map { list -> list.groupBy { it.blok }.mapValues { (_, panenList) -> panenList.sumOf { it.totalBuah } } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val statistikJenisBuahPerPemanen: StateFlow<Map<String, Map<String, Int>>> = panenDao.getAllPanen()
        .map { panenList ->
            panenList.groupBy { it.namaPemanen }.mapValues { (_, dataList) ->
                mapOf(
                    "Buah N" to dataList.sumOf { it.buahN },
                    "Buah A" to dataList.sumOf { it.buahA },
                    "Buah OR" to dataList.sumOf { it.buahOR },
                    "Buah E" to dataList.sumOf { it.buahE },
                    "Buah AB" to dataList.sumOf { it.buahAB },
                    "Buah BL" to dataList.sumOf { it.buahBL }
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalJenisBuah: StateFlow<Map<String, Int>> = panenDao.getAllPanen()
        .map { panenList ->
            mapOf(
                "Buah N" to panenList.sumOf { it.buahN },
                "Buah A" to panenList.sumOf { it.buahA },
                "Buah OR" to panenList.sumOf { it.buahOR },
                "Buah E" to panenList.sumOf { it.buahE },
                "Buah AB" to panenList.sumOf { it.buahAB },
                "Buah BL" to panenList.sumOf { it.buahBL }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // ==================== WorkManager Trigger ====================
    private fun startSyncWorker() {
        Log.d("PanenViewModel", "Enqueuing SyncPanenWorker...")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncPanenWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(getApplication()).enqueue(syncWorkRequest)
    }

    // ==================== CRUD Operations ====================
    fun compressImageAndSavePanen(panenData: PanenData, imageUri: Uri?) {
        viewModelScope.launch {
            if (imageUri == null) {
                savePanenData(panenData)
                return@launch
            }
            try {
                val compressedUri = compressImage(imageUri)
                val panenDataWithCompressedImage = panenData.copy(
                    localImageUri = compressedUri.toString(),
                    isSynced = false
                )
                savePanenData(panenDataWithCompressedImage)
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Error during image compression", e)
                savePanenData(panenData.copy(localImageUri = null))
            }
        }
    }

    private fun savePanenData(panenData: PanenData) {
        viewModelScope.launch {
            try {
                panenDao.insertPanen(panenData)
                startSyncWorker() // Memanggil WorkManager setelah data disimpan
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Error saving panen data to local DB", e)
            }
        }
    }

    private suspend fun compressImage(uri: Uri): Uri = suspendCoroutine { continuation ->
        viewModelScope.launch(Dispatchers.IO) { // Gunakan Dispatchers.IO untuk operasi file
            try {
                val originalBitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                if (originalBitmap == null) {
                    continuation.resume(uri)
                    return@launch
                }

                // Resizing gambar
                val resizedBitmap = resizeBitmap(originalBitmap, 1200, 860)

                val compressedImageFile = File(context.cacheDir, "compressed_image_${UUID.randomUUID()}.jpg")

                // Logika kompresi adaptif
                var quality = 90
                var finalUri: Uri? = null

                while (quality >= 50) {
                    val outputStream = FileOutputStream(compressedImageFile)
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    outputStream.flush()
                    outputStream.close()

                    val fileSizeInKb = compressedImageFile.length() / 1024
                    Log.d("CompressImage", "Quality: $quality, Size: ${fileSizeInKb}KB")

                    if (fileSizeInKb <= 100) { // Target ukuran file 200KB
                        finalUri = compressedImageFile.toUri()
                        break
                    }
                    quality -= 5 // Turunkan kualitas 5%
                }

                if (finalUri != null) {
                    continuation.resume(finalUri)
                } else {
                    // Jika tidak tercapai, gunakan kualitas terendah yang dicoba
                    val outputStream = FileOutputStream(compressedImageFile)
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    continuation.resume(compressedImageFile.toUri())
                }

                originalBitmap.recycle()
                if (originalBitmap != resizedBitmap) resizedBitmap.recycle()

            } catch (e: Exception) {
                Log.e("PanenViewModel", "Error compressing image: ${e.message}", e)
                continuation.resume(uri)
            }
        }
    }

    // Pisahkan fungsi resizing agar kode lebih bersih
    private fun resizeBitmap(originalBitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalWidth = originalBitmap.width
        val originalHeight = originalBitmap.height
        val scaleFactor = minOf(
            targetWidth.toFloat() / originalWidth.toFloat(),
            targetHeight.toFloat() / originalHeight.toFloat()
        )
        return if (scaleFactor < 1) {
            val matrix = Matrix().apply { postScale(scaleFactor, scaleFactor) }
            Bitmap.createBitmap(originalBitmap, 0, 0, originalWidth, originalHeight, matrix, true)
        } else {
            originalBitmap
        }
    }

    fun updatePanenData(panen: PanenData) {
        viewModelScope.launch {
            panenDao.updatePanen(panen)
            startSyncWorker() // Memanggil WorkManager setelah data diperbarui
        }
    }

    fun loadPanenDataById(id: Int) {
        viewModelScope.launch {
            _panenDataToEdit.value = panenDao.getPanenById(id)
        }
    }

    fun clearPanenDataToEdit() {
        _panenDataToEdit.value = null
    }

    private suspend fun deleteImageFromFirebaseStorage(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            return
        }
        try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
        } catch (e: Exception) {
            Log.e("PanenViewModel", "Error deleting image from Firebase Storage", e)
        }
    }

    fun clearAllPanenData() {
        viewModelScope.launch {
            try {
                val allPanen = panenDao.getAllPanen().firstOrNull() ?: emptyList()
                panenDbRef.removeValue().await()
                allPanen.forEach { panen ->
                    deleteImageFromFirebaseStorage(panen.firebaseImageUrl)
                }
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Error clearing all panen data", e)
            } finally {
                panenDao.clearAllPanen()
            }
        }
    }

    fun deleteSelectedPanenData(ids: List<Int>) {
        viewModelScope.launch {
            val dataToDelete = panenDao.getPanenByIds(ids)
            dataToDelete.forEach { panen ->
                try {
                    panenDbRef.child(panen.uniqueNo).removeValue().await()
                    deleteImageFromFirebaseStorage(panen.firebaseImageUrl)
                } catch (e: Exception) {
                    Log.e("PanenViewModel", "Error deleting selected panen data", e)
                }
            }
            panenDao.deleteMultiplePanen(ids)
        }
    }

    // --- Filter & Sort Functions ---
    fun setSortBy(criteria: String) {
        _sortBy.value = criteria
    }

    fun toggleSortOrder() {
        _sortOrderAscending.value = !_sortOrderAscending.value
    }

    fun setPemanenFilter(pemanen: String) {
        _selectedPemanenFilter.value = pemanen
    }

    fun setBlokFilter(blok: String) {
        _selectedBlokFilter.value = blok
    }

    fun clearFilters() {
        _selectedPemanenFilter.value = "Semua"
        _selectedBlokFilter.value = "Semua"
    }

    private fun sortPanenList(list: List<PanenData>, sortBy: String): List<PanenData> {
        return when (sortBy) {
            "Nama" -> list.sortedBy { it.namaPemanen }
            "Blok" -> list.sortedBy { it.blok }
            else -> list
        }
    }

    private fun sortPanenListByOrder(list: List<PanenData>, isAscending: Boolean): List<PanenData> {
        val sortedList = if (isAscending) {
            when (_sortBy.value) {
                "Nama" -> list.sortedBy { it.namaPemanen }
                "Blok" -> list.sortedBy { it.blok }
                else -> list
            }
        } else {
            when (_sortBy.value) {
                "Nama" -> list.sortedByDescending { it.namaPemanen }
                "Blok" -> list.sortedByDescending { it.blok }
                else -> list
            }
        }
        return sortedList
    }

    private fun filterByPemanen(list: List<PanenData>, pemanenFilter: String): List<PanenData> {
        return if (pemanenFilter == "Semua") list else list.filter { it.namaPemanen == pemanenFilter }
    }

    private fun filterByBlok(list: List<PanenData>, blokFilter: String): List<PanenData> {
        return if (blokFilter == "Semua") list else list.filter { it.blok == blokFilter }
    }
}