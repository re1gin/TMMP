// File: com/teladanprimaagro/tmpp/ui/viewmodels/ReportViewModel.kt
package com.teladanprimaagro.tmpp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.BlokSummary
import com.teladanprimaagro.tmpp.data.MainStats
import com.teladanprimaagro.tmpp.data.SupirSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val pengirimanDao = AppDatabase.getDatabase(application).pengirimanDao()
    private val allPengirimanData = pengirimanDao.getAllPengiriman()

    val mainStats: StateFlow<MainStats> = allPengirimanData.map { list ->
        val totalBuah = list.sumOf { it.totalBuah }
        val totalScannedData = list.size
        val finalizedData = list.size
        MainStats(totalBuah, totalScannedData, finalizedData)
    }.stateIn(viewModelScope, SharingStarted.Lazily, MainStats(0, 0, 0))

    val blokSummary: StateFlow<List<BlokSummary>> = allPengirimanData.map { list ->
        list.groupBy { it.blok }
            .map { (blok, items) ->
                BlokSummary(blok, items.sumOf { it.totalBuah })
            }
            .sortedBy { it.blok }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val supirSummary: StateFlow<List<SupirSummary>> = allPengirimanData.map { list ->
        list.groupBy { it.namaSupir }
            .map { (namaSupir, items) ->
                SupirSummary(namaSupir, items.sumOf { it.totalBuah })
            }
            .sortedByDescending { it.totalBuah }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}