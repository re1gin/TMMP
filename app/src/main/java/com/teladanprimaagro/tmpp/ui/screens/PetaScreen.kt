package com.teladanprimaagro.tmpp.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.components.OfflineMapView // <-- Tambahkan baris impor ini
import com.teladanprimaagro.tmpp.ui.viewmodels.MapViewModel

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
            onLocationClick = { panen: PanenData -> // <-- Tentukan tipe parameter panen secara eksplisit
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
