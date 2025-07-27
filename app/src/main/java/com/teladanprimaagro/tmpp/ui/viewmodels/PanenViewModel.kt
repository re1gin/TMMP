package com.teladanprimaagro.tmpp.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.teladanprimaagro.tmpp.ui.data.PanenData

class PanenViewModel : ViewModel() {
    val panenList = mutableStateListOf<PanenData>()

    fun addPanenData(data: PanenData) {
        panenList.add(data)
    }

    // Fungsi untuk mengosongkan data (misalnya, reset harian)
    fun clearPanenData() {
        panenList.clear()
    }

}