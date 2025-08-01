package com.teladanprimaagro.tmpp.ui.viewmodels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.data.PengirimanDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Month
import java.util.Locale

data class ScannedItem(
    val uniqueNo: String,
    val tanggal: String,
    val blok: String,
    val totalBuah: Int
)

// Definisikan sealed class untuk status scan
sealed class ScanStatus {
    object Idle : ScanStatus()
    object Success : ScanStatus()
    data class Duplicate(val uniqueNo: String) : ScanStatus()
    object Finalized : ScanStatus()
}

@RequiresApi(Build.VERSION_CODES.O)
class PengirimanViewModel(application: Application) : AndroidViewModel(application) {

    private val pengirimanDao: PengirimanDao = AppDatabase.getDatabase(application).pengirimanDao()
    private val settingsViewModel: SettingsViewModel = SettingsViewModel(application)
    private val gson = Gson()

    val uniqueNoDisplay = mutableStateOf("Scan NFC")
    val dateTimeDisplay = mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")))
    val totalBuahCalculated = mutableIntStateOf(0)

    val spbNumber = mutableStateOf("")

    private val _scannedItems = MutableStateFlow<List<ScannedItem>>(emptyList())
    val scannedItems: StateFlow<List<ScannedItem>> = _scannedItems.asStateFlow()

    // State untuk memberikan umpan balik status scan ke UI
    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus: StateFlow<ScanStatus> = _scanStatus.asStateFlow()

    val pengirimanList: StateFlow<List<PengirimanData>> = pengirimanDao.getAllPengiriman()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalDataMasuk: StateFlow<Int> = pengirimanList.map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalSemuaBuah: StateFlow<Int> = pengirimanList.map { list -> list.sumOf { it.totalBuah } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        generateSpbNumber(selectedMandorLoading = "A")
    }

    private fun getRomanMonth(month: Month): String {
        return when (month) {
            Month.JANUARY -> "I"
            Month.FEBRUARY -> "II"
            Month.MARCH -> "III"
            Month.APRIL -> "IV"
            Month.MAY -> "V"
            Month.JUNE -> "VI"
            Month.JULY -> "VII"
            Month.AUGUST -> "VIII"
            Month.SEPTEMBER -> "IX"
            Month.OCTOBER -> "X"
            Month.NOVEMBER -> "XI"
            Month.DECEMBER -> "XII"
        }
    }

    suspend fun getPengirimanById(id: Int): PengirimanData? {
        return pengirimanDao.getPengirimanById(id)
    }

    fun generateSpbNumber(selectedMandorLoading: String) {
        val currentDateTime = LocalDateTime.now()
        val currentMonth = currentDateTime.monthValue
        val currentYear = currentDateTime.year

        var counter = settingsViewModel.getSpbCounter()
        val lastMonth = settingsViewModel.getSpbLastMonth()
        val lastYear = settingsViewModel.getSpbLastYear()

        if (currentMonth != lastMonth || currentYear != lastYear) {
            counter = 0
            settingsViewModel.setSpbLastMonth(currentMonth)
            settingsViewModel.setSpbLastYear(currentYear)
            Log.d("PengirimanViewModel", "SPB Counter reset. New month/year detected.")
        }

        counter++
        settingsViewModel.setSpbCounter(counter)

        val sequenceNumber = String.format(Locale.getDefault(), "%04d", counter)
        val romanMonth = getRomanMonth(currentDateTime.month)

        spbNumber.value = "E005/ESPB/AFD1/$romanMonth/$currentYear/${selectedMandorLoading}$sequenceNumber"
        Log.d("PengirimanViewModel", "Generated SPB: ${spbNumber.value}")
    }

