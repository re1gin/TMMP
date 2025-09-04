package com.teladanprimaagro.tmpp.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.teladanprimaagro.tmpp.data.AppDatabase
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.workers.SyncPanenWorker
import com.teladanprimaagro.tmpp.util.ConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("StaticFieldLeak")
class PanenViewModel(application: Application) : AndroidViewModel(application) {

    private val panenDao = AppDatabase.getDatabase(application).panenDao()
    private val panenDbRef = FirebaseDatabase.getInstance("https://ineka-database.firebaseio.com/").getReference("panenEntries")
    private val storage = FirebaseStorage.getInstance()
    private val context = application.applicationContext
    private val settingsViewModel: SettingsViewModel = SettingsViewModel(application)
    private val connectivityObserver = ConnectivityObserver(context)

    // State untuk form input
    private val _locationPart1 = MutableStateFlow("")
    val locationPart1: StateFlow<String> = _locationPart1.asStateFlow()

    private val _locationPart2 = MutableStateFlow("")
    val locationPart2: StateFlow<String> = _locationPart2.asStateFlow()

    private val _isFindingLocation = MutableStateFlow(false)
    val isFindingLocation: StateFlow<Boolean> = _isFindingLocation.asStateFlow()

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()

    private val _imageBitmap = MutableStateFlow<Bitmap?>(null)
    val imageBitmap: StateFlow<Bitmap?> = _imageBitmap.asStateFlow()

    private val _selectedForeman = MutableStateFlow("Pilih Mandormu!")
    val selectedForeman: StateFlow<String> = _selectedForeman.asStateFlow()

    private val _selectedHarvester = MutableStateFlow("Pilih Pemanenmu?")
    val selectedHarvester: StateFlow<String> = _selectedHarvester.asStateFlow()

    private val _selectedBlock = MutableStateFlow("Pilih Blok")
    val selectedBlock: StateFlow<String> = _selectedBlock.asStateFlow()

    private val _selectedTph = MutableStateFlow("Pilih TPH")
    val selectedTph: StateFlow<String> = _selectedTph.asStateFlow()

    private val _buahN = MutableStateFlow(0)
    val buahN: StateFlow<Int> = _buahN.asStateFlow()

    private val _buahA = MutableStateFlow(0)
    val buahA: StateFlow<Int> = _buahA.asStateFlow()

    private val _buahOR = MutableStateFlow(0)
    val buahOR: StateFlow<Int> = _buahOR.asStateFlow()

    private val _buahE = MutableStateFlow(0)
    val buahE: StateFlow<Int> = _buahE.asStateFlow()

    private val _buahAB = MutableStateFlow(0)
    val buahAB: StateFlow<Int> = _buahAB.asStateFlow()

    private val _buahBL = MutableStateFlow(0)
    val buahBL: StateFlow<Int> = _buahBL.asStateFlow()

