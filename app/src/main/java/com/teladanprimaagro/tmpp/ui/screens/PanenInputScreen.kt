@file:Suppress("DEPRECATION")

package com.teladanprimaagro.tmpp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.components.*
import com.teladanprimaagro.tmpp.util.NfcWriteDialog
import com.teladanprimaagro.tmpp.viewmodels.PanenViewModel
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.Exception
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanenInputScreen(
    navController: NavController,
    panenViewModel: PanenViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    nfcIntentFromActivity: State<Intent?>,
    panenDataToEdit: PanenData? = null
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val isEditing = panenDataToEdit != null
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)

    val currentDateTime = remember { LocalDateTime.now() }
    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm") }
    val dateTimeDisplay =
        remember { panenDataToEdit?.tanggalWaktu ?: currentDateTime.format(formatter) }

    var locationPart1 by remember(panenDataToEdit) { mutableStateOf(panenDataToEdit?.locationPart1 ?: "") }
    var locationPart2 by remember(panenDataToEdit) { mutableStateOf(panenDataToEdit?.locationPart2 ?: "") }

    val foremanOptions = settingsViewModel.mandorList.toList()
    var selectedForeman by remember(panenDataToEdit, foremanOptions) {
        mutableStateOf(panenDataToEdit?.kemandoran ?: foremanOptions.firstOrNull() ?: "")
    }
    var foremanExpanded by remember { mutableStateOf(false) }

    val harvesterOptions = settingsViewModel.pemanenList.toList()
    var selectedHarvester by remember(panenDataToEdit, harvesterOptions) {
        mutableStateOf(panenDataToEdit?.namaPemanen ?: harvesterOptions.firstOrNull() ?: "")
    }
    var harvesterExpanded by remember { mutableStateOf(false) }

    val blockOptions = settingsViewModel.blokList.toList()
    var selectedBlock by remember(panenDataToEdit, blockOptions) {
        mutableStateOf(panenDataToEdit?.blok ?: blockOptions.firstOrNull() ?: "")
    }
    var blockExpanded by remember { mutableStateOf(false) }

    val tphOptions = settingsViewModel.tphList.toList()
    var selectedTph by remember(panenDataToEdit, tphOptions) {
        mutableStateOf(panenDataToEdit?.noTph ?: tphOptions.firstOrNull() ?: "")
    }
    var tphExpanded by remember { mutableStateOf(false) }

    var buahN by remember(panenDataToEdit) { mutableIntStateOf(panenDataToEdit?.buahN ?: 0) }
    var buahA by remember(panenDataToEdit) { mutableIntStateOf(panenDataToEdit?.buahA ?: 0) }
    var buahOR by remember(panenDataToEdit) { mutableIntStateOf(panenDataToEdit?.buahOR ?: 0) }
    var buahE by remember(panenDataToEdit) { mutableIntStateOf(panenDataToEdit?.buahE ?: 0) }
    var buahAB by remember(panenDataToEdit) { mutableIntStateOf(panenDataToEdit?.buahAB ?: 0) }
    var buahBL by remember(panenDataToEdit) { mutableIntStateOf(panenDataToEdit?.buahBL ?: 0) }

    val totalBuah = remember(buahN, buahAB, buahOR) { buahN + buahAB + buahOR }

    var imageUri by remember(panenDataToEdit) {
        mutableStateOf(panenDataToEdit?.localImageUri?.toUri())
    }
    var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(imageUri) {
        imageBitmap = null
        if (imageUri != null) {
            var attempts = 0
            val maxAttempts = 5
            val retryDelayMs = 750L
            var lastException: Exception? = null
            while (attempts < maxAttempts) {
                try {
                    if (attempts > 0) {
                        delay(retryDelayMs)
                    }
                    context.contentResolver.openInputStream(imageUri!!)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            imageBitmap = bitmap
                            return@LaunchedEffect
                        }
                    }
                } catch (e: Exception) {
                    lastException = e
                }
                attempts++
            }
            imageBitmap = null
            if (lastException != null) {
                Log.e("PanenInputScreen", "Final Error loading image from URI: ${lastException.message}", lastException)
                if (isEditing) {
                    Toast.makeText(context, "Gagal memuat gambar lama: ${lastException.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                if (isEditing) {
                    Toast.makeText(context, "Gambar lama rusak atau tidak valid.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var showNfcWriteDialog by remember { mutableStateOf(false) }
    var nfcDataToPass by remember { mutableStateOf<PanenData?>(null) }
    val nfcAdapter: NfcAdapter? = remember { NfcAdapter.getDefaultAdapter(context) }
    val fusedLocationClient: FusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // --- KODE LOKASI BARU ---
    var isFindingLocation by remember { mutableStateOf(false) }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    locationPart1 = location.latitude.toString()
                    locationPart2 = location.longitude.toString()
                    Toast.makeText(context, "Lokasi terdeteksi.", Toast.LENGTH_SHORT).show()

                    // Setelah mendapatkan lokasi, hentikan pembaruan
                    fusedLocationClient.removeLocationUpdates(this)
                    isFindingLocation = false
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    val startLocationUpdates = {
        isFindingLocation = true
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000L // Coba ambil lokasi setiap 1 detik
            numUpdates = 1 // Hanya ambil 1 pembaruan, lalu berhenti
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
    // --- AKHIR KODE LOKASI BARU ---

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }
    var failureMessage by remember { mutableStateOf("") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                startLocationUpdates()
            } else {
                Toast.makeText(context, "Izin lokasi ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun generateUniqueCode(
        dateTime: LocalDateTime,
        block: String,
        totalBuah: Int
    ): String {
        val uniqueNoFormat = settingsViewModel.getUniqueNoFormat()
        val dateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HHmm")
        val formattedDate = dateTime.format(dateFormatter)
        val formattedTime = dateTime.format(timeFormatter)
        val cleanBlock = block.replace("[^a-zA-Z0-9]".toRegex(), "")
        val formattedBuah = totalBuah.toString().padStart(3, '0')
        return "$uniqueNoFormat$formattedDate$formattedTime$cleanBlock$formattedBuah"
    }

    val initialUniqueNo = remember {
        panenDataToEdit?.uniqueNo ?: generateUniqueCode(currentDateTime, "", 0)
    }
    var uniqueNo by remember(panenDataToEdit) { mutableStateOf(initialUniqueNo) }

    LaunchedEffect(selectedBlock, totalBuah) {
        if (!isEditing) {
            uniqueNo = generateUniqueCode(currentDateTime, selectedBlock, totalBuah)
        }
    }

    fun createImageUri(context: Context): Uri {
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
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        imageUri = uri
        return uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val capturedUri = imageUri
                if (capturedUri != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(100)
                        try {
                            context.contentResolver.openInputStream(capturedUri)?.use { inputStream ->
                                val bitmap = BitmapFactory.decodeStream(inputStream)
                                if (bitmap != null) {
                                    imageBitmap = bitmap
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Gagal memuat gambar: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        imageUri = capturedUri
                    }
                }
            } else {
                imageUri = null
                imageBitmap = null
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                cameraLauncher.launch(createImageUri(context))
            } else {
                Toast.makeText(context, "Izin kamera ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun resetForm() {
        locationPart1 = ""
        locationPart2 = ""
        selectedForeman = foremanOptions.firstOrNull() ?: ""
        selectedHarvester = harvesterOptions.firstOrNull() ?: ""
        selectedBlock = blockOptions.firstOrNull() ?: ""
        selectedTph = tphOptions.firstOrNull() ?: ""
        buahN = 0
        buahA = 0
        buahOR = 0
        buahE = 0
        buahAB = 0
        buahBL = 0
        imageUri = null
        imageBitmap = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.onPrimary)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = if (isEditing) "Edit Panen" else "Panen",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = Color.Transparent,
                )
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            TextInputField(
                label = "No. Unik",
                value = uniqueNo,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextInputField(
                label = "Tanggal/Jam",
                value = dateTimeDisplay,
                onValueChange = {},
                readOnly = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextInputField(
                    label = "Latitude",
                    value = locationPart1,
                    onValueChange = { if (!isEditing) locationPart1 = it },
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(end = 8.dp),
                    keyboardType = KeyboardType.Decimal,
                    readOnly = isEditing
                )
                TextInputField(
                    label = "Longitude",
                    value = locationPart2,
                    onValueChange = { if (!isEditing) locationPart2 = it },
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(start = 8.dp),
                    keyboardType = KeyboardType.Decimal,
                    readOnly = isEditing
                )
                IconButton(
                    onClick = {
                        if (!isEditing) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                startLocationUpdates()
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    },
                    enabled = !isEditing && !isFindingLocation, // Nonaktifkan saat mencari lokasi
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxHeight()
                        .background(
                            color = if (isEditing || isFindingLocation) Color.Gray.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Dapatkan Lokasi",
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            DropdownInputField(
                label = "Kemandoran",
                options = foremanOptions,
                selectedOption = selectedForeman,
                onOptionSelected = { selectedForeman = it },
                expanded = foremanExpanded,
                onExpandedChange = { foremanExpanded = it },
            )
            Spacer(modifier = Modifier.height(12.dp))
            DropdownInputField(
                label = "Nama Pemanen",
                options = harvesterOptions,
                selectedOption = selectedHarvester,
                onOptionSelected = { selectedHarvester = it },
                expanded = harvesterExpanded,
                onExpandedChange = { harvesterExpanded = it },
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropdownInputField(
                    label = "Blok",
                    options = blockOptions,
                    selectedOption = selectedBlock,
                    onOptionSelected = { selectedBlock = it },
                    expanded = blockExpanded,
                    onExpandedChange = { blockExpanded = it },
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(end = 8.dp),
                )
                DropdownInputField(
                    label = "No. TPH",
                    options = tphOptions,
                    selectedOption = selectedTph,
                    onOptionSelected = { selectedTph = it },
                    expanded = tphExpanded,
                    onExpandedChange = { tphExpanded = it },
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(start = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.primary
            )
            BuahCounter(label = "Buah N", count = buahN, onCountChange = { buahN = it })
            Spacer(modifier = Modifier.height(10.dp))
            BuahCounter(label = "Buah A", count = buahA, onCountChange = { buahA = it })
            Spacer(modifier = Modifier.height(10.dp))
            BuahCounter(label = "Buah OR", count = buahOR, onCountChange = { buahOR = it })
            Spacer(modifier = Modifier.height(10.dp))
            BuahCounter(label = "Buah E", count = buahE, onCountChange = { buahE = it })
            Spacer(modifier = Modifier.height(10.dp))
            BuahCounter(label = "Buah AB", count = buahAB, onCountChange = { buahAB = it })
            Spacer(modifier = Modifier.height(10.dp))
            BuahCounter(
                label = "Berondolan Lepas",
                count = buahBL,
                onCountChange = { buahBL = it }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Buah",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = totalBuah.toString(),
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        disabledTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledContainerColor = MaterialTheme.colorScheme.secondary,
                        cursorColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.width(120.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            cameraLauncher.launch(createImageUri(context))
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Image (Manual)",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "*Tekan untuk ambil gambar baru",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Ambil Gambar",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ambil Gambar",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (locationPart1.isBlank() || locationPart2.isBlank()) {
                        Toast.makeText(
                            context,
                            "Lokasi (Latitude/Longitude) tidak boleh kosong. Gunakan tombol lokasi atau isi manual.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (totalBuah <= 0) {
                        Toast.makeText(
                            context,
                            "Total Buah harus lebih dari 0. Pastikan setidaknya Buah N, Buah AB, atau Buah OR memiliki nilai.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (buahN == 0 && buahA == 0 && buahOR == 0 && buahE == 0 && buahAB == 0 && buahBL == 0) {
                        Toast.makeText(
                            context,
                            "Minimal satu jenis buah harus memiliki nilai lebih dari 0.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (imageUri == null || imageBitmap == null) {
                        Toast.makeText(context, "Harap ambil gambar panen.", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }
                    if (nfcAdapter == null || !nfcAdapter.isEnabled) {
                        val message = if (nfcAdapter == null) {
                            "NFC tidak tersedia di perangkat ini. Data tidak dapat disimpan."
                        } else {
                            "NFC dinonaktifkan. Harap aktifkan NFC untuk menyimpan data."
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    val panenDataFinal = PanenData(
                        id = panenDataToEdit?.id ?: 0,
                        tanggalWaktu = dateTimeDisplay,
                        uniqueNo = uniqueNo,
                        locationPart1 = locationPart1,
                        locationPart2 = locationPart2,
                        kemandoran = selectedForeman,
                        namaPemanen = selectedHarvester,
                        blok = selectedBlock,
                        noTph = selectedTph,
                        totalBuah = totalBuah,
                        buahN = buahN,
                        buahA = buahA,
                        buahOR = buahOR,
                        buahE = buahE,
                        buahAB = buahAB,
                        buahBL = buahBL,
                        localImageUri = imageUri?.toString(),
                        firebaseImageUrl = null,
                        isSynced = false
                    )
                    nfcDataToPass = panenDataFinal.copy(id = 0)
                    showNfcWriteDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFADFF2F),
                    contentColor = Color.Black),
            ) {
                Text(if (isEditing) "Simpan Perubahan" else "Kirim", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(50))
                            .padding(horizontal = 4.dp)
                    )
                    if (it < 2) Spacer(modifier = Modifier.width(8.dp))
                }
            }
            NfcWriteDialog(
                showDialog = showNfcWriteDialog,
                onDismissRequest = {
                    showNfcWriteDialog = false
                    nfcDataToPass = null
                },
                dataToWrite = nfcDataToPass,
                onWriteComplete = { success, message ->
                    showNfcWriteDialog = false
                    nfcDataToPass = null
                    if (success) {
                        showSuccessDialog = true
                        vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                        val panenData = PanenData(
                            id = panenDataToEdit?.id ?: 0,
                            tanggalWaktu = dateTimeDisplay,
                            uniqueNo = uniqueNo,
                            locationPart1 = locationPart1,
                            locationPart2 = locationPart2,
                            kemandoran = selectedForeman,
                            namaPemanen = selectedHarvester,
                            blok = selectedBlock,
                            noTph = selectedTph,
                            totalBuah = totalBuah,
                            buahN = buahN,
                            buahA = buahA,
                            buahOR = buahOR,
                            buahE = buahE,
                            buahAB = buahAB,
                            buahBL = buahBL,
                            localImageUri = imageUri?.toString(),
                            firebaseImageUrl = panenDataToEdit?.firebaseImageUrl,
                            isSynced = false
                        )
                        if (isEditing) {
                            panenViewModel.updatePanenData(panenData)
                        } else {
                            panenViewModel.compressImageAndSavePanen(panenData, imageUri)
                        }
                    } else {
                        failureMessage = message
                        showFailureDialog = true
                        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1))
                    }
                },
                nfcIntentFromActivity = nfcIntentFromActivity
            )
            if (showSuccessDialog) {
                SuccessDialog(
                    onDismiss = {
                        showSuccessDialog = false
                        navController.popBackStack()
                        if (!isEditing) {
                            resetForm()
                        }
                    }
                )
            }
            if (showFailureDialog) {
                FailureDialog(
                    message = failureMessage,
                    onDismiss = {
                        showFailureDialog = false
                    }
                )
            }
        }
    }
}