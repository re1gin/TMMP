package com.teladanprimaagro.tmpp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.viewmodels.MapViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import java.io.File
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetaScreen(
    mapViewModel: MapViewModel = viewModel(),
    paddingValues: PaddingValues
) {
    val panenLocations by mapViewModel.panenLocations.collectAsState()
    var selectedPanenData by remember { mutableStateOf<PanenData?>(null) }

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
            }
        )

        // Tampilkan dialog saat marker diklik
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
}

@SuppressLint("RememberReturnType")
@Composable
fun OfflineMapView(
    modifier: Modifier = Modifier,
    panenLocations: List<PanenData>,
    onLocationClick: (PanenData) -> Unit
) {
    val context = LocalContext.current

    // Konfigurasi osmdroid (dilakukan hanya sekali)
    remember {
        val osmdroidConfig = Configuration.getInstance()
        osmdroidConfig.userAgentValue = context.packageName

        val offlineFolder = File(context.getExternalFilesDir(null), "osmdroid/tiles/")
        if (offlineFolder.exists()) {
            osmdroidConfig.osmdroidTileCache = offlineFolder
        }
    }

    AndroidView(
        factory = {
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                overlays.add(CompassOverlay(it, this))

            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            val compassOverlay = CompassOverlay(context, mapView)
            compassOverlay.enableCompass()
            mapView.overlays.add(compassOverlay)


            if (panenLocations.isNotEmpty()) {
                val firstPanen = panenLocations.first()
                val lat = firstPanen.locationPart1.toDoubleOrNull()
                val lon = firstPanen.locationPart2.toDoubleOrNull()
                if (lat != null && lon != null) {
                    mapView.controller.setCenter(GeoPoint(lat, lon))
                    mapView.controller.setZoom(17.0)
                }
            }

            panenLocations.forEachIndexed { index, panen ->
                val marker = Marker(mapView)
                val lat = panen.locationPart1.toDoubleOrNull()
                val lon = panen.locationPart2.toDoubleOrNull()

                if (lat != null && lon != null) {
                    marker.position = GeoPoint(lat, lon)
                    marker.title = "Panen: ${panen.namaPemanen}"
                    marker.snippet = "Blok: ${panen.blok}, Total Buah: ${panen.totalBuah}"

                    val color = (AndroidColor.BLUE + (index * 12345) % 0xFFFFFF)
                    val iconDrawable = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
                    iconDrawable?.setTint(color)
                    marker.icon = iconDrawable

                    marker.setOnMarkerClickListener { _, _ ->
                        onLocationClick(panen)
                        true
                    }
                    mapView.overlays.add(marker)
                }
            }
            mapView.invalidate()
        },
        modifier = modifier
    )
}