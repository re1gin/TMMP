package com.teladanprimaagro.tmpp.ui.viewmodels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.FinalizedUniqueNoEntity
import com.teladanprimaagro.tmpp.data.PengirimanDao
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.data.ScannedItemDao
import com.teladanprimaagro.tmpp.data.ScannedItemEntity
import com.teladanprimaagro.tmpp.util.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Locale

// Data Classes
data class ScannedItem(
    val uniqueNo: String,
    val tanggal: String,
    val blok: String,
    val totalBuah: Int
)

data class BlokSummary(
    val blok: String,
    val totalBuah: Int
)

data class SimplePengirimanData(
    val spbNumber: String,
    val waktuPengiriman: String,
    val namaSupir: String,
    val noPolisi: String,
    val mandorLoading: String,
    val totalBuah: Int,
    val ringkasanPerBlok: Map<String, Int>
)

data class SupirSummary(
    val namaSupir: String,
    val totalBuah: Int
)

sealed class ScanStatus {
    object Idle : ScanStatus()
    data class Success(val uniqueNo: String) : ScanStatus()
    data class Duplicate(val uniqueNo: String) : ScanStatus()
    object Finalized : ScanStatus()
}

@RequiresApi(Build.VERSION_CODES.O)
class PengirimanViewModel(application: Application) : AndroidViewModel(application) {

    // --- Repositories & Utilities ---
    private val pengirimanDao: PengirimanDao = AppDatabase.getDatabase(application).pengirimanDao()
    private val scannedItemDao: ScannedItemDao = AppDatabase.getDatabase(application).scannedItemDao()
    private val settingsViewModel: SettingsViewModel = SettingsViewModel(application)
    private val connectivityObserver = ConnectivityObserver(application)
    private val gson = Gson()
    private val pengirimanDbRef = FirebaseDatabase.getInstance().getReference("pengirimanEntries")

    // --- State untuk UI (Composable) ---
    val uniqueNoDisplay = mutableStateOf("Scan NFC")
    val dateTimeDisplay = mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")))
    val totalBuahCalculated = mutableIntStateOf(0)
    val spbNumber = mutableStateOf("")

