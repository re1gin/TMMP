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
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.data.PengirimanDao
import com.teladanprimaagro.tmpp.data.ScannedItemDao
import com.teladanprimaagro.tmpp.data.ScannedItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
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
    private val scannedItemDao: ScannedItemDao = AppDatabase.getDatabase(application).scannedItemDao() // Tambahkan ini
    private val settingsViewModel: SettingsViewModel = SettingsViewModel(application)
    private val gson = Gson()

    val uniqueNoDisplay = mutableStateOf("Scan NFC")
    val dateTimeDisplay = mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")))
    val totalBuahCalculated = mutableIntStateOf(0)

    val spbNumber = mutableStateOf("")

    // Ubah _scannedItems agar membaca dari database
    private val _scannedItems = scannedItemDao.getAllScannedItems()
        .map { list ->
            // Konversi dari ScannedItemEntity ke ScannedItem
            list.map { ScannedItem(it.uniqueNo, it.tanggal, it.blok, it.totalBuah) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val scannedItems: StateFlow<List<ScannedItem>> = _scannedItems

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
        // Langganan ke perubahan database untuk memperbarui UI
        viewModelScope.launch {
            scannedItems.collect { items ->
                if (items.isNotEmpty()) {
                    uniqueNoDisplay.value = if (items.size == 1) items.first().uniqueNo else "${items.size} Item Discan"
                    dateTimeDisplay.value = items.first().tanggal
                } else {
                    uniqueNoDisplay.value = "Scan NFC"
                    dateTimeDisplay.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))
                }
                totalBuahCalculated.intValue = items.sumOf { it.totalBuah }
            }
        }
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
        viewModelScope.launch {
            // Cek duplikat di database
            val isDuplicate = scannedItemDao.getAllScannedItems().first().any { it.uniqueNo == item.uniqueNo }

            if (isDuplicate) {
                _scanStatus.value = ScanStatus.Duplicate(item.uniqueNo)
                Log.d("PengirimanViewModel", "Scan item rejected: Duplicate uniqueNo -> ${item.uniqueNo}")
            } else {
                // Simpan item ke database
                scannedItemDao.insertScannedItem(
                    ScannedItemEntity(
                        uniqueNo = item.uniqueNo,
                        tanggal = item.tanggal,
                        blok = item.blok,
                        totalBuah = item.totalBuah
                    )
                )
                _scanStatus.value = ScanStatus.Success
                Log.d("PengirimanViewModel", "New item added and saved to DB: ${item.uniqueNo}")
            }
        }
    }

    fun finalizeScannedItemsAsPengiriman(
        namaSupir: String,
        noPolisi: String,
        mandorLoading: String
    ) {
        viewModelScope.launch {
            // Ambil data item yang discan dari database
            val scannedItemsFromDb = scannedItemDao.getAllScannedItems().first()

            if (scannedItemsFromDb.isEmpty()) {
                Log.w("PengirimanViewModel", "Tidak ada item untuk difinalisasi.")
                return@launch
            }

            generateSpbNumber(selectedMandorLoading = mandorLoading)

            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")
            val waktuPengirimanFormatted = currentDateTime.format(formatter)

            val detailScannedItemsJson = gson.toJson(scannedItemsFromDb.map {
                ScannedItem(
                    it.uniqueNo,
                    it.tanggal,
                    it.blok,
                    it.totalBuah
                )
            })

            val firstScannedItem = scannedItemsFromDb.firstOrNull()

            val newPengiriman = PengirimanData(
                uniqueNo = firstScannedItem?.uniqueNo ?: "N/A",
                tanggalNfc = firstScannedItem?.tanggal ?: "N/A",
                blok = firstScannedItem?.blok ?: "N/A",
                totalBuah = scannedItemsFromDb.sumOf { it.totalBuah },
                waktuPengiriman = waktuPengirimanFormatted,
                namaSupir = namaSupir,
                noPolisi = noPolisi,
                spbNumber = spbNumber.value,
                detailScannedItemsJson = detailScannedItemsJson,
                mandorLoading = mandorLoading,
                isUploaded = false
            )
            pengirimanDao.insertPengiriman(newPengiriman)

            // Hapus semua item dari tabel sementara setelah finalisasi
            scannedItemDao.deleteAllScannedItems()

            // Reset state lokal
            uniqueNoDisplay.value = "Scan NFC"
            dateTimeDisplay.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))
            totalBuahCalculated.intValue = 0
            _scanStatus.value = ScanStatus.Finalized

            generateSpbNumber(selectedMandorLoading = "A")
            Log.d("PengirimanViewModel", "Item difinalisasi dan disimpan. Total Pengiriman: ${pengirimanList.value.size}")
        }
    }

    fun clearAllPengirimanData() {
        viewModelScope.launch {
            pengirimanDao.clearAllPengiriman()
            scannedItemDao.deleteAllScannedItems() // Pastikan tabel sementara juga dibersihkan
            uniqueNoDisplay.value = "Scan NFC"
            dateTimeDisplay.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))
            totalBuahCalculated.intValue = 0
            settingsViewModel.setSpbCounter(0)
            generateSpbNumber(selectedMandorLoading = "A")
            _scanStatus.value = ScanStatus.Idle
        }
    }
}