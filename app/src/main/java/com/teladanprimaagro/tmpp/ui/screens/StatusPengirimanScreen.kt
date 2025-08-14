package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusPengirimanScreen(
    navController: NavController,
    pengirimanViewModel: PengirimanViewModel = viewModel()
) {
    // Mengambil semua data pengiriman
    val allPengirimanData by pengirimanViewModel.pengirimanList.collectAsState()

    // Mengambil status sinkronisasi
    val isSyncing by pengirimanViewModel.isSyncing.collectAsState()

    // Mengambil progres sinkronisasi
    val syncProgress by pengirimanViewModel.syncProgress.collectAsState()
    val totalItemsToSync by pengirimanViewModel.totalItemsToSync.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status Data Pengiriman") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Tampilkan indikator loading dan persentase jika sedang melakukan sinkronisasi
            if (isSyncing) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = syncProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mensinkronkan ${(syncProgress * totalItemsToSync).toInt()} dari $totalItemsToSync...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (allPengirimanData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada data pengiriman.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Filter dan tampilkan data yang sudah terkirim terlebih dahulu
                    val syncedData = allPengirimanData.filter { it.isUploaded }
                    if (syncedData.isNotEmpty()) {
                        item {
                            Text(
                                text = "Sudah Terkirim (${syncedData.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(syncedData) { pengirimanItem ->
                            PengirimanTerkirimCard(pengirimanItem = pengirimanItem)
                        }
                    }

                    // Tambahkan pemisah
                    val unsyncedData = allPengirimanData.filter { !it.isUploaded }
                    if (syncedData.isNotEmpty() && unsyncedData.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Filter dan tampilkan data yang belum terkirim
                    if (unsyncedData.isNotEmpty()) {
                        item {
                            Text(
                                text = "Belum Terkirim (${unsyncedData.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(unsyncedData) { pengirimanItem ->
                            PengirimanBelumTerkirimCard(pengirimanItem = pengirimanItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PengirimanTerkirimCard(pengirimanItem: PengirimanData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terkirim: ${pengirimanItem.waktuPengiriman}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Status: Terkirim",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Green,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Supir: ${pengirimanItem.namaSupir}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "SPB No: ${pengirimanItem.spbNumber}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun PengirimanBelumTerkirimCard(pengirimanItem: PengirimanData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ditambahkan: ${pengirimanItem.waktuPengiriman}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Status: Menunggu",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Supir: ${pengirimanItem.namaSupir}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "SPB No: ${pengirimanItem.spbNumber}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}