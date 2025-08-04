package com.teladanprimaagro.tmpp.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.teladanprimaagro.tmpp.ui.components.BuahCounter
import com.teladanprimaagro.tmpp.ui.components.DropdownInputField
import com.teladanprimaagro.tmpp.ui.components.TextInputField
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.theme.BackgroundLightGray
import com.teladanprimaagro.tmpp.ui.theme.DotGray
import com.teladanprimaagro.tmpp.ui.theme.IconOrange
import com.teladanprimaagro.tmpp.ui.theme.TextGray
import com.teladanprimaagro.tmpp.ui.viewmodels.PanenViewModel
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel
import com.teladanprimaagro.tmpp.util.NfcWriteDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun getLocation(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    onLocationReceived: (String, String) -> Unit
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(location.latitude.toString(), location.longitude.toString())
                    Toast.makeText(context, "Lokasi terdeteksi.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Tidak dapat menemukan lokasi. Pastikan GPS aktif.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal mendapatkan lokasi: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanenInputScreen(
    navController: NavController,
    panenViewModel: PanenViewModel,
    settingsViewModel: SettingsViewModel,
    nfcIntentFromActivity: State<Intent?>
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var uniqueNo by remember { mutableStateOf("") }
    val currentDateTime = remember { LocalDateTime.now() }
    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm") }
    val dateTimeDisplay = remember { currentDateTime.format(formatter) }
    var locationPart1 by remember { mutableStateOf("") } // Latitude
    var locationPart2 by remember { mutableStateOf("") } // Longitude

    val foremanOptions = settingsViewModel.mandorList.toList()
    var selectedForeman by remember(foremanOptions) { mutableStateOf(foremanOptions.firstOrNull() ?: "") }
    var foremanExpanded by remember { mutableStateOf(false) }

    val harvesterOptions = settingsViewModel.pemanenList.toList()
    var selectedHarvester by remember(harvesterOptions) { mutableStateOf(harvesterOptions.firstOrNull() ?: "") }
    var harvesterExpanded by remember { mutableStateOf(false) }

    val blockOptions = settingsViewModel.blokList.toList()
    var selectedBlock by remember(blockOptions) { mutableStateOf(blockOptions.firstOrNull() ?: "") }
    var blockExpanded by remember { mutableStateOf(false) }

    val tphOptions = settingsViewModel.tphList.toList()
    var selectedTph by remember(tphOptions) { mutableStateOf(tphOptions.firstOrNull() ?: "") }
    var tphExpanded by remember { mutableStateOf(false) }

    var buahN by remember { mutableIntStateOf(0) }
    var buahA by remember { mutableIntStateOf(0) }
    var buahOR by remember { mutableIntStateOf(0) }
    var buahE by remember { mutableIntStateOf(0) }
    var buahAB by remember { mutableIntStateOf(0) }
    var buahBL by remember { mutableIntStateOf(0) }

    val totalBuah = buahN + buahAB + buahOR

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    var showNfcWriteDialog by remember { mutableStateOf(false) }
    var nfcDataToPass by remember { mutableStateOf<PanenData?>(null) }
    val nfcAdapter: NfcAdapter? = remember { NfcAdapter.getDefaultAdapter(context) }

    val fusedLocationClient: FusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // VARIABEL UNTUK MENYIMPAN DATA LENGKAP YANG AKAN DISIMPAN KE ROOM
    var dataToSaveToRoom by remember { mutableStateOf<PanenData?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    getLocation(fusedLocationClient, context) { lat, lon ->
                        locationPart1 = lat
                        locationPart2 = lon
                    }
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    getLocation(fusedLocationClient, context) { lat, lon ->
                        locationPart1 = lat
                        locationPart2 = lon
                    }
                }
                else -> {
                    Toast.makeText(context, "Izin lokasi ditolak.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    fun generateUniqueCode(
        dateTime: LocalDateTime,
        block: String,
        totalBuah: Int
    ): String {
        val dateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HHmm")
        val formattedDate = dateTime.format(dateFormatter)
        val formattedTime = dateTime.format(timeFormatter)

        val cleanBlock = block.replace("[^a-zA-Z0-9]".toRegex(), "")
        val formattedBuah = totalBuah.toString().padStart(3, '0')

        return "AME1$formattedDate$formattedTime$cleanBlock$formattedBuah"
    }

    LaunchedEffect(currentDateTime, selectedBlock, totalBuah) {
        uniqueNo = generateUniqueCode(currentDateTime, selectedBlock, totalBuah)
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
                        delay(500) // Small delay to ensure the image is written to the file system

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

                        imageUri = null
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
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = "Panen",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { navController.navigate("pengaturan_screen") }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Pengaturan",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
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
                    onValueChange = { locationPart1 = it },
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(end = 8.dp),
                    keyboardType = KeyboardType.Decimal
                )
                TextInputField(
                    label = "Longitude",
                    value = locationPart2,
                    onValueChange = { locationPart2 = it },
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(start = 8.dp),
                    keyboardType = KeyboardType.Decimal
                )
                IconButton(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxHeight()
                        .background(
                            color = IconOrange,
                            shape = RoundedCornerShape(10.dp))
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
                onExpandedChange = { foremanExpanded = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            DropdownInputField(
                label = "Nama Pemanen",
                options = harvesterOptions,
                selectedOption = selectedHarvester,
                onOptionSelected = { selectedHarvester = it },
                expanded = harvesterExpanded,
                onExpandedChange = { harvesterExpanded = it }
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
                        .padding(end = 8.dp)
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
                        .padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                thickness = 1.dp,
                color = DotGray
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
            BuahCounter(label = "Berondolan Lepas", count = buahBL, onCountChange = { buahBL = it })

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Buah",
                    color = MaterialTheme.colorScheme.onSurface,
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
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = DotGray,
                        disabledBorderColor = DotGray,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.width(120.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Input Gambar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(BackgroundLightGray, RoundedCornerShape(8.dp))
                    .border(1.dp, DotGray, RoundedCornerShape(8.dp))
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
                            tint = DotGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ambil Gambar",
                            color = TextGray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol untuk mengirim data panen.
            Button(
                onClick = {

                    // Validasi Lokasi
                    if (locationPart1.isBlank() || locationPart2.isBlank()) {
                        Toast.makeText(context, "Lokasi (Latitude/Longitude) tidak boleh kosong. Gunakan tombol lokasi atau isi manual.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // totalBuah adalah hasil penjumlahan buahN, buahAB, buahOR
                    if (totalBuah <= 0) {
                        Toast.makeText(context, "Total Buah harus lebih dari 0. Pastikan setidaknya Buah N, Buah AB, atau Buah OR memiliki nilai.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (buahN == 0) {
                        Toast.makeText(context, "Minimal satu buah tidak boleh 0.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (imageUri == null || imageBitmap == null) {
                        Toast.makeText(context, "Harap ambil gambar panen.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    // --- AKHIR VALIDASI INPUT ---


                    // Buat objek PanenData LENGKAP
                    val newPanenData = PanenData(
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
                        imageUri = imageUri?.toString()
                    )

                    // Simpan data lengkap ke state untuk diakses nanti jika NFC berhasil
                    dataToSaveToRoom = newPanenData

                    // Cek ketersediaan dan status NFC
                    if (nfcAdapter == null) {
                        Toast.makeText(context, "NFC tidak tersedia di perangkat ini. Data akan langsung disimpan.", Toast.LENGTH_LONG).show()
                        // Jika NFC tidak tersedia, langsung simpan ke Room
                        panenViewModel.addPanenData(
                            newPanenData.tanggalWaktu,
                            newPanenData.uniqueNo,
                            newPanenData.locationPart1,
                            newPanenData.locationPart2,
                            newPanenData.kemandoran,
                            newPanenData.namaPemanen,
                            newPanenData.blok,
                            newPanenData.noTph,
                            newPanenData.totalBuah,
                            newPanenData.buahN,
                            newPanenData.buahA,
                            newPanenData.buahOR,
                            newPanenData.buahE,
                            newPanenData.buahAB,
                            newPanenData.buahBL,
                            newPanenData.imageUri
                        )
                        navController.popBackStack()
                        resetForm()
                    } else if (!nfcAdapter.isEnabled) {
                        Toast.makeText(context, "NFC dinonaktifkan. Harap aktifkan di pengaturan. Data akan langsung disimpan.", Toast.LENGTH_LONG).show()
                        panenViewModel.addPanenData(
                            newPanenData.tanggalWaktu,
                            newPanenData.uniqueNo,
                            newPanenData.locationPart1,
                            newPanenData.locationPart2,
                            newPanenData.kemandoran,
                            newPanenData.namaPemanen,
                            newPanenData.blok,
                            newPanenData.noTph,
                            newPanenData.totalBuah,
                            newPanenData.buahN,
                            newPanenData.buahA,
                            newPanenData.buahOR,
                            newPanenData.buahE,
                            newPanenData.buahAB,
                            newPanenData.buahBL,
                            newPanenData.imageUri
                        )
                        navController.popBackStack()
                        resetForm()
                    } else {
                        nfcDataToPass = newPanenData.toNfcWriteableData()
                        showNfcWriteDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFADFF2F),
                    contentColor = Color.Black
                )
            ) {
                Text("Kirim", fontWeight = FontWeight.Bold)
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
                            .background(DotGray, shape = RoundedCornerShape(50))
                            .padding(horizontal = 4.dp)
                    )
                    if (it < 2) Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        // Dialog penulisan NFC.
        NfcWriteDialog(
            showDialog = showNfcWriteDialog,
            onDismissRequest = {
                showNfcWriteDialog = false
                nfcDataToPass = null
                dataToSaveToRoom = null
            },
            dataToWrite = nfcDataToPass,
            onWriteComplete = { success, message ->
                if (success) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    // Jika NFC berhasil ditulis, baru simpan data LENGKAP ke Room
                    dataToSaveToRoom?.let { originalData ->
                        panenViewModel.addPanenData(
                            originalData.tanggalWaktu,
                            originalData.uniqueNo,
                            originalData.locationPart1,
                            originalData.locationPart2,
                            originalData.kemandoran,
                            originalData.namaPemanen, // Menggunakan namaPemanen dari data LENGKAP
                            originalData.blok,
                            originalData.noTph,
                            originalData.totalBuah,
                            originalData.buahN,
                            originalData.buahA,
                            originalData.buahOR,
                            originalData.buahE,
                            originalData.buahAB,
                            originalData.buahBL,
                            originalData.imageUri
                        )
                    }
                    navController.popBackStack()
                    resetForm()
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
                showNfcWriteDialog = false
                nfcDataToPass = null
                dataToSaveToRoom = null // Reset setelah selesai, baik berhasil maupun gagal NFC
            },
            nfcIntentFromActivity = nfcIntentFromActivity
        )
    }
}