    val totalBuah: StateFlow<Int> = combine(_buahN, _buahAB, _buahOR) { n, ab, or ->
        n + ab + or
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _uniqueNo = MutableStateFlow("")
    val uniqueNo: StateFlow<String> = combine(_selectedBlock, totalBuah
    ) { block, total ->
        generateUniqueCode(LocalDateTime.now(), block, total)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // State untuk data dan filter
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

    val statistikJenisBuahPerBlok: StateFlow<Map<String, Map<String, Int>>> = panenDao.getAllPanen()
        .map { panenList ->
            panenList.groupBy { it.blok }.mapValues { (_, dataList) ->
                mapOf(
                    "Buah N" to dataList.sumOf { it.buahN },
                    "Buah A" to dataList.sumOf { it.buahA },
                    "Buah OR" to dataList.sumOf { it.buahOR },
                    "Buah E" to dataList.sumOf { it.buahE },
                    "Buah AB" to dataList.sumOf { it.buahAB },
                    "Buah BL" to dataList.sumOf { it.buahBL }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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

    init {
        viewModelScope.launch {
            panenDao.getUnsyncedPanenDataFlow()
                .combine(connectivityObserver.isConnected) { unuploadedData, connected ->
                    unuploadedData.isNotEmpty() && connected
                }.collect { shouldSync ->
                    if (shouldSync) {
                        Log.d("PanenViewModel", "Triggering automatic sync via WorkManager.")
                        startSyncWorker()
                    }
                }
        }
    }

    fun setLocationPart1(value: String) { _locationPart1.value = value }
    fun setLocationPart2(value: String) { _locationPart2.value = value }
    fun setSelectedForeman(value: String) { _selectedForeman.value = value }
    fun setSelectedHarvester(value: String) { _selectedHarvester.value = value }
    fun setSelectedBlock(value: String) { _selectedBlock.value = value }
    fun setSelectedTph(value: String) { _selectedTph.value = value }
    fun setBuahN(value: Int) { _buahN.value = value }
    fun setBuahA(value: Int) { _buahA.value = value }
    fun setBuahOR(value: Int) { _buahOR.value = value }
    fun setBuahE(value: Int) { _buahE.value = value }
    fun setBuahAB(value: Int) { _buahAB.value = value }
    fun setBuahBL(value: Int) { _buahBL.value = value }

    // Membuat objek PanenData dari state saat ini
    @RequiresApi(Build.VERSION_CODES.O)
    fun createPanenData(id: Int, tanggalWaktu: String, firebaseImageUrl: String? = null): PanenData {
        return PanenData(
            id = id,
            tanggalWaktu = tanggalWaktu,
            uniqueNo = uniqueNo.value,
            locationPart1 = locationPart1.value,
            locationPart2 = locationPart2.value,
            kemandoran = selectedForeman.value,
            namaPemanen = selectedHarvester.value,
            blok = selectedBlock.value,
            noTph = selectedTph.value,
            totalBuah = totalBuah.value,
            buahN = buahN.value,
            buahA = buahA.value,
            buahOR = buahOR.value,
            buahE = buahE.value,
            buahAB = buahAB.value,
            buahBL = buahBL.value,
            localImageUri = imageUri.value?.toString(),
            firebaseImageUrl = firebaseImageUrl,
            isSynced = false
        )
    }

    // Validasi data form
    fun validatePanenData(nfcAdapter: android.nfc.NfcAdapter?): Pair<Boolean, String?> {
        if (_selectedForeman.value == "Pilih Mandor") {
            return Pair(false, "Harap pilih nama mandor.")
        }
        if (_selectedHarvester.value == "Pilih Pemanen") {
            return Pair(false, "Harap pilih nama pemanen.")
        }
        if (_selectedBlock.value == "Pilih Blok") {
            return Pair(false, "Harap pilih blok.")
        }
        if (_selectedTph.value == "Pilih TPH") {
            return Pair(false, "Harap pilih TPH.")
        }
        if (_locationPart1.value.isBlank() || _locationPart2.value.isBlank()) {
            return Pair(false, "Lokasi (Latitude/Longitude) tidak boleh kosong.")
        }
        if (totalBuah.value <= 0) {
            return Pair(false, "Total Buah harus lebih dari 0.")
        }
        if (_imageUri.value == null || _imageBitmap.value == null) {
            return Pair(false, "Harap ambil gambar panen.")
        }
        if (nfcAdapter == null || !nfcAdapter.isEnabled) {
            val message = if (nfcAdapter == null) {
                "NFC tidak tersedia di perangkat ini."
            } else {
                "NFC dinonaktifkan. Harap aktifkan NFC."
            }
            return Pair(false, message)
        }
        return Pair(true, null)
    }

    // Mengatur ulang form
    @RequiresApi(Build.VERSION_CODES.O)
    fun resetPanenForm() {
        _locationPart1.value = ""
        _locationPart2.value = ""
        _imageUri.value = null
        _imageBitmap.value?.recycle()
        _imageBitmap.value = null
        _selectedForeman.value = "Pilih Mandor"
        _selectedHarvester.value = "Pilih Pemanen"
        _selectedBlock.value = "Pilih Blok"
        _selectedTph.value = "Pilih TPH"
        _buahN.value = 0
        _buahA.value = 0
        _buahOR.value = 0
        _buahE.value = 0
        _buahAB.value = 0
        _buahBL.value = 0
    }

    // Memuat data untuk edit
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadEditData(panenData: PanenData) {
        _locationPart1.value = panenData.locationPart1
        _locationPart2.value = panenData.locationPart2
        _imageUri.value = panenData.localImageUri?.toUri()
        _uniqueNo.value = panenData.uniqueNo
        _selectedForeman.value = panenData.kemandoran
        _selectedHarvester.value = panenData.namaPemanen
        _selectedBlock.value = panenData.blok
        _selectedTph.value = panenData.noTph
        _buahN.value = panenData.buahN
        _buahA.value = panenData.buahA
        _buahOR.value = panenData.buahOR
        _buahE.value = panenData.buahE
        _buahAB.value = panenData.buahAB
        _buahBL.value = panenData.buahBL
        loadImageBitmap(
            uri = panenData.localImageUri?.toUri(),
            onSuccess = { /* Bitmap sudah diatur */ },
            onError = { /* Kesalahan ditangani di UI */ }
        )
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(onLocationResult: (String, String) -> Unit, onError: (String) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000L
            numUpdates = 1
        }
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    _locationPart1.value = location.latitude.toString()
                    _locationPart2.value = location.longitude.toString()
                    onLocationResult(location.latitude.toString(), location.longitude.toString())
                    fusedLocationClient.removeLocationUpdates(this)
                    _isFindingLocation.value = false
                } ?: run {
                    onError("Gagal mendapatkan lokasi.")
                    _isFindingLocation.value = false
                }
            }
        }
        _isFindingLocation.value = true
        viewModelScope.launch {
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
                delay(10_000)
                if (_isFindingLocation.value) {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    _isFindingLocation.value = false
                    onError("Timeout: Gagal mendapatkan lokasi dalam 10 detik.")
                }
            } catch (e: Exception) {
                onError("Error memulai pembaruan lokasi: ${e.message}")
                _isFindingLocation.value = false
            }
        }
    }

    fun createImageUri(): Uri {
        val photosDir = File(context.cacheDir, "panen_photos")
        photosDir.mkdirs()
        val newFile = File(photosDir, "IMG_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            newFile
        )
        context.grantUriPermission(
            context.packageName,
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        _imageUri.value = uri
        return uri
    }

    fun loadImageBitmap(uri: Uri?, onSuccess: (Bitmap?) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _imageBitmap.value?.recycle()
            _imageBitmap.value = null
            if (uri == null) {
                onSuccess(null)
                return@launch
            }
            var attempts = 0
            val maxAttempts = 5
            val retryDelayMs = 750L
            var lastException: Exception? = null
            while (attempts < maxAttempts) {
                try {
                    if (attempts > 0) {
                        delay(retryDelayMs)
                    }
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        _imageBitmap.value = bitmap
                        onSuccess(bitmap)
                        return@launch
                    }
                } catch (e: Exception) {
                    lastException = e
                }
                attempts++
            }
            _imageBitmap.value = null
            onError("Gagal memuat gambar: ${lastException?.message ?: "Gambar tidak valid"}")
        }
    }

