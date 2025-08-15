package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.SuccessGreen
import com.teladanprimaagro.tmpp.viewmodels.PanenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataTerkirimScreen(
    navController: NavController,
    panenViewModel: PanenViewModel = viewModel()
) {
    // Mengambil semua data panen
    val allPanenData by panenViewModel.panenList.collectAsState()

    // Mengambil status sinkronisasi
    val isSyncing by panenViewModel.isSyncing.collectAsState()

    // Mengambil progres sinkronisasi
    val syncProgress by panenViewModel.syncProgress.collectAsState()
    val totalItemsToSync by panenViewModel.totalItemsToSync.collectAsState()

    // State untuk mengontrol filter data
    var selectedFilter by remember { mutableStateOf("Sudah Terkirim") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Status Data Panen",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
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

            @OptIn(ExperimentalMaterial3Api::class)
            if (allPanenData.isNotEmpty()) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SegmentedButton(
                        selected = selectedFilter == "Sudah Terkirim",
                        onClick = { selectedFilter = "Sudah Terkirim" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = SuccessGreen,
                            activeContentColor = Color.White,
                            activeBorderColor = Color.Transparent, // border saat aktif
                            inactiveContainerColor = Color.White,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                            inactiveBorderColor = Color.Transparent // border saat nonaktif
                        )
                    ) {
                        Text("Sudah Terkirim")
                    }

                    SegmentedButton(
                        selected = selectedFilter == "Belum Terkirim",
                        onClick = { selectedFilter = "Belum Terkirim" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = DangerRed,
                            activeContentColor = Color.White,
                            activeBorderColor = Color.Transparent,
                            inactiveContainerColor = Color.White,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                            inactiveBorderColor = Color.Transparent
                        )
                    ) {
                        Text("Belum Terkirim")
                    }
                }
            }

            // Tentukan data yang akan ditampilkan berdasarkan filter yang dipilih
            val filteredData = when (selectedFilter) {
                "Sudah Terkirim" -> allPanenData.filter { it.isSynced }
                "Belum Terkirim" -> allPanenData.filter { !it.isSynced }
                else -> emptyList()
            }

            if (filteredData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val message = if (selectedFilter == "Sudah Terkirim") {
                        "Belum ada data panen yang terkirim."
                    } else {
                        "Belum ada data panen yang menunggu."
                    }
                    Text(message, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredData) { panenItem ->
                        if (panenItem.isSynced) {
                            DataTerkirimCard(panenItem = panenItem)
                        } else {
                            DataBelumTerkirimCard(panenItem = panenItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DataTerkirimCard(panenItem: PanenData) {
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
                    text = "Terkirim: ${panenItem.tanggalWaktu}",
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
                text = "Pemanen: ${panenItem.namaPemanen}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No. Unik: ${panenItem.uniqueNo}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DataBelumTerkirimCard(panenItem: PanenData) {
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
                    text = "Ditambahkan: ${panenItem.tanggalWaktu}",
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
                text = "Pemanen: ${panenItem.namaPemanen}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No. Unik: ${panenItem.uniqueNo}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}