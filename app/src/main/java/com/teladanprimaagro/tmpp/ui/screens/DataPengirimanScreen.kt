package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
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
                        color = White
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
            Text(
                text = syncMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = when {
                    syncMessage.contains("berhasil", ignoreCase = true) -> SuccessGreen
                    syncMessage.contains("gagal", ignoreCase = true) -> DangerRed
                    else -> White
                }
            )

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