    // Membersihkan gambar
    fun clearImage() {
        _imageUri.value = null
        _imageBitmap.value?.recycle()
        _imageBitmap.value = null
    }

    // Menyimpan data panen dengan gambar terkompresi
    fun compressImageAndSavePanen(panenData: PanenData, imageUri: Uri?) {
        viewModelScope.launch {
            if (imageUri == null) {
                savePanenData(panenData)
                return@launch
            }
            try {
                val compressedUri = compressImage(imageUri)
                val panenDataWithCompressedImage = panenData.copy(
                    localImageUri = compressedUri.toString(),
                    isSynced = false
                )
                savePanenData(panenDataWithCompressedImage)
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Error during image compression", e)
                savePanenData(panenData.copy(localImageUri = null))
            }
        }
    }

    private fun savePanenData(panenData: PanenData) {
        viewModelScope.launch {
            try {
                panenDao.insertPanen(panenData)
                // Tidak perlu memanggil startSyncWorker() di sini karena init block akan menangani sinkronisasi otomatis
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Error saving panen data to local DB", e)
            }
        }
    }

    private suspend fun compressImage(uri: Uri): Uri = suspendCoroutine { continuation ->
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val originalBitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                if (originalBitmap == null) {
                    continuation.resume(uri)
                    return@launch
                }

                val resizedBitmap = resizeBitmap(originalBitmap, 1200, 860)
                val compressedImageFile = File(context.cacheDir, "compressed_image_${UUID.randomUUID()}.jpg")

                var quality = 90
                var finalUri: Uri? = null

                while (quality >= 50) {
                    val outputStream = FileOutputStream(compressedImageFile)
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    outputStream.flush()
                    outputStream.close()

                    val fileSizeInKb = compressedImageFile.length() / 1024
                    Log.d("CompressImage", "Quality: $quality, Size: ${fileSizeInKb}KB")

                    if (fileSizeInKb <= 100) {
                        finalUri = compressedImageFile.toUri()
                        break
                    }
                    quality -= 5
                }

                if (finalUri != null) {
                    continuation.resume(finalUri)
                } else {
                    val outputStream = FileOutputStream(compressedImageFile)
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    continuation.resume(compressedImageFile.toUri())
                }

                originalBitmap.recycle()
                if (originalBitmap != resizedBitmap) resizedBitmap.recycle()

            } catch (e: Exception) {
                Log.e("PanenViewModel", "Error compressing image: ${e.message}", e)
                continuation.resume(uri)
            }
        }
    }

    private fun resizeBitmap(originalBitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalWidth = originalBitmap.width
        val originalHeight = originalBitmap.height
        val scaleFactor = minOf(
            targetWidth.toFloat() / originalWidth.toFloat(),
            targetHeight.toFloat() / originalHeight.toFloat()
        )
        return if (scaleFactor < 1) {
            val matrix = Matrix().apply { postScale(scaleFactor, scaleFactor) }
            Bitmap.createBitmap(originalBitmap, 0, 0, originalWidth, originalHeight, matrix, true)
        } else {
            originalBitmap
        }
    }

    // Memperbarui data panen
    fun updatePanenData(panen: PanenData) {
        viewModelScope.launch {
            try {
                panenDao.updatePanen(panen)
                // Tidak perlu memanggil startSyncWorker() di sini karena init block akan menangani sinkronisasi otomatis
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Error updating panen data", e)
            }
        }
    }

    // Memuat data panen berdasarkan ID
    fun loadPanenDataById(id: Int) {
        viewModelScope.launch {
            _panenDataToEdit.value = panenDao.getPanenById(id)
        }
    }

    // Mengosongkan data edit
    fun clearPanenDataToEdit() {
        _panenDataToEdit.value = null
    }

    // Menghapus gambar dari Firebase Storage
    private suspend fun deleteImageFromFirebaseStorage(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            return
        }
        try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
        } catch (e: Exception) {
            Log.e("PanenViewModel", "Error deleting image from Firebase Storage", e)
        }
    }

    // Menghapus data panen terpilih
    fun deleteSelectedPanenData(ids: List<Int>) {
        viewModelScope.launch {
            val dataToDelete = panenDao.getPanenByIds(ids)
            dataToDelete.forEach { panen ->
                try {
                    panenDbRef.child(panen.uniqueNo).removeValue().await()
                    deleteImageFromFirebaseStorage(panen.firebaseImageUrl)
                } catch (e: Exception) {
                    Log.e("PanenViewModel", "Error deleting selected panen data", e)
                }
            }
            panenDao.deleteMultiplePanen(ids)
        }
    }

    // Memicu WorkManager untuk sinkronisasi
    private fun startSyncWorker() {
        Log.d("PanenViewModel", "Checking network status before enqueuing...")

        viewModelScope.launch {
            try {
                if (connectivityObserver.isConnected.value) {
                    Log.d("PanenViewModel", "Network is available, enqueuing SyncPanenWorker...")
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    val syncWorkRequest = OneTimeWorkRequestBuilder<SyncPanenWorker>()
                        .setConstraints(constraints)
                        .build()

                    WorkManager.getInstance(getApplication()).enqueueUniqueWork(
                        "SyncPanenWork",
                        ExistingWorkPolicy.KEEP,
                        syncWorkRequest
                    )
                } else {
                    Log.d("PanenViewModel", "Network not available, skipping enqueue.")
                }
            } catch (e: Exception) {
                Log.e("PanenViewModel", "Failed to start sync worker: ${e.message}")
            }
        }
    }

    // Membuat kode unik
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateUniqueCode(dateTime: LocalDateTime, block: String, totalBuah: Int): String {
        val uniqueNoFormat = settingsViewModel.getUniqueNoFormat()
        val dateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HHmm")
        val formattedDate = dateTime.format(dateFormatter)
        val formattedTime = dateTime.format(timeFormatter)
        val formattedBlock = if (block == "Pilih Blok") "ABC" else block.replace("[^a-zA-Z0-9]".toRegex(), "")
        val formattedBuah = totalBuah.toString().padStart(3, '0')
        return "$uniqueNoFormat$formattedDate$formattedTime$formattedBlock$formattedBuah"
    }

    // Fungsi untuk filter dan sort
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
            "Waktu" -> list.sortedBy { it.tanggalWaktu }
            else -> list
        }
    }

    private fun sortPanenListByOrder(list: List<PanenData>, isAscending: Boolean): List<PanenData> {
        return if (isAscending) {
            when (_sortBy.value) {
                "Nama" -> list.sortedBy { it.namaPemanen }
                "Blok" -> list.sortedBy { it.blok }
                "Waktu" -> list.sortedBy { it.tanggalWaktu }
                else -> list
            }
        } else {
            when (_sortBy.value) {
                "Nama" -> list.sortedByDescending { it.namaPemanen }
                "Blok" -> list.sortedByDescending { it.blok }
                "Waktu" -> list.sortedByDescending { it.tanggalWaktu }
                else -> list
            }
        }
    }

    private fun filterByPemanen(list: List<PanenData>, pemanenFilter: String): List<PanenData> {
        return if (pemanenFilter == "Semua") list else list.filter { it.namaPemanen == pemanenFilter }
    }

    private fun filterByBlok(list: List<PanenData>, blokFilter: String): List<PanenData> {
        return if (blokFilter == "Semua") list else list.filter { it.blok == blokFilter }
    }
}