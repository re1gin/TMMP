package com.teladanprimaagro.tmpp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.PanenData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val panenDao = AppDatabase.getDatabase(application).panenDao()

    val panenLocations: StateFlow<List<PanenData>> = panenDao.getAllPanen()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}