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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.SuccessGreen
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

    // State untuk mengontrol filter data
    var selectedFilter by remember { mutableStateOf("Sudah Terkirim") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Status Data Pengiriman",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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

            // Perubahan dimulai di sini untuk menambahkan jumlah data
            if (allPengirimanData.isNotEmpty()) {
                val sentCount = allPengirimanData.count { it.isUploaded }
                val unsentCount = allPengirimanData.count { !it.isUploaded }

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
                            activeBorderColor = Color.Transparent,
                            inactiveContainerColor = Color.White,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                            inactiveBorderColor = Color.Transparent
                        )
                    ) {
                        Text("Sudah Terkirim ($sentCount)")
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
                        Text("Belum Terkirim ($unsentCount)")
                    }
                }
            }
            // Perubahan berakhir di sini

            // Tentukan data yang akan ditampilkan berdasarkan filter yang dipilih
            val filteredData = when (selectedFilter) {
                "Sudah Terkirim" -> allPengirimanData.filter { it.isUploaded }
                "Belum Terkirim" -> allPengirimanData.filter { !it.isUploaded }
                else -> emptyList()
            }

            if (filteredData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val message = if (selectedFilter == "Sudah Terkirim") {
                        "Belum ada data pengiriman yang terkirim."
                    } else {
                        "Belum ada data pengiriman yang menunggu."
                    }
                    Text(message, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredData) { pengirimanItem ->
                        if (pengirimanItem.isUploaded) {
                            PengirimanTerkirimCard(pengirimanItem = pengirimanItem)
                        } else {
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    color = Color.White
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    color = Color.White
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