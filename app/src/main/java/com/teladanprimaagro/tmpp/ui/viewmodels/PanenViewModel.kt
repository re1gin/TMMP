package com.teladanprimaagro.tmpp.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.PanenDao
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.util.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

class PanenViewModel(application: Application) : AndroidViewModel(application) {

    private val panenDao: PanenDao = AppDatabase.getDatabase(application).panenDao()
    private val panenDbRef = FirebaseDatabase.getInstance().getReference("panenEntries")
    private val storage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference.child("images")

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

    // ==================== Syncing State ====================
    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _totalItemsToSync = MutableStateFlow(0)
    val totalItemsToSync: StateFlow<Int> = _totalItemsToSync.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val connectivityObserver = ConnectivityObserver(application)
    val isConnected: StateFlow<Boolean> = connectivityObserver.isConnected.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // ==================== Data Flows ====================
    val unsyncedPanenList: StateFlow<List<PanenData>> = panenDao.getUnsyncedPanenDataFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    // ==================== Initialization Block ====================
    init {
        viewModelScope.launch {
            combine(unsyncedPanenList, isConnected) { data, connected ->
                data.isNotEmpty() && connected && !_isSyncing.value
            }.collect { shouldSync ->
                if (shouldSync) {
                    syncDataToServer()
                }
            }
        }
    }

    suspend fun getPanenByIds(ids: List<Int>): List<PanenData> = panenDao.getPanenByIds(ids)

    fun compressImageAndSavePanen(panenData: PanenData, imageUri: Uri?) {
        viewModelScope.launch {
            if (imageUri == null) {
                // Jika tidak ada gambar, langsung simpan data tanpa URI gambar
                savePanenData(panenData)
                return@launch
            }

            try {
                // Kompres gambar
                val compressedUri = compressImage(imageUri)
                Log.d("PanenViewModel", "Gambar berhasil dikompres ke URI: $compressedUri")

                val panenDataWithCompressedImage = panenData.copy(
                    localImageUri = compressedUri.toString(),
                    isSynced = false
                )

                savePanenData(panenDataWithCompressedImage)
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Gagal mengkompres gambar: ${e.message}", e)
                savePanenData(panenData.copy(localImageUri = null))
            }
        }
    }

    /**
     * Menyimpan data panen ke Room.
     */
    private fun savePanenData(panenData: PanenData) {
        viewModelScope.launch {
            try {
                panenDao.insertPanen(panenData)
                Log.d("PanenViewModel", "ROOM: Data panen berhasil dimasukkan secara lokal.")
                if (isConnected.value) {
                    syncDataToServer()
                }
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Gagal menyimpan data ke Room: ${e.message}", e)
            }
        }
    }

    private suspend fun compressImage(uri: Uri): Uri = suspendCoroutine { continuation ->
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                if (bitmap == null) {
                    continuation.resume(uri) // Gagal, kembalikan URI asli
                    return@launch
                }

                val compressedImageFile = File(context.cacheDir, "compressed_image_${UUID.randomUUID()}.jpg")
                val outputStream = FileOutputStream(compressedImageFile)

                // Kompres bitmap menjadi JPEG dengan kualitas 80
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

                outputStream.flush()
                outputStream.close()

                val compressedUri = compressedImageFile.toUri()
                continuation.resume(compressedUri)
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Error saat kompresi gambar: ${e.message}")
                continuation.resume(uri) // Gagal, kembalikan URI asli
            }
        }
    }

    fun syncDataToServer() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val unsyncedDataList = panenDao.getUnsyncedPanenDataFlow().first()
                _totalItemsToSync.value = unsyncedDataList.size
                _syncProgress.value = 0f

                if (unsyncedDataList.isNotEmpty()) {
                    for ((index, panenData) in unsyncedDataList.withIndex()) {
                        try {
                            var firebaseImageUrl: String? = panenData.firebaseImageUrl

                            // LANGKAH PENCEGAHAN DUPLIKASI GAMBAR
                            // Hanya unggah gambar jika ada URI lokal dan belum memiliki URL Firebase.
                            // Ini akan mencegah gambar yang sudah pernah diunggah dikirim lagi.
                            if (panenData.localImageUri != null && panenData.firebaseImageUrl.isNullOrEmpty()) {
                                val imageUri = panenData.localImageUri.toUri()
                                val imageName = UUID.randomUUID().toString()
                                val imageRef = storageRef.child("$imageName.jpg")

                                imageRef.putFile(imageUri).await()
                                firebaseImageUrl = imageRef.downloadUrl.await().toString()
                                Log.d("PanenViewModel", "Gambar untuk No. Unik ${panenData.uniqueNo} disinkronkan. URL: $firebaseImageUrl")
                            }

                            // Langkah 2: Perbarui data di Room dengan URL Firebase dan status sinkron.
                            val updatedData = panenData.copy(
                                firebaseImageUrl = firebaseImageUrl,
                                isSynced = true
                            )
                            panenDao.updatePanen(updatedData)
                            Log.d("PanenViewModel", "ROOM: Data ${updatedData.uniqueNo} diperbarui dengan URL Firebase dan status sinkron.")

                            // Langkah 3: Unggah data yang sudah lengkap ke Firebase Realtime Database.
                            panenDbRef.child(updatedData.uniqueNo).setValue(updatedData).await()
                            Log.d("PanenViewModel", "FIREBASE: Data ${updatedData.uniqueNo} berhasil diunggah.")

                            _syncProgress.value = (index + 1).toFloat() / unsyncedDataList.size.toFloat()

                        } catch (e: Exception) {
                            Log.e("PanenViewModel", "Gagal mengunggah item dengan No. Unik ${panenData.uniqueNo}: ${e.message}", e)
                            // Lanjutkan ke item berikutnya meskipun ada yang gagal
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Gagal menyinkronkan data: ${e.message}", e)
            } finally {
                _isSyncing.value = false
                _syncProgress.value = 0f
                _totalItemsToSync.value = 0
            }
        }
    }

    fun updatePanenData(panen: PanenData) {
        viewModelScope.launch {
            panenDao.updatePanen(panen)
            if (isConnected.value) {
                syncDataToServer()
            }
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
            Log.d("PanenViewModel", "FIREBASE STORAGE: Gambar berhasil dihapus dari URL: $imageUrl")
        } catch (e: Exception) {
            Log.e("PanenViewModel", "FIREBASE STORAGE: Gagal menghapus gambar: ${e.message}", e)
            // Log error tapi jangan hentikan proses karena mungkin gambar sudah tidak ada
            // atau ada masalah koneksi.
        }
    }

    fun clearAllPanenData() {
        viewModelScope.launch {
            try {
                // Ambil semua data panen untuk mendapatkan URL gambar
                val allPanen = panenDao.getAllPanen().firstOrNull() ?: emptyList()

                // Hapus semua data dari Firebase Realtime Database
                panenDbRef.removeValue().await()
                Log.d("PanenViewModel", "FIREBASE: All data successfully removed from Firebase.")

                // Hapus gambar terkait dari Firebase Storage
                allPanen.forEach { panen ->
                    deleteImageFromFirebaseStorage(panen.firebaseImageUrl)
                }

            } catch (e: Exception) {
                Log.e("PanenViewModel", "Failed to delete all data from Firebase: ${e.message}", e)
            } finally {
                // Hapus semua data dari Room
                panenDao.clearAllPanen()
                Log.d("PanenViewModel", "ROOM: All data cleared from local DB.")
            }
        }
    }

    fun deletePanenDataById(id: Int) {
        viewModelScope.launch {
            val panenData = panenDao.getPanenById(id)
            if (panenData != null) {
                try {
                    // Hapus gambar dari Firebase Storage
                    deleteImageFromFirebaseStorage(panenData.firebaseImageUrl)

                    // Hapus data dari Firebase Realtime Database
                    panenDbRef.child(panenData.uniqueNo).removeValue().await()
                    Log.d("PanenViewModel", "FIREBASE: Data with Unique No ${panenData.uniqueNo} deleted from Firebase.")
                } catch (e: Exception) {
                    Log.e("PanenViewModel", "Failed to delete from Firebase: ${e.message}")
                } finally {
                    // Hapus data dari Room
                    panenDao.deletePanenById(id)
                    Log.d("PanenViewModel", "ROOM: Panen data deleted for ID: $id")
                }
            }
        }
    }

    fun deleteSelectedPanenData(ids: List<Int>) {
        viewModelScope.launch {
            val dataToDelete = panenDao.getPanenByIds(ids)
            dataToDelete.forEach { panen ->
                try {
                    // Hapus gambar dari Firebase Storage
                    deleteImageFromFirebaseStorage(panen.firebaseImageUrl)

                    // Hapus data dari Firebase Realtime Database
                    panenDbRef.child(panen.uniqueNo).removeValue().await()
                    Log.d("PanenViewModel", "FIREBASE: Data with Unique No ${panen.uniqueNo} deleted from Firebase.")
                } catch (e: Exception) {
                    Log.e("PanenViewModel", "Failed to delete item ${panen.uniqueNo} from Firebase: ${e.message}")
                }
            }
            // Hapus semua data yang dipilih dari Room
            panenDao.deleteMultiplePanen(ids)
            Log.d("PanenViewModel", "ROOM: Deleted multiple panen data with IDs: $ids")
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