    fun addScannedItem(item: ScannedItem) {
        // Cek apakah item dengan uniqueNo yang sama sudah ada di daftar
        val isDuplicate = _scannedItems.value.any { it.uniqueNo == item.uniqueNo }

        if (isDuplicate) {
            // Jika duplikat, jangan tambahkan dan perbarui status
            _scanStatus.value = ScanStatus.Duplicate(item.uniqueNo)
            Log.d("PengirimanViewModel", "Scan item rejected: Duplicate uniqueNo -> ${item.uniqueNo}")
        } else {
            // Jika bukan duplikat, tambahkan item baru dan perbarui status
            _scannedItems.value = _scannedItems.value + item
            _scanStatus.value = ScanStatus.Success

            // Perbarui tampilan di UI
            if (_scannedItems.value.size == 1) {
                uniqueNoDisplay.value = item.uniqueNo
            } else if (_scannedItems.value.size > 1) {
                uniqueNoDisplay.value = "${_scannedItems.value.size} Item Discan"
            }
            totalBuahCalculated.intValue = _scannedItems.value.sumOf { it.totalBuah }
            dateTimeDisplay.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))

            Log.d("PengirimanViewModel", "New item added: ${item.uniqueNo}")
        }
    }

    fun removeScannedItem(item: ScannedItem) {
        _scannedItems.value = _scannedItems.value.filter { it != item }
        totalBuahCalculated.intValue = _scannedItems.value.sumOf { it.totalBuah }
        if (_scannedItems.value.isEmpty()) {
            uniqueNoDisplay.value = "Scan NFC"
        } else if (_scannedItems.value.size == 1) {
            uniqueNoDisplay.value = _scannedItems.value.first().uniqueNo
        }
    }

    fun finalizeScannedItemsAsPengiriman(
        namaSupir: String,
        noPolisi: String,
        mandorLoading: String
    ) {
        if (_scannedItems.value.isEmpty()) {
            Log.w("PengirimanViewModel", "Tidak ada item untuk difinalisasi.")
            return
        }

        generateSpbNumber(selectedMandorLoading = mandorLoading)

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")
        val waktuPengirimanFormatted = currentDateTime.format(formatter)

        val detailScannedItemsJson = gson.toJson(_scannedItems.value)

        viewModelScope.launch {
            val firstScannedItem = _scannedItems.value.firstOrNull()

            val newPengiriman = PengirimanData(
                uniqueNo = firstScannedItem?.uniqueNo ?: "N/A",
                tanggalNfc = firstScannedItem?.tanggal ?: "N/A",
                blok = firstScannedItem?.blok ?: "N/A",
                totalBuah = totalBuahCalculated.intValue,
                waktuPengiriman = waktuPengirimanFormatted,
                namaSupir = namaSupir,
                noPolisi = noPolisi,
                spbNumber = spbNumber.value,
                detailScannedItemsJson = detailScannedItemsJson,
                mandorLoading = mandorLoading,
                isUploaded = false
            )
            pengirimanDao.insertPengiriman(newPengiriman)

            // Reset state setelah finalisasi
            _scannedItems.value = emptyList()
            uniqueNoDisplay.value = "Scan NFC"
            dateTimeDisplay.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))
            totalBuahCalculated.intValue = 0
            _scanStatus.value = ScanStatus.Finalized // Set status finalisasi

            generateSpbNumber(selectedMandorLoading = "A")
            Log.d("PengirimanViewModel", "Item difinalisasi dan disimpan. Total Pengiriman: ${pengirimanList.value.size}")
        }
    }

    fun getDetailScannedItemsFromJson(jsonString: String): List<ScannedItem> {
        val type = object : TypeToken<List<ScannedItem>>() {}.type
        return gson.fromJson(jsonString, type) ?: emptyList()
    }

    fun clearAllPengirimanData() {
        viewModelScope.launch {
            pengirimanDao.clearAllPengiriman()
            _scannedItems.value = emptyList()
            uniqueNoDisplay.value = "Scan NFC"
            dateTimeDisplay.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))
            totalBuahCalculated.intValue = 0
            settingsViewModel.setSpbCounter(0)
            generateSpbNumber(selectedMandorLoading = "A")
            _scanStatus.value = ScanStatus.Idle // Reset status
        }
    }

    fun deletePengirimanDataById(id: Int) {
        viewModelScope.launch {
            pengirimanDao.deletePengirimanById(id)
        }
    }

    fun updatePengirimanData(pengiriman: PengirimanData) {
        viewModelScope.launch {
            pengirimanDao.updatePengiriman(pengiriman)
        }
    }
}