package com.teladanprimaagro.tmpp.ui.screens.harvester

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.components.OfflineMapView
import com.teladanprimaagro.tmpp.ui.theme.BackgroundDarkGrey
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.PanenViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetaScreen(
    navController: NavController,
    panenViewModel: PanenViewModel = viewModel(),
) {
    val panenLocations by panenViewModel.panenList.collectAsState()
    var selectedPanenData by remember { mutableStateOf<PanenData?>(null) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    val context = LocalContext.current

    // State untuk lokasi pengguna saat ini
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var showDeleteButton by remember { mutableStateOf(false) }

    // State untuk mengontrol tampilan ModalBottomSheet
    var showBottomSheet by remember { mutableStateOf(false) }

    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val uniquePemanenNames = panenLocations.map { it.namaPemanen }.toSet().toList().sorted()
    val colors = listOf(
        Color.Red, Color.Blue, Color.Green, Color.Magenta, Color.Cyan,
        Color.Yellow, Color.Gray, Color.DarkGray, Color.LightGray
    )
    val pemanenColors = remember(uniquePemanenNames) {
        uniquePemanenNames.mapIndexed { index, name ->
            name to colors[index % colors.size].toArgb()
        }.toMap()
    }

    // SheetState untuk ModalBottomSheet
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    // Launcher untuk meminta izin lokasi
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation(fusedLocationClient) { location ->
                currentLocation = location
                mapViewInstance?.controller?.animateTo(location)
                mapViewInstance?.controller?.setZoom(17.0)
                showDeleteButton = true
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pengiriman",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MainBackground
                )
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(paddingValues)
        ) {
            OfflineMapView(
                modifier = Modifier.fillMaxSize(),
                panenLocations = panenLocations,
                onLocationClick = { panen: PanenData ->
                    selectedPanenData = panen
                },
                onMapReady = { mapView ->
                    mapViewInstance = mapView
                },
                pemanenColors = pemanenColors,
                currentLocation = currentLocation
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Tombol baru untuk menampilkan daftar pemanen
                FloatingActionButton(
                    onClick = { showBottomSheet = true },
                    modifier = Modifier.padding(bottom = 8.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Tampilkan Daftar Pemanen",
                        tint = MainColor
                    )
                }

                if (showDeleteButton) {
                    FloatingActionButton(
                        onClick = {
                            currentLocation = null
                            showDeleteButton = false
                        },
                        modifier = Modifier.padding(bottom = 8.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Hapus Lokasi",
                            tint = Color.Red
                        )
                    }
                }

                FloatingActionButton(
                    onClick = {
                        checkAndRequestLocation(
                            context = context,
                            onPermissionGranted = {
                                getCurrentLocation(fusedLocationClient) { location ->
                                    currentLocation = location
                                    mapViewInstance?.controller?.animateTo(location)
                                    mapViewInstance?.controller?.setZoom(17.0)
                                    showDeleteButton = true
                                }
                            },
                            onPermissionDenied = {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationSearching,
                        contentDescription = "Tampilkan Posisiku",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    // ModalBottomSheet sekarang hanya ditampilkan ketika `showBottomSheet` bernilai true
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color.Gray, RoundedCornerShape(2.dp))
                    )
                }
            },
            containerColor = BackgroundDarkGrey,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 200.dp, max = 500.dp)
            ) {
                Text(
                    "Daftar Pemanen",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
                LazyColumn(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(uniquePemanenNames) { pemanenName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val firstPanenLocation = panenLocations.firstOrNull { it.namaPemanen == pemanenName }
                                    firstPanenLocation?.let { panen ->
                                        val lat = panen.locationPart1.toDoubleOrNull()
                                        val lon = panen.locationPart2.toDoubleOrNull()
                                        if (lat != null && lon != null) {
                                            mapViewInstance?.controller?.animateTo(GeoPoint(lat, lon))
                                            mapViewInstance?.controller?.setZoom(17.0)
                                        }
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = pemanenColors[pemanenName]?.let { Color(it) } ?: Color.Red,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(pemanenName, color = Color.White)
                            Spacer(Modifier.weight(1f))
                            Text("Indikator", color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = pemanenColors[pemanenName]?.let {Color(it) }?: Color.Red,
                                        shape = CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }

    selectedPanenData?.let { panenData ->
        AlertDialog(
            onDismissRequest = { selectedPanenData = null },
            title = { Text(
                text = "Detail Panen",
                color = MaterialTheme.colorScheme.onPrimary) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pemanen: ${panenData.namaPemanen}",
                        color = Color.White)
                    Text(
                        text = "Blok: ${panenData.blok}",
                        color = Color.White)
                    Text(
                        text = "Total Buah: ${panenData.totalBuah}",
                        color = Color.White)
                    Text(
                        text = "Waktu: ${panenData.tanggalWaktu}",
                        color = Color.White)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPanenData = null }) {
                    Text("Tutup", color = Color.White)
                }
            },
            containerColor = BackgroundDarkGrey,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private fun checkAndRequestLocation(
    context: Context,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    when {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED -> {
            onPermissionGranted()
        }
        else -> {
            onPermissionDenied()
        }
    }
}

@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (GeoPoint) -> Unit
) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(GeoPoint(location.latitude, location.longitude))
            }
        }
}
