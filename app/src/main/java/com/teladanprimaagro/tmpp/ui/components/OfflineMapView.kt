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

@SuppressLint("RememberReturnType")
@Composable
fun OfflineMapView(
    modifier: Modifier = Modifier,
    panenLocations: List<PanenData>,
    onLocationClick: (PanenData) -> Unit,
    onMapReady: (MapView) -> Unit,
    pemanenColors: Map<String, Int>,
    currentLocation: GeoPoint? // Parameter baru untuk lokasi pengguna saat ini
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

                // Atur posisi awal peta
                val initialLocation = currentLocation
                    ?: if (panenLocations.isNotEmpty()) {
                        val firstPanen = panenLocations.first()
                        val lat = firstPanen.locationPart1.toDoubleOrNull()
                        val lon = firstPanen.locationPart2.toDoubleOrNull()
                        if (lat != null && lon != null) GeoPoint(lat, lon) else GeoPoint(-0.0, 102.0)
                    } else {
                        GeoPoint(-0.0, 102.0)
                    }
                controller.setCenter(initialLocation)
                controller.setZoom(17.0)
                onMapReady(this)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            val compassOverlay = CompassOverlay(context, mapView)
            compassOverlay.enableCompass()
            mapView.overlays.add(compassOverlay)

            val humanIconDrawable = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)

            panenLocations.forEach { panen ->
                val marker = Marker(mapView)
                val lat = panen.locationPart1.toDoubleOrNull()
                val lon = panen.locationPart2.toDoubleOrNull()

                if (lat != null && lon != null) {
                    marker.position = GeoPoint(lat, lon)
                    marker.title = "Pemanen: ${panen.namaPemanen}"
                    marker.snippet = "Blok: ${panen.blok}, Total Buah: ${panen.totalBuah}"

                    val color = pemanenColors[panen.namaPemanen]
                    humanIconDrawable?.let {
                        val wrappedDrawable = DrawableCompat.wrap(it).mutate()
                        if (color != null) {
                            DrawableCompat.setTint(wrappedDrawable, color)
                        }
                        marker.icon = wrappedDrawable
                    }

                    marker.setOnMarkerClickListener { _, _ ->
                        onLocationClick(panen)
                        true
                    }
                    mapView.overlays.add(marker)
                }
            }

            // Tambahkan penanda lokasi pengguna saat ini jika ada
            currentLocation?.let { location ->
                val userMarker = Marker(mapView)
                userMarker.position = location
                userMarker.title = "Lokasi Anda"
                val userIcon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default) // Gunakan ikon kustom jika ada
                userMarker.icon = userIcon
                mapView.overlays.add(userMarker)
            }

            mapView.invalidate()
        },
        modifier = modifier
    )
}