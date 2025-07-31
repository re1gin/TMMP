package com.teladanprimaagro.tmpp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.data.PanenDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PanenViewModel(application: Application) : AndroidViewModel(application) {

    private val panenDao: PanenDao = AppDatabase.getDatabase(application).panenDao()
    // HAPUS: private val gson = Gson() // TIDAK DIPERLUKAN

    // --- Sortir States ---
    private val _sortBy = MutableStateFlow("Nama") // Default sort by Nama
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortOrderAscending = MutableStateFlow(true) // Default Ascending (true for Asc, false for Desc)
    val sortOrderAscending: StateFlow<Boolean> = _sortOrderAscending.asStateFlow()
    // --- End Sortir States ---

    // --- Filtering States ---
    private val _selectedPemanenFilter = MutableStateFlow("Semua Pemanen") // Default: tampilkan semua
    val selectedPemanenFilter: StateFlow<String> = _selectedPemanenFilter.asStateFlow()

    private val _selectedBlokFilter = MutableStateFlow("Semua Blok") // Default: tampilkan semua
    val selectedBlokFilter: StateFlow<String> = _selectedBlokFilter.asStateFlow()
    // --- End Filtering States ---

    val panenList: StateFlow<List<PanenData>> =
        panenDao.getAllPanen()
            .combine(_sortBy) { list, sortBy ->
                when (sortBy) {
                    "Nama" -> list.sortedBy { it.namaPemanen }
                    "Blok" -> list.sortedBy { it.blok }
                    else -> list
                }
            }
            .combine(_sortOrderAscending) { list, isAscending ->
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
                sortedList
            }
            // --- TERAPKAN FILTER SETELAH SORTIR ---
            .combine(_selectedPemanenFilter) { list, pemanenFilter ->
                if (pemanenFilter == "Semua Pemanen") {
                    list
                } else {
                    list.filter { it.namaPemanen == pemanenFilter }
                }
            }
            .combine(_selectedBlokFilter) { list, blokFilter ->
                if (blokFilter == "Semua Blok") {
                    list
                } else {
                    list.filter { it.blok == blokFilter }
                }
            }
            // --- AKHIR FILTER ---
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val totalDataMasuk: StateFlow<Int> = panenList.map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalSemuaBuah: StateFlow<Int> = panenList.map { list -> list.sumOf { it.totalBuah } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun addPanenData(
        tanggalWaktu: String,
        uniqueNo: String,
        locationPart1: String,
        locationPart2: String,
        kemandoran: String,
        namaPemanen: String,
        blok: String,
        noTph: String,
        totalBuah: Int,
        buahN: Int,
        buahA: Int,
        buahOR: Int,
        buahE: Int,
        buahAB: Int,
        buahBL: Int,
        imageUri: String?
    ) {
        viewModelScope.launch {
            val newPanen = PanenData(
                tanggalWaktu = tanggalWaktu,
                uniqueNo = uniqueNo,
                locationPart1 = locationPart1,
                locationPart2 = locationPart2,
                kemandoran = kemandoran,
                namaPemanen = namaPemanen,
                blok = blok,
                noTph = noTph,
                totalBuah = totalBuah,
                buahN = buahN,
                buahA = buahA,
                buahOR = buahOR,
                buahE = buahE,
                buahAB = buahAB,
                buahBL = buahBL,
                imageUri = imageUri
            )
            panenDao.insertPanen(newPanen)
        }
    }

    fun setSortBy(criteria: String) {
        _sortBy.value = criteria
    }

    fun toggleSortOrder() {
        _sortOrderAscending.value = !_sortOrderAscending.value
    }

    // --- Fungsi untuk mengatur filter ---
    fun setPemanenFilter(pemanen: String) {
        _selectedPemanenFilter.value = pemanen
    }

    fun setBlokFilter(blok: String) {
        _selectedBlokFilter.value = blok
    }

    fun clearFilters() {
        _selectedPemanenFilter.value = "Semua Pemanen"
        _selectedBlokFilter.value = "Semua Blok"
    }
    // --- Akhir Fungsi Filter ---

    fun clearAllPanenData() {
        viewModelScope.launch {
            panenDao.clearAllPanen()
        }
    }

    fun updatePanenData(panen: PanenData) {
        viewModelScope.launch {
            panenDao.updatePanen(panen)
        }
    }

    fun deletePanenDataById(id: Int) {
        viewModelScope.launch {
            panenDao.deletePanenById(id)
        }
    }
}