package com.teladanprimaagro.tmpp.ui.components

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.teladanprimaagro.tmpp.data.PanenData
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import java.io.File
import android.graphics.Color as AndroidColor

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

        // Direktori tempat ubin peta offline disimpan
        val offlineFolder = File(context.getExternalFilesDir(null), "osmdroid/tiles/")

        // Atur direktori cache agar osmdroid menggunakannya
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
                controller.setZoom(17.0)
                controller.setCenter(GeoPoint(-0.0, 102.0))
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            val compassOverlay = CompassOverlay(context, mapView)
            compassOverlay.enableCompass()
            mapView.overlays.add(compassOverlay)

            panenLocations.forEachIndexed { index, panen ->
                val marker = Marker(mapView)
                val lat = panen.locationPart1.toDoubleOrNull()
                val lon = panen.locationPart2.toDoubleOrNull()

                if (lat != null && lon != null) {
                    marker.position = GeoPoint(lat, lon)
                    marker.title = "Panen: ${panen.namaPemanen}"
                    marker.snippet = "Blok: ${panen.blok}, Total Buah: ${panen.totalBuah}"

                    val color = (AndroidColor.BLUE + (index * 12345) % 0xFFFFFF)

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