    // --- Observers & Data Flow ---
    val scannedItems: StateFlow<List<ScannedItem>> = scannedItemDao.getAllScannedItems()
        .map { list -> list.map { ScannedItem(it.uniqueNo, it.tanggal, it.blok, it.totalBuah) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pengirimanList: StateFlow<List<PengirimanData>> = pengirimanDao.getAllPengiriman()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSuccessfulScans: StateFlow<Int> = pengirimanDao.getTotalScanCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDataMasuk: StateFlow<Int> = pengirimanList.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSemuaBuah: StateFlow<Int> = pengirimanList.map { list -> list.sumOf { it.totalBuah } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val blokSummary: StateFlow<List<BlokSummary>> = pengirimanList.map { list ->
        list.groupBy { it.blok }
            .map { (blok, items) -> BlokSummary(blok, items.sumOf { it.totalBuah }) }
            .sortedBy { it.blok }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val supirSummary: StateFlow<List<SupirSummary>> = pengirimanList.map { list ->
        list.groupBy { it.namaSupir }
            .map { (namaSupir, items) -> SupirSummary(namaSupir, items.sumOf { it.totalBuah }) }
            .sortedByDescending { it.totalBuah }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val unuploadedPengirimanList: StateFlow<List<PengirimanData>> = pengirimanDao.getUnuploadedPengirimanDataFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State untuk Sinkronisasi & Status Scan ---
    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus: StateFlow<ScanStatus> = _scanStatus.asStateFlow()

    private val allFinalizedUniqueNos = pengirimanDao.getAllFinalizedUniqueNos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isConnected: StateFlow<Boolean> = connectivityObserver.isConnected.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _totalItemsToSync = MutableStateFlow(0)
    val totalItemsToSync: StateFlow<Int> = _totalItemsToSync.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        Log.d("PengirimanViewModel", "INIT: PengirimanViewModel created.")
        viewModelScope.launch {
            val mandor = settingsViewModel.selectedMandorLoading.first()
            generateSpbNumber(mandor)
        }

        viewModelScope.launch {
            scannedItems.collect { items ->
                Log.d("PengirimanViewModel", "Scanned items loaded: ${items.size} items")
                if (items.isNotEmpty()) {
                    val firstItem = items.first()
                    uniqueNoDisplay.value = if (items.size == 1) firstItem.uniqueNo else "${items.size} Item Discan"
                    dateTimeDisplay.value = firstItem.tanggal
                } else {
                    uniqueNoDisplay.value = "Scan NFC"
                    dateTimeDisplay.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))
                }
                totalBuahCalculated.intValue = items.sumOf { it.totalBuah }
            }
        }

        viewModelScope.launch {
            combine(unuploadedPengirimanList, isConnected) { data, connected ->
                data.isNotEmpty() && connected && !_isSyncing.value
            }.collect { shouldSync ->
                if (shouldSync) {
                    Log.d("PengirimanViewModel", "Triggering automatic sync.")
                    syncDataToServer()
                }
            }
        }
    }

    // --- Business Logic Functions ---
    private fun getRomanMonth(month: Month): String = when (month) {
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

    suspend fun getPengirimanById(id: Int): PengirimanData? = pengirimanDao.getPengirimanById(id)
    suspend fun getPengirimanByIds(ids: List<Int>): List<PengirimanData> = pengirimanDao.getPengirimanByIds(ids)


    fun generateSpbNumber(selectedMandorLoading: String) {
        val currentDateTime = LocalDateTime.now()
        val currentMonth = currentDateTime.monthValue
        val currentYear = currentDateTime.year

        var counter = settingsViewModel.getSpbCounterForMandor(selectedMandorLoading)
        val lastMonth = settingsViewModel.getSpbLastMonth()
        val lastYear = settingsViewModel.getSpbLastYear()

        if (currentMonth != lastMonth || currentYear != lastYear) {
            settingsViewModel.resetAllSpbCounters()
            counter = 0
            settingsViewModel.setSpbLastMonth(currentMonth)
            settingsViewModel.setSpbLastYear(currentYear)
            Log.d("PengirimanViewModel", "SPB Counters reset. New month/year detected.")
        }

        counter++
        settingsViewModel.setSpbCounterForMandor(selectedMandorLoading, counter)

        val spbFormat = settingsViewModel.getSpbFormat()
        val afdCode = settingsViewModel.getAfdCode()
        val sequenceNumber = String.format(Locale.getDefault(), "%04d", counter)
        val romanMonth = getRomanMonth(currentDateTime.month)

        spbNumber.value = "$spbFormat/$afdCode/$romanMonth/$currentYear/${selectedMandorLoading}$sequenceNumber"
        Log.d("PengirimanViewModel", "Generated SPB: ${spbNumber.value}")
    }

    fun addScannedItem(item: ScannedItem) {
        viewModelScope.launch {
            _scanStatus.value = ScanStatus.Idle
            Log.d("PengirimanViewModel", "ADD_ITEM: Starting check for uniqueNo -> ${item.uniqueNo}")

            val isDuplicateInSession = scannedItems.first().any { it.uniqueNo == item.uniqueNo }
            val isAlreadyFinalized = allFinalizedUniqueNos.first().contains(item.uniqueNo)

            if (isDuplicateInSession || isAlreadyFinalized) {
                _scanStatus.value = ScanStatus.Duplicate(item.uniqueNo)
                Log.w("PengirimanViewModel", "ADD_ITEM: Scan item rejected. Duplicate found.")
            } else {
                scannedItemDao.insertScannedItem(
                    ScannedItemEntity(
                        uniqueNo = item.uniqueNo,
                        tanggal = item.tanggal,
                        blok = item.blok,
                        totalBuah = item.totalBuah
                    )
                )
                _scanStatus.value = ScanStatus.Success(item.uniqueNo)
                Log.d("PengirimanViewModel", "ADD_ITEM: New item added and saved to DB: ${item.uniqueNo}")
            }
        }
    }

    fun finalizeScannedItemsAsPengiriman(namaSupir: String, noPolisi: String) {
        viewModelScope.launch {
            val scannedItemsFromDb = scannedItemDao.getAllScannedItems().first()
            if (scannedItemsFromDb.isEmpty()) {
                Log.w("PengirimanViewModel", "Tidak ada item untuk difinalisasi.")
                return@launch
            }

            val mandorLoading = settingsViewModel.selectedMandorLoading.first()
            generateSpbNumber(mandorLoading)

            val waktuPengirimanFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))
            val detailScannedItemsJson = gson.toJson(scannedItemsFromDb.map { ScannedItem(it.uniqueNo, it.tanggal, it.blok, it.totalBuah) })
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
            scannedItemsFromDb.forEach { item ->
                pengirimanDao.insertFinalizedUniqueNo(FinalizedUniqueNoEntity(uniqueNo = item.uniqueNo))
            }
            scannedItemDao.deleteAllScannedItems()

            Log.d("PengirimanViewModel", "FINALIZE: Finalization complete. Temporary items deleted.")
            _scanStatus.value = ScanStatus.Finalized
        }
    }

    fun syncDataToServer() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val unuploadedDataList = unuploadedPengirimanList.value
                _totalItemsToSync.value = unuploadedDataList.size
                _syncProgress.value = 0f

                if (unuploadedDataList.isNotEmpty()) {
                    for ((index, pengirimanData) in unuploadedDataList.withIndex()) {
                        try {
                            val simplePengirimanData = mapToSimplePengirimanData(pengirimanData)
                            val finalPengirimanData = pengirimanData.copy(isUploaded = true)

                            // Gunakan spbNumber langsung sebagai kunci
                            val firebaseKey = finalPengirimanData.spbNumber.replace('/', '-')
                            pengirimanDbRef.child(firebaseKey).setValue(simplePengirimanData).await()

                            pengirimanDao.updatePengiriman(finalPengirimanData)

                            _syncProgress.value = (index + 1).toFloat() / unuploadedDataList.size.toFloat()

                        } catch (e: Exception) {
                            Log.e("PengirimanViewModel", "Failed to upload item with SPB ${pengirimanData.spbNumber}: ${e.message}", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PengirimanViewModel", "Failed to sync data: ${e.message}", e)
            } finally {
                _isSyncing.value = false
                _syncProgress.value = 0f
                _totalItemsToSync.value = 0
            }
        }
    }

    private fun mapToSimplePengirimanData(pengirimanData: PengirimanData): SimplePengirimanData {
        val scannedItemsType = object : TypeToken<List<ScannedItem>>() {}.type
        val rawDetailScannedItems: List<ScannedItem> = gson.fromJson(pengirimanData.detailScannedItemsJson, scannedItemsType) ?: emptyList()

        val ringkasanPerBlok = rawDetailScannedItems
            .groupBy { it.blok }
            .mapValues { (_, items) -> items.sumOf { it.totalBuah } }

        return SimplePengirimanData(
            spbNumber = pengirimanData.spbNumber,
            waktuPengiriman = pengirimanData.waktuPengiriman,
            namaSupir = pengirimanData.namaSupir,
            noPolisi = pengirimanData.noPolisi,
            mandorLoading = pengirimanData.mandorLoading,
            totalBuah = pengirimanData.totalBuah,
            ringkasanPerBlok = ringkasanPerBlok
        )
    }

    // --- Fungsi yang Disempurnakan untuk Menghapus Data ---

    fun clearAllPengirimanData() {
        viewModelScope.launch {
            try {
                // Hapus semua data dari Firebase
                pengirimanDbRef.removeValue().await()
                Log.d("PengirimanViewModel", "FIREBASE: All data successfully removed from Firebase.")
            } catch (e: Exception) {
                Log.e("PengirimanViewModel", "Failed to delete all data from Firebase: ${e.message}", e)
            } finally {
                // Hapus semua data dari Room
                pengirimanDao.clearAllPengiriman()
                scannedItemDao.deleteAllScannedItems()
                pengirimanDao.clearAllFinalizedUniqueNos()
                Log.d("PengirimanViewModel", "ROOM: All data cleared from local DB.")
                resetUiState()
            }
        }
    }

    fun deletePengirimanDataById(id: Int) {
        viewModelScope.launch {
            try {
                // Ambil data dari Room sebelum dihapus untuk mendapatkan spbNumber
                val pengirimanData = pengirimanDao.getPengirimanById(id)
                if (pengirimanData != null) {
                    val firebaseKey = pengirimanData.spbNumber.replace('/', '-')
                    // Hapus data dari Firebase
                    pengirimanDbRef.child(firebaseKey).removeValue().await()
                    Log.d("PengirimanViewModel", "FIREBASE: Data with SPB ${pengirimanData.spbNumber} deleted from Firebase path: $firebaseKey")
                }
            } catch (e: Exception) {
                Log.e("PengirimanViewModel", "Failed to delete from Firebase: ${e.message}")
            } finally {
                // Hapus data dari Room
                pengirimanDao.deletePengirimanById(id)
                Log.d("PengirimanViewModel", "ROOM: Pengiriman data deleted for ID: $id")
            }
        }
    }

    fun deleteSelectedPengirimanData(ids: List<Int>) {
        viewModelScope.launch {
            try {
                val dataToDelete = pengirimanDao.getPengirimanByIds(ids)

                for (data in dataToDelete) {
                    val firebaseKey = data.spbNumber.replace('/', '-')
                    pengirimanDbRef.child(firebaseKey).removeValue().await()
                    Log.d("PengirimanViewModel", "FIREBASE: Data with SPB ${data.spbNumber} deleted from Firebase path: $firebaseKey")
                }
            } catch (e: Exception) {
                Log.e("PengirimanViewModel", "Failed to delete multiple data from Firebase: ${e.message}")
            } finally {
                // Hapus data dari Room
                pengirimanDao.deleteMultiplePengiriman(ids)
                Log.d("PengirimanViewModel", "ROOM: Deleted multiple pengiriman data with IDs: $ids")
            }
        }
    }

    fun resetScanStatus() {
        _scanStatus.value = ScanStatus.Idle
    }

    private fun resetUiState() {
        uniqueNoDisplay.value = "Scan NFC"
        dateTimeDisplay.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"))
        totalBuahCalculated.intValue = 0
        viewModelScope.launch {
            val mandor = settingsViewModel.selectedMandorLoading.first()
            generateSpbNumber(mandor)
        }
        _scanStatus.value = ScanStatus.Idle
    }
}