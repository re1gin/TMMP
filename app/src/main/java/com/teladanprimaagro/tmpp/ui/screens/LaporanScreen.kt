package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel

// Menambahkan parameter 'titleColor' agar bisa menerima warna dari MainScreen
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanScreen(
    pengirimanViewModel: PengirimanViewModel = viewModel(),
    titleColor: Color // Parameter baru untuk warna teks
) {
    val totalSemuaBuah by pengirimanViewModel.totalSemuaBuah.collectAsState()
    val totalDataMasuk by pengirimanViewModel.totalDataMasuk.collectAsState()
    val blokSummary by pengirimanViewModel.blokSummary.collectAsState()
    val supirSummary by pengirimanViewModel.supirSummary.collectAsState()
    val totalSuccessfulScans by pengirimanViewModel.totalSuccessfulScans.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Laporan Pengiriman",
                        fontWeight = FontWeight.Bold,
                        color = Color.White // Menggunakan warna putih
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Ringkasan Statistik Utama",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // Menggunakan warna putih
                )
                Spacer(modifier = Modifier.height(8.dp))
                MainStatsColumn(
                    totalBuah = totalSemuaBuah,
                    totalScanned = totalSuccessfulScans,
                    totalFinalized = totalDataMasuk
                )
            }

            item {
                ReportStatistikSection(
                    title = "Total Buah per Blok",
                    data = blokSummary.associate { it.blok to it.totalBuah },
                    barColor = MaterialTheme.colorScheme.onPrimary,
                    titleColor = Color.White // Meneruskan warna putih
                )
            }

            item {
                ReportStatistikSection(
                    title = "Total Buah per Supir",
                    data = supirSummary.associate { it.namaSupir to it.totalBuah },
                    barColor = MaterialTheme.colorScheme.onPrimary,
                    titleColor = Color.White // Meneruskan warna putih
                )
            }
        }
    }
}

@Composable
fun MainStatsColumn(totalBuah: Int, totalScanned: Int, totalFinalized: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        StatistikItem("Total Buah", totalBuah.toString(), Color.White) // Menggunakan warna putih
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        StatistikItem("Data Scan", totalScanned.toString(), Color.White) // Menggunakan warna putih
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        StatistikItem("Data Masuk", totalFinalized.toString(), Color.White) // Menggunakan warna putih
    }
}

@Composable
fun StatistikItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// Menambahkan parameter 'titleColor' di sini
@Composable
fun ReportStatistikSection(
    title: String,
    data: Map<String, Int>,
    barColor: Color,
    titleColor: Color
) {
    val maxValue = data.values.maxOrNull()?.toFloat() ?: 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = titleColor // Menggunakan parameter yang baru
        )
        HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 8.dp))

        if (data.isEmpty()) {
            Text(
                text = "Belum ada data.",
                style = MaterialTheme.typography.bodySmall,
                color = titleColor.copy(alpha = 0.8f), // Menggunakan parameter yang baru
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            data.forEach { (key, value) ->
                ReportBarChartItem(
                    label = key,
                    value = value,
                    maxValue = maxValue,
                    barColor = barColor
                )
            }
        }
    }
}

@Composable
fun ReportBarChartItem(
    label: String,
    value: Int,
    maxValue: Float,
    barColor: Color
) {
    var animationPlayed by remember { mutableStateOf(false) }

    val barWidth by animateFloatAsState(
        targetValue = if (animationPlayed) (value.toFloat() / maxValue) else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "bar_width_animation"
    )
    LaunchedEffect(key1 = Unit) {
        animationPlayed = true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(90.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White // Menggunakan warna putih
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(barColor.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = barWidth)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$value",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White, // Menggunakan warna putih
            modifier = Modifier.width(40.dp)
        )
    }
}
