package com.teladanprimaagro.tmpp.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.SummaryBox
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistikPengirimanScreen(
    pengirimanViewModel: PengirimanViewModel = viewModel(),
    navController: NavController
) {
    val totalSemuaBuah by pengirimanViewModel.totalSemuaBuah.collectAsState()
    val totalDataMasuk by pengirimanViewModel.totalDataMasuk.collectAsState()
    val blokSummary by pengirimanViewModel.blokSummary.collectAsState()
    val supirSummary by pengirimanViewModel.supirSummary.collectAsState()
    val totalSuccessfulScans by pengirimanViewModel.totalSuccessfulScans.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Statistik Pengiriman",
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Ringkasan Statistik Pengiriman Hari ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryBox(
                        label = "Total Scan",
                        value = totalSuccessfulScans.toString(),
                    )
                    SummaryBox(
                        label = "Data Masuk",
                        value = totalDataMasuk.toString(),
                    )
                    SummaryBox(
                        label = "Total Buah",
                        value = totalSemuaBuah.toString(),
                    )
                }
            }

            item {
                ReportStatistikSection(
                    title = "Total Buah per Blok",
                    data = blokSummary.associate { it.blok to it.totalBuah },
                    barColor = MainColor
                )
            }

            item {
                ReportStatistikSection(
                    title = "Total Buah per Supir",
                    data = supirSummary.associate { it.namaSupir to it.totalBuah },
                    barColor = MainColor
                )
            }
        }
    }
}

// Menambahkan parameter 'titleColor' di sini
@Composable
fun ReportStatistikSection(
    title: String,
    data: Map<String, Int>,
    barColor: Color,
) {
    val maxValue = data.values.maxOrNull()?.toFloat() ?: 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 8.dp))

        if (data.isEmpty()) {
            Text(
                text = "Belum ada data.",
                style = MaterialTheme.typography.bodySmall,
                color = MainColor.copy(alpha = 0.8f),
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
            color = White // Menggunakan warna putih
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
