@file:Suppress("DEPRECATION")
package com.teladanprimaagro.tmpp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.components.BuahCounter
import com.teladanprimaagro.tmpp.ui.components.DropdownInputField
import com.teladanprimaagro.tmpp.ui.components.FailureDialog
import com.teladanprimaagro.tmpp.ui.components.SuccessDialog
import com.teladanprimaagro.tmpp.ui.components.TextInputField
import com.teladanprimaagro.tmpp.ui.components.TotalBuahDisplay
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.Grey
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.OldGrey
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.util.NfcWriteDialog
import com.teladanprimaagro.tmpp.viewmodels.PanenViewModel
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanenInputScreen(
    navController: NavController,
    panenViewModel: PanenViewModel,
    settingsViewModel: SettingsViewModel,
    sharedNfcViewModel: SharedNfcViewModel,
    panenDataToEdit: PanenData? = null
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val isEditing = panenDataToEdit != null
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)

    val currentDateTime = remember { LocalDateTime.now() }
    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm") }
    val dateTimeDisplay = remember { panenDataToEdit?.tanggalWaktu ?: currentDateTime.format(formatter) }

    // Menggunakan state dari PanenViewModel
    val locationPart1 by panenViewModel.locationPart1.collectAsState()
    val locationPart2 by panenViewModel.locationPart2.collectAsState()
    val isFindingLocation by panenViewModel.isFindingLocation.collectAsState()
    val imageUri by panenViewModel.imageUri.collectAsState()
    val imageBitmap by panenViewModel.imageBitmap.collectAsState()
    val uniqueNo by panenViewModel.uniqueNo.collectAsState()
    val selectedForeman by panenViewModel.selectedForeman.collectAsState()
    val selectedHarvester by panenViewModel.selectedHarvester.collectAsState()
    val selectedBlock by panenViewModel.selectedBlock.collectAsState()
    val selectedTph by panenViewModel.selectedTph.collectAsState()
    val buahN by panenViewModel.buahN.collectAsState()
    val buahA by panenViewModel.buahA.collectAsState()
    val buahOR by panenViewModel.buahOR.collectAsState()
    val buahE by panenViewModel.buahE.collectAsState()
    val buahAB by panenViewModel.buahAB.collectAsState()
    val buahBL by panenViewModel.buahBL.collectAsState()
    val totalBuah by panenViewModel.totalBuah.collectAsState()

    // Dropdown options dari SettingsViewModel
    val foremanOptions = settingsViewModel.mandorList.toList()
    var foremanExpanded by remember { mutableStateOf(false) }

    val harvesterOptions = settingsViewModel.pemanenList.toList()
    var harvesterExpanded by remember { mutableStateOf(false) }

    val blockOptions = settingsViewModel.blokList.toList()
    var blockExpanded by remember { mutableStateOf(false) }

    val tphOptions = settingsViewModel.tphList.toList()
    var tphExpanded by remember { mutableStateOf(false) }

    // State untuk indikator loading gambar
    var isImageLoading by remember { mutableStateOf(false) }

    // Memuat data edit jika ada
    LaunchedEffect(panenDataToEdit) {
        if (panenDataToEdit != null) {
            panenViewModel.loadEditData(panenDataToEdit)
        } else {
            panenViewModel.resetPanenForm()
        }
    }

    // Memuat bitmap saat imageUri berubah
    LaunchedEffect(imageUri) {
        isImageLoading = true
        panenViewModel.loadImageBitmap(
            uri = imageUri,
            onSuccess = { isImageLoading = false },
            onError = { error ->
                isImageLoading = false
                if (isEditing) {
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // State untuk NFC dan dialog
    var showNfcWriteDialog by remember { mutableStateOf(false) }
    var nfcDataToPass by remember { mutableStateOf<PanenData?>(null) }
    val nfcAdapter = remember { android.nfc.NfcAdapter.getDefaultAdapter(context) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }
    var failureMessage by remember { mutableStateOf("") }

    // Launcher untuk izin lokasi
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                panenViewModel.startLocationUpdates(
                    onLocationResult = { lat, lon ->
                        Toast.makeText(context, "Lokasi terdeteksi.", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(context, "Izin lokasi ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Launcher untuk izin kamera dan pengambilan gambar
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            isImageLoading = false
            if (success) {
                panenViewModel.loadImageBitmap(
                    uri = imageUri,
                    onSuccess = { /* Bitmap sudah diatur */ },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                panenViewModel.clearImage()
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isImageLoading = true
                cameraLauncher.launch(panenViewModel.createImageUri())
            } else {
                Toast.makeText(context, "Izin kamera ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Panen" else "Panen",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) {
                            navController.popBackStack()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Clear else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isEditing) "Batal" else "Kembali",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .verticalScroll(scrollState)
            ) {
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
                        onValueChange = { if (!isEditing) panenViewModel.setLocationPart1(it) },
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(end = 8.dp),
                        keyboardType = KeyboardType.Decimal,
                        readOnly = true
                    )
                    TextInputField(
                        label = "Longitude",
                        value = locationPart2,
                        onValueChange = { if (!isEditing) panenViewModel.setLocationPart2(it) },
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(start = 8.dp),
                        keyboardType = KeyboardType.Decimal,
                        readOnly = true
                    )
                    IconButton(
                        onClick = {
                            if (!isEditing) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    panenViewModel.startLocationUpdates(
                                        onLocationResult = { lat, lon ->
                                            Toast.makeText(
                                                context,
                                                "Lokasi terdeteksi.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, error, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    )
                                } else {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }
                        },
                        enabled = !isEditing && !isFindingLocation,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .fillMaxHeight()
                            .background(
                                color = if (isEditing || isFindingLocation) OldGrey.copy(alpha = 0.5f) else MainColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        if (isFindingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                color = MainColor,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Dapatkan Lokasi",
                                tint = Black,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                DropdownInputField(
                    label = "Kemandoran",
                    options = foremanOptions,
                    selectedOption = selectedForeman,
                    onOptionSelected = { panenViewModel.setSelectedForeman(it) },
                    expanded = foremanExpanded,
                    onExpandedChange = { foremanExpanded = it },
                )
                Spacer(modifier = Modifier.height(12.dp))
                DropdownInputField(
                    label = "Nama Pemanen",
                    options = harvesterOptions,
                    selectedOption = selectedHarvester,
                    onOptionSelected = { panenViewModel.setSelectedHarvester(it) },
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
                        onOptionSelected = { panenViewModel.setSelectedBlock(it) },
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
                        onOptionSelected = { panenViewModel.setSelectedTph(it) },
                        expanded = tphExpanded,
                        onExpandedChange = { tphExpanded = it },
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(start = 8.dp),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = White
                )
                Spacer(modifier = Modifier.height(20.dp))

                BuahCounter(
                    label = "Buah N",
                    count = buahN,
                    onCountChange = { panenViewModel.setBuahN(it) })
                Spacer(modifier = Modifier.height(10.dp))
                BuahCounter(
                    label = "Buah A",
                    count = buahA,
                    onCountChange = { panenViewModel.setBuahA(it) })
                Spacer(modifier = Modifier.height(10.dp))
                BuahCounter(
                    label = "Buah OR",
                    count = buahOR,
                    onCountChange = { panenViewModel.setBuahOR(it) })
                Spacer(modifier = Modifier.height(10.dp))
                BuahCounter(
                    label = "Buah E",
                    count = buahE,
                    onCountChange = { panenViewModel.setBuahE(it) })
                Spacer(modifier = Modifier.height(10.dp))
                BuahCounter(
                    label = "Buah AB",
                    count = buahAB,
                    onCountChange = { panenViewModel.setBuahAB(it) })
                Spacer(modifier = Modifier.height(10.dp))
                BuahCounter(
                    label = "Berondolan Lepas",
                    count = buahBL,
                    onCountChange = { panenViewModel.setBuahBL(it) })
                Spacer(modifier = Modifier.height(10.dp))
                TotalBuahDisplay(value = totalBuah)

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = White
                )
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Grey, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !isImageLoading) {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                isImageLoading = true
                                cameraLauncher.launch(panenViewModel.createImageUri())
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isImageLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = Black,
                            strokeWidth = 4.dp
                        )
                    } else if (imageBitmap != null) {
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
                            color = White,
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
                                tint = Black,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Ambil Gambar",
                                color = Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val (isValid, errorMessage) = panenViewModel.validatePanenData(nfcAdapter)
                        if (!isValid) {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val panenDataFinal = panenViewModel.createPanenData(
                            id = panenDataToEdit?.id ?: 0,
                            tanggalWaktu = dateTimeDisplay,
                            firebaseImageUrl = null
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
                        contentColor = Color.Black,
                        disabledContentColor = Color.White)
                ) {
                    Text(
                        text = if (isEditing) "Simpan Perubahan" else "Kirim",
                        color = Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            val panenData = panenViewModel.createPanenData(
                                id = panenDataToEdit?.id ?: 0,
                                tanggalWaktu = dateTimeDisplay,
                                firebaseImageUrl = panenDataToEdit?.firebaseImageUrl
                            )
                            if (isEditing) {
                                panenViewModel.updatePanenData(panenData)
                            } else {
                                panenViewModel.compressImageAndSavePanen(panenData, imageUri)
                            }
                        } else {
                            failureMessage = message
                            showFailureDialog = true
                            vibrator?.vibrate(
                                VibrationEffect.createWaveform(
                                    longArrayOf(
                                        0,
                                        200,
                                        100,
                                        200
                                    ), -1
                                )
                            )
                        }
                    },
                    sharedNfcViewModel = sharedNfcViewModel
                )
                if (showSuccessDialog) {
                    SuccessDialog(
                        onDismiss = {
                            showSuccessDialog = false
                            navController.popBackStack()
                            if (!isEditing) {
                                panenViewModel.resetPanenForm()
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
}