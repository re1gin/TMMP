package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SignalWifi0Bar
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.Grey
import com.teladanprimaagro.tmpp.ui.theme.LightGrey
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.OldGrey
import com.teladanprimaagro.tmpp.ui.theme.SuccessGreen
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.viewmodels.SyncStatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataPengirimanScreen(
    navController: NavController,
    pengirimanViewModel: PengirimanViewModel = viewModel(),
    syncStatusViewModel: SyncStatusViewModel = viewModel()
) {
    val allPengirimanData by pengirimanViewModel.pengirimanList.collectAsState()

    val syncMessage by syncStatusViewModel.syncMessage.collectAsState()
    val isSyncing by syncStatusViewModel.isSyncing.collectAsState()
    val syncProgress by syncStatusViewModel.syncProgress.collectAsState()
    val currentSyncId by syncStatusViewModel.currentSyncId.collectAsState()

    var selectedFilter by remember { mutableStateOf("Sudah Terkirim") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Status Data Pengiriman",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White,
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
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            val fabAlpha = if (isSyncing) 0.5f else 1f
            FloatingActionButton(
                onClick = {
                    if (!isSyncing) {
                        syncStatusViewModel.triggerManualSync()
                    }
                },
                modifier = Modifier.alpha(fabAlpha),
                containerColor = MainColor,
                contentColor = Black
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudSync,
                    contentDescription = "Sinkronisasi Sekarang"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (icon, iconColor, message) = when {
                    allPengirimanData.isEmpty() -> Triple(Icons.Filled.CloudOff, White, "Belum ada data yang diunggah")
                    syncMessage.contains("berhasil", ignoreCase = true) -> Triple(Icons.Filled.CheckCircle, SuccessGreen, syncMessage)
                    syncMessage.contains("gagal", ignoreCase = true) || syncMessage.contains("tidak ada koneksi", ignoreCase = true) -> Triple(Icons.Filled.Error, DangerRed, syncMessage)
                    syncMessage.contains("Mengunggah") || syncMessage.contains("Sinkronisasi sedang berjalan") -> Triple(Icons.Filled.CloudSync, MainColor, syncMessage)
                    syncMessage.contains("Menunggu koneksi") -> Triple(Icons.Filled.SignalWifi0Bar, Grey, syncMessage)
                    syncMessage.contains("Idle, terhubung") -> Triple(Icons.Filled.SignalWifi4Bar, SuccessGreen, syncMessage)
                    else -> Triple(Icons.Filled.CloudDone, Grey, syncMessage)
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Status Sinkronisasi",
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        message.contains("berhasil", ignoreCase = true) -> SuccessGreen
                        message.contains("gagal", ignoreCase = true) || message.contains("tidak ada koneksi", ignoreCase = true) || message == "Belum ada data yang diunggah" -> White
                        else -> White
                    }
                )
            }

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
                            activeContentColor = White,
                            activeBorderColor = Color.Transparent,
                            inactiveContainerColor = White,
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
                            activeContentColor = White,
                            activeBorderColor = Color.Transparent,
                            inactiveContainerColor = White,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                            inactiveBorderColor = Color.Transparent
                        )
                    ) {
                        Text("Belum Terkirim ($unsentCount)")
                    }
                }
            }

            val filteredData = when (selectedFilter) {
                "Sudah Terkirim" -> allPengirimanData.filter { it.isUploaded }
                "Belum Terkirim" -> allPengirimanData.filter { !it.isUploaded }
                else -> emptyList()
            }

            if (filteredData.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val message = if (selectedFilter == "Sudah Terkirim") {
                        "Belum ada data pengiriman yang terkirim."
                    } else {
                        "Belum ada data pengiriman yang menunggu."
                    }

                    val icon = if (selectedFilter == "Sudah Terkirim") {
                        Icons.Default.Upload
                    } else {
                        Icons.Default.Update
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = LightGrey
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = message,
                        color = LightGrey,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
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
                            val isItemSyncing =
                                isSyncing && (currentSyncId == pengirimanItem.spbNumber || syncMessage.contains(
                                    pengirimanItem.spbNumber
                                ))
                            val progress = if (isItemSyncing) syncProgress else 0f
                            PengirimanBelumTerkirimCard(
                                pengirimanItem = pengirimanItem,
                                syncProgress = progress,
                                isSyncing = isItemSyncing
                            )
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
                .fillMaxWidth()
                .background(OldGrey)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terkirim: ${pengirimanItem.waktuPengiriman}",
                    style = MaterialTheme.typography.bodySmall,
                    color = White
                )
                Text(
                    text = "Status: Terkirim",
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen,
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
fun PengirimanBelumTerkirimCard(pengirimanItem: PengirimanData, syncProgress: Float, isSyncing: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OldGrey)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Ditambahkan: ${pengirimanItem.waktuPengiriman}",
                        style = MaterialTheme.typography.bodySmall,
                        color = White
                    )
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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(60.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            progress = { syncProgress },
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
                            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                            strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap
                        )
                        Text(
                            text = "${(syncProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Menunggu Sinkronisasi",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}