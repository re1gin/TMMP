package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.OldGrey
import com.teladanprimaagro.tmpp.ui.theme.SuccessGreen
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataPengirimanScreen(
    navController: NavController,
    pengirimanViewModel: PengirimanViewModel = viewModel()
) {
    val context = LocalContext.current

    // Mengambil semua data pengiriman dari ViewModel
    val allPengirimanData by pengirimanViewModel.pengirimanList.collectAsState()

    // Mengambil semua WorkInfo dari WorkManager untuk sync_pengiriman_work
    val workInfos by WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData("sync_pengiriman_work")
        .observeAsState(emptyList())

    // Mendapatkan WorkInfo yang sedang berjalan
    val syncingWorkInfo = workInfos.firstOrNull { workInfo -> workInfo.state == WorkInfo.State.RUNNING }

    // State untuk mengontrol filter data
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(paddingValues)
        ) {
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
                            val currentProgressData = syncingWorkInfo?.outputData
                            val currentWorkerId = currentProgressData?.getString("currentWorkerId")
                            val progress = if (currentWorkerId == pengirimanItem.spbNumber) {
                                currentProgressData.getFloat("progress", 0f)
                            } else {
                                0f // Tunjukkan 0 atau status "Menunggu"
                            }

                            val isSyncing = currentWorkerId == pengirimanItem.spbNumber

                            PengirimanBelumTerkirimCard(
                                pengirimanItem = pengirimanItem,
                                syncProgress = progress,
                                isSyncing = isSyncing
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
                Text(
                    text = "Ditambahkan: ${pengirimanItem.waktuPengiriman}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
                if (isSyncing) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { syncProgress },
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
                            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                            strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap
                        )
                        Text(
                            text = "${(syncProgress * 100).toInt()}%",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Text(
                        text = "Status: Menunggu",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
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