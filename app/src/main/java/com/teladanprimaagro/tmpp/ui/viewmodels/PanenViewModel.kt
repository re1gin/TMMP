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
                if (isAscending) {
                    list
                } else {
                    list.reversed()
                }
            }
            .combine(_selectedPemanenFilter) { list, pemanenFilter ->
                if (pemanenFilter == "Semua") {
                    list
                } else {
                    list.filter { it.namaPemanen == pemanenFilter }
                }
            }
            .combine(_selectedBlokFilter) { list, blokFilter ->
                if (blokFilter == "Semua") {
                    list
                } else {
                    list.filter { it.blok == blokFilter }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val statistikPerPemanen: StateFlow<Map<String, Int>> = panenDao.getAllPanen()
        .map { list ->
            list.groupBy { it.namaPemanen }
                .mapValues { (_, panenList) -> panenList.sumOf { it.totalBuah } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val statistikPerBlok: StateFlow<Map<String, Int>> = panenDao.getAllPanen()
        .map { list ->
            list.groupBy { it.blok }
                .mapValues { (_, panenList) -> panenList.sumOf { it.totalBuah } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
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


    fun addPanenData(panen: PanenData) {
        viewModelScope.launch {
            panenDao.insertPanen(panen)
        }
    }

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

    fun clearAllPanenData() {
        viewModelScope.launch {
            panenDao.clearAllPanen()
        }
    }

    fun deletePanenDataById(id: Int) {
        viewModelScope.launch {
            panenDao.deletePanenById(id)
        }
    }

    fun deleteSelectedPanenData(ids: List<Int>) {
        viewModelScope.launch {
            panenDao.deleteMultiplePanen(ids)
        }
    }
}