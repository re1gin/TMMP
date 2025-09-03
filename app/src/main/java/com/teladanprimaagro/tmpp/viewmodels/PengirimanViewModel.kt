package com.teladanprimaagro.tmpp.viewmodels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.FinalizedUniqueNoEntity
import com.teladanprimaagro.tmpp.data.PengirimanDao
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.data.ScannedItemDao
import com.teladanprimaagro.tmpp.data.ScannedItemEntity
import com.teladanprimaagro.tmpp.util.ConnectivityObserver
import com.teladanprimaagro.tmpp.workers.SyncPengirimanWorker
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

// Data Classes (tetap sama)
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
    private val pengirimanDbRef = FirebaseDatabase.getInstance("https://ineka-database.firebaseio.com/").getReference("pengirimanEntries")
    private val finalizedUniqueNoDbRef = FirebaseDatabase.getInstance("https://ineka-database.firebaseio.com/").getReference("finalizedUniqueNos")

    // --- State untuk UI (Composable) ---
    val uniqueNoDisplay = mutableStateOf("Scan NFC")
    val dateTimeDisplay = mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")))
    val totalBuahCalculated = mutableIntStateOf(0)

    val spbNumber = mutableStateOf("")

    // --- Observers & Data Flow ---

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

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

    private val unuploadedFinalizedUniqueNos: StateFlow<List<FinalizedUniqueNoEntity>> = pengirimanDao.getUnuploadedFinalizedUniqueNosFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus: StateFlow<ScanStatus> = _scanStatus.asStateFlow()

    private val allFinalizedUniqueNos = pengirimanDao.getAllFinalizedUniqueNos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isConnected: StateFlow<Boolean> = connectivityObserver.isConnected.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    init {
        Log.d("PengirimanViewModel", "INIT: PengirimanViewModel created.")
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
            combine(unuploadedPengirimanList, unuploadedFinalizedUniqueNos, isConnected) { pengirimanData, finalizedData, connected ->
                (pengirimanData.isNotEmpty() || finalizedData.isNotEmpty()) && connected
            }.collect { shouldSync ->
                if (shouldSync) {
                    Log.d("PengirimanViewModel", "Triggering automatic sync via WorkManager.")
                    startSyncWorker()
                }
            }
        }
    }

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

    fun generateSpbNumber(selectedMandorLoading: String) {
        if (_isSessionActive.value) {
            Log.d("PengirimanViewModel", "Sesi aktif, lewati pembuatan SPB.")
            return
        }

        val currentDateTime = LocalDateTime.now()
        val currentMonth = currentDateTime.monthValue
        val currentYear = currentDateTime.year

        val lastMonth = settingsViewModel.getSpbLastMonth()
        val lastYear = settingsViewModel.getSpbLastYear()
        if (currentMonth != lastMonth || currentYear != lastYear) {
            settingsViewModel.resetAllSpbCounters()
            settingsViewModel.setSpbLastMonth(currentMonth)
            settingsViewModel.setSpbLastYear(currentYear)
            Log.d("PengirimanViewModel", "Counter SPB direset. Bulan/tahun baru terdeteksi.")
        }

        val counter = settingsViewModel.getSpbCounterForMandor(selectedMandorLoading) + 1
        settingsViewModel.setSpbCounterForMandor(selectedMandorLoading, counter)

        val spbFormat = settingsViewModel.getSpbFormat()
        val afdCode = settingsViewModel.getAfdCode()
        val sequenceNumber = String.format(Locale.getDefault(), "%04d", counter)
        val romanMonth = getRomanMonth(currentDateTime.month)

        spbNumber.value = "$spbFormat/$afdCode/$romanMonth/$currentYear/${selectedMandorLoading}$sequenceNumber"
        Log.d("PengirimanViewModel", "SPB dihasilkan: ${spbNumber.value}")
        _isSessionActive.value = true // Tandai sesi sebagai aktif
    }

    fun addScannedItem(item: ScannedItem) {
        viewModelScope.launch {
            _scanStatus.value = ScanStatus.Idle
            Log.d("PengirimanViewModel", "ADD_ITEM: Starting check for uniqueNo -> ${item.uniqueNo}")

            val isDuplicateInSession = scannedItems.first().any { it.uniqueNo == item.uniqueNo }
            val isAlreadyFinalized = allFinalizedUniqueNos.first().any { it.uniqueNo == item.uniqueNo }

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
                val finalizedEntity = FinalizedUniqueNoEntity(uniqueNo = item.uniqueNo, isUploaded = false)
                pengirimanDao.insertFinalizedUniqueNo(finalizedEntity)
            }
            scannedItemDao.deleteAllScannedItems()

            Log.d("PengirimanViewModel", "FINALISASI: Finalisasi selesai. Item sementara dihapus.")
            _scanStatus.value = ScanStatus.Finalized
            _isSessionActive.value = false // Reset sesi
            startSyncWorker()
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

                    val scannedItemsType = object : com.google.gson.reflect.TypeToken<List<ScannedItem>>() {}.type
                    val rawDetailScannedItems: List<ScannedItem> = gson.fromJson(data.detailScannedItemsJson, scannedItemsType) ?: emptyList()

                    rawDetailScannedItems.forEach { item ->
                        finalizedUniqueNoDbRef.child(item.uniqueNo).removeValue().await()
                        Log.d("PengirimanViewModel", "FIREBASE: Finalized uniqueNo ${item.uniqueNo} deleted.")
                    }
                }
            } catch (e: Exception) {
                Log.e("PengirimanViewModel", "Failed to delete multiple data from Firebase: ${e.message}")
            } finally {
                pengirimanDao.deleteMultiplePengiriman(ids)
                Log.d("PengirimanViewModel", "ROOM: Deleted multiple pengiriman data with IDs: $ids")
            }
        }
    }

    fun resetScanStatus() {
        _scanStatus.value = ScanStatus.Idle
    }

    private fun startSyncWorker() {
        Log.d("PengirimanViewModel", "Checking network status before enqueuing...")

        viewModelScope.launch {
            try {
                if (connectivityObserver.isConnected.value) {
                    Log.d("PengirimanViewModel", "Network is available, enqueuing SyncPengirimanWorker...")
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    val syncWorkRequest = OneTimeWorkRequestBuilder<SyncPengirimanWorker>()
                        .setConstraints(constraints)
                        .build()

                    WorkManager.getInstance(getApplication()).enqueueUniqueWork(
                        "SyncPengirimanWork",
                        ExistingWorkPolicy.KEEP,
                        syncWorkRequest
                    )
                } else {
                    Log.d("PengirimanViewModel", "Network not available, skipping enqueue.")
                }
            } catch (e: Exception) {
                Log.e("PengirimanViewModel", "Failed to start sync worker: ${e.message}")
            }
        }
    }
}