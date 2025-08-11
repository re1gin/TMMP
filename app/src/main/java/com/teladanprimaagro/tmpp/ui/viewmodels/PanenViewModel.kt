package com.teladanprimaagro.tmpp.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.PanenDao
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.util.ConnectivityObserver
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PanenViewModel(application: Application) : AndroidViewModel(application) {

    private val panenDao: PanenDao = AppDatabase.getDatabase(application).panenDao()
    private val panenDbRef = FirebaseDatabase.getInstance().getReference("panenEntries")
    private val storage = FirebaseStorage.getInstance()

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

    // This function is new and is needed for the deletion logic.
    suspend fun getPanenByIds(ids: List<Int>): List<PanenData> = panenDao.getPanenByIds(ids)

    // ==================== Business Logic ====================
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
                            val finalPanenData = panenData.copy(isSynced = true)
                            // Menggunakan uniqueNo sebagai kunci unik di Firebase
                            panenDbRef.child(finalPanenData.uniqueNo).setValue(finalPanenData).await()
                            // Setelah berhasil, update status isSynced di Room
                            panenDao.updatePanen(finalPanenData)
                            _syncProgress.value = (index + 1).toFloat() / unsyncedDataList.size.toFloat()
                        } catch (e: Exception) {
                            Log.e("PanenViewModel", "Failed to upload item with Unique No ${panenData.uniqueNo}: ${e.message}", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Failed to sync data: ${e.message}", e)
            } finally {
                _isSyncing.value = false
                _syncProgress.value = 0f
                _totalItemsToSync.value = 0
            }
        }
    }

    fun addPanenData(panen: PanenData) {
        viewModelScope.launch {
            panenDao.insertPanen(panen)
        }
    }

    fun updatePanenData(panen: PanenData) {
        viewModelScope.launch {
            panenDao.updatePanen(panen)
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

    // --- DELETION FUNCTIONS (UPDATED) ---

    fun clearAllPanenData() {
        viewModelScope.launch {
            try {
                // Hapus semua data dari Firebase
                panenDbRef.removeValue().await()
                Log.d("PanenViewModel", "FIREBASE: All data successfully removed from Firebase.")
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
            try {
                val panenData = panenDao.getPanenById(id)
                if (panenData != null) {
                    // Hapus data dari Firebase menggunakan uniqueNo sebagai kunci
                    panenDbRef.child(panenData.uniqueNo).removeValue().await()
                    Log.d("PanenViewModel", "FIREBASE: Data with Unique No ${panenData.uniqueNo} deleted from Firebase.")
                }
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Failed to delete from Firebase: ${e.message}")
            } finally {
                // Hapus data dari Room
                panenDao.deletePanenById(id)
                Log.d("PanenViewModel", "ROOM: Panen data deleted for ID: $id")
            }
        }
    }

    fun deleteSelectedPanenData(ids: List<Int>) {
        viewModelScope.launch {
            try {
                val dataToDelete = panenDao.getPanenByIds(ids)
                dataToDelete.forEach { panen ->
                    panenDbRef.child(panen.uniqueNo).removeValue().await()
                    Log.d("PanenViewModel", "FIREBASE: Data with Unique No ${panen.uniqueNo} deleted from Firebase.")
                }
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Failed to delete multiple data from Firebase: ${e.message}")
            } finally {
                // Hapus data dari Room
                panenDao.deleteMultiplePanen(ids)
                Log.d("PanenViewModel", "ROOM: Deleted multiple panen data with IDs: $ids")
            }
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