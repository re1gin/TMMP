package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.ui.components.PengirimanDetailDialog
import com.teladanprimaagro.tmpp.ui.components.PengirimanTableRow
import com.teladanprimaagro.tmpp.ui.components.SummaryBox
import com.teladanprimaagro.tmpp.ui.components.TableHeaderText
import com.teladanprimaagro.tmpp.ui.theme.BackgroundLightGray
import com.teladanprimaagro.tmpp.ui.theme.DotGray
import com.teladanprimaagro.tmpp.ui.theme.PrimaryOrange
import com.teladanprimaagro.tmpp.ui.theme.TextGray
import com.teladanprimaagro.tmpp.ui.viewmodels.PengirimanViewModel
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekapPengirimanScreen(navController: NavController, pengirimanViewModel: PengirimanViewModel = viewModel()) {

    val pengirimanList by pengirimanViewModel.pengirimanList.collectAsState()
    val totalDataMasuk by pengirimanViewModel.totalDataMasuk.collectAsState()
    val totalSemuaBuah by pengirimanViewModel.totalSemuaBuah.collectAsState()

    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedPengirimanData by remember { mutableStateOf<PengirimanData?>(null) }

    LaunchedEffect(key1 = Unit) {
        while(true) {
            val now = LocalTime.now()
            val midnight = LocalTime.MIDNIGHT

            val durationUntilMidnight = if (now.isBefore(midnight)) {
                ChronoUnit.MILLIS.between(now, midnight)
            } else {
                ChronoUnit.MILLIS.between(now, midnight.plusHours(24))
            }

            Log.d("RekapPengirimanScreen", "Menunggu hingga tengah malam: $durationUntilMidnight ms")

            delay(durationUntilMidnight)
            pengirimanViewModel.clearAllPengirimanData()
            Log.d("RekapPengirimanScreen", "Data berhasil di reset pada pukul 00.00.")

            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Rekap Evakuasi",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Aksi pengaturan */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(PrimaryOrange, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderText(text = "Tanggal", weight = 0.20f)
                TableHeaderText(text = "No. SPB", weight = 0.35f)
                TableHeaderText(text = "Total", weight = 0.20f)
                TableHeaderText(text = "Edit", weight = 0.10f)
                TableHeaderText(text = "Detail", weight = 0.15f)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .background(BackgroundLightGray.copy(alpha = 0.1f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            ) {
                if (pengirimanList.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada data pengiriman.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = TextGray
                        )
                    }
                } else {
                    items(pengirimanList, key = { it.id }) { data ->
                        PengirimanTableRow(
                            data = data,
                            onDetailClick = { clickedData: PengirimanData ->
                                selectedPengirimanData = clickedData
                                showDetailDialog = true
                            },
                            onEditClick = { editedData: PengirimanData ->
                                Log.d("RekapPengiriman", "Edit clicked for ID: ${editedData.id}")
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.5f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryBox(label = "Data Masuk", value = totalDataMasuk.toString())
                SummaryBox(label = "Total Buah", value = totalSemuaBuah.toString())
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "*Data akan di reset setiap pukul 00.00",
                color = TextGray,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }

    if (showDetailDialog && selectedPengirimanData != null) {
        PengirimanDetailDialog(
            pengirimanEntry = selectedPengirimanData!!,
            onDismiss = {
                showDetailDialog = false
                selectedPengirimanData = null
            },
            onSendPrintClick = { pengirimanDataToPrint ->
                navController.navigate("send_print_data/${pengirimanDataToPrint.id}")
                showDetailDialog = false
                selectedPengirimanData = null
            }
        )
    }
}