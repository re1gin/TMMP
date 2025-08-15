package com.teladanprimaagro.tmpp.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.navigation.NavHostController
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.viewmodels.MapViewModel
import com.teladanprimaagro.tmpp.ui.components.OfflineMapView
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetaScreen(
    navController: NavHostController,
    mapViewModel: MapViewModel = viewModel(),
    paddingValues: PaddingValues
) {
    val panenLocations by mapViewModel.panenLocations.collectAsState()
    var selectedPanenData by remember { mutableStateOf<PanenData?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    val context = LocalContext.current

    // State untuk lokasi pengguna saat ini
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var showDeleteButton by remember { mutableStateOf(false) }

    // FusedLocationProviderClient untuk mendapatkan lokasi
    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Daftar unik pemanen dan peta warna
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .padding(16.dp)
                ) {
                    Text(
                        "Daftar Pemanen",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
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
                                        coroutineScope.launch { drawerState.close() }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            color = pemanenColors[pemanenName]?.let { Color(it) } ?: Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(pemanenName)
                            }
                        }
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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

            TopAppBar(
                title = {
                    Text(
                        text = "Lokasi Panen",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch { drawerState.open() }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Daftar Pemanen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (showDeleteButton) {
                    FloatingActionButton(
                        onClick = {
                            currentLocation = null
                            showDeleteButton = false
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
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
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationSearching,
                        contentDescription = "Tampilkan Posisiku"
                    )
                }
            }
        }
    }

    selectedPanenData?.let { panenData ->
        AlertDialog(
            onDismissRequest = { selectedPanenData = null },
            title = { Text("Detail Panen") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Pemanen: ${panenData.namaPemanen}")
                    Text("Blok: ${panenData.blok}")
                    Text("Total Buah: ${panenData.totalBuah}")
                    Text("Waktu: ${panenData.tanggalWaktu}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPanenData = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

// Fungsi untuk memeriksa dan meminta izin lokasi
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

// Fungsi untuk mendapatkan lokasi terakhir yang diketahui
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