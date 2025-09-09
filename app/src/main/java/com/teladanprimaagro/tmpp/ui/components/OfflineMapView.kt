package com.teladanprimaagro.tmpp.ui.components

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.teladanprimaagro.tmpp.R
import com.teladanprimaagro.tmpp.data.PanenData
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import java.io.File
import androidx.core.graphics.drawable.DrawableCompat

@Suppress("DEPRECATION")
@SuppressLint("RememberReturnType")
@Composable
fun OfflineMapView(
    modifier: Modifier = Modifier,
    panenLocations: List<PanenData>,
    onLocationClick: (PanenData) -> Unit,
    onMapReady: (MapView) -> Unit,
    pemanenColors: Map<String, Int>,
    currentLocation: GeoPoint? // Lokasi user
) {
    val context = LocalContext.current

    // Konfigurasi osmdroid (sekali saja)
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

                val initialCenter = GeoPoint(1.349672, 118.5097795)
                controller.setCenter(initialCenter)
                controller.setZoom(15)
                minZoomLevel = 13.5
                onMapReady(this)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            // Compass
            val compassOverlay = CompassOverlay(context, mapView)
            compassOverlay.enableCompass()
            mapView.overlays.add(compassOverlay)


            // Tambah marker panen
            panenLocations.forEach { panen ->
                val lat = panen.locationPart1.toDoubleOrNull()
                val lon = panen.locationPart2.toDoubleOrNull()

                if (lat != null && lon != null) {
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(lat, lon)
                        title = "Pemanen: ${panen.namaPemanen}"
                        snippet = "Blok: ${panen.blok}, Total Buah: ${panen.totalBuah}"

                        // Ikon marker panen → Buat ikon baru di setiap iterasi agar warnanya unik
                        val harvesterIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_map)
                        harvesterIconDrawable?.let {
                            val wrappedDrawable = DrawableCompat.wrap(it).mutate()
                            pemanenColors[panen.namaPemanen]?.let { color ->
                                DrawableCompat.setTint(wrappedDrawable, color)
                            }
                            icon = wrappedDrawable
                        }

                        setOnMarkerClickListener { _, _ ->
                            onLocationClick(panen)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }
            }

            currentLocation?.let { location ->
                val userMarker = Marker(mapView).apply {
                    position = location
                    title = "Lokasi Anda"
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_user_location)
                }
                mapView.overlays.add(userMarker)
            }

            mapView.invalidate()
        },
        modifier = modifier
    )
}
