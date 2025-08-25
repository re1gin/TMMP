package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.viewmodels.PanenViewModel

// Added colors for each fruit type
val buahNColor = Color(0xFF4CAF50) // Green
val buahAColor = Color(0xFFFF9800) // Orange
val buahORColor = Color(0xFF2196F3) // Blue
val buahEColor = Color(0xFFE91E63) // Pink
val buahABColor = Color(0xFF9C27B0) // Purple
val buahBLColor = Color(0xFFFFEB3B) // Yellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistikPanenScreen(
    navController: NavController,
    panenViewModel: PanenViewModel = viewModel()
) {
    val totalDataMasuk by panenViewModel.totalDataMasuk.collectAsState()
    val totalSemuaBuah by panenViewModel.totalSemuaBuah.collectAsState()
    val statistikPerPemanen by panenViewModel.statistikPerPemanen.collectAsState()
    val statistikPerBlok by panenViewModel.statistikPerBlok.collectAsState()
    val statistikJenisBuahPerPemanen by panenViewModel.statistikJenisBuahPerPemanen.collectAsState()
    val totalJenisBuah by panenViewModel.totalJenisBuah.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Statistik Panen",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SummarySection(
                    totalDataMasuk = totalDataMasuk,
                    totalSemuaBuah = totalSemuaBuah
                )
            }

            // Bar chart for total fruit per harvester
            item {
                StatistikSection(
                    title = "Total Buah per Pemanen",
                    data = statistikPerPemanen,
                    barColor = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Bar chart for total fruit per block
            item {
                StatistikSection(
                    title = "Total Buah per Blok",
                    data = statistikPerBlok,
                    barColor = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Tabel untuk detail buah per pemanen
            item {
                FruitTableSection(
                    data = statistikJenisBuahPerPemanen,
                    totalJenisBuah = totalJenisBuah
                )
            }
        }
    }
}

@Composable
fun SummarySection(totalDataMasuk: Int, totalSemuaBuah: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$totalDataMasuk",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Data Masuk",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
        // Tambahkan pemisah vertikal jika perlu, atau gunakan Spacer
        Spacer(modifier = Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$totalSemuaBuah",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Total Buah",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }
}

@Composable
fun StatistikSection(title: String, data: Map<String, Int>, barColor: Color) {
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
            color = Color.White
        )
        HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 8.dp))

        if (data.isEmpty()) {
            Text(
                text = "Belum ada data panen.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            data.forEach { (key, value) ->
                BarChartItem(
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
fun BarChartItem(
    label: String,
    value: Int,
    maxValue: Float,
    barColor: Color
) {
    var animationPlayed by remember { mutableStateOf(false) }

    val barWidth by animateFloatAsState(
        targetValue = if (animationPlayed) (value.toFloat() / maxValue) else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "bar_chart_animation_for_${label.replace(" ", "_")}"
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
            color = Color.White
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
            color = Color.White,
            modifier = Modifier.width(40.dp)
        )
    }
}


@Composable
fun FruitTableSection(
    data: Map<String, Map<String, Int>>,
    totalJenisBuah: Map<String, Int>
) {
    // Definisi warna tetap statis untuk setiap jenis buah
    val buahColors = mapOf(
        "Buah N" to buahNColor,
        "Buah A" to buahAColor,
        "Buah OR" to buahORColor,
        "Buah E" to buahEColor,
        "Buah AB" to buahABColor,
        "Buah BL" to buahBLColor
    )
    val sortedFruitKeys = listOf("Buah N", "Buah A", "Buah OR", "Buah E", "Buah AB", "Buah BL")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Detail Buah per Pemanen",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(Modifier.padding(bottom = 8.dp))

        if (data.isEmpty()) {
            Text(
                text = "Belum ada data panen buah.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        } else {
            // Header Tabel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nama",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1.2f)
                )
                sortedFruitKeys.forEach { jenis ->
                    Row(
                        modifier = Modifier.weight(0.5f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(buahColors[jenis] ?: Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = jenis.replace("Buah ", ""),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            // Baris data per pemanen
            data.forEach { (pemanen, jenisBuahMap) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pemanen,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.weight(1.2f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    sortedFruitKeys.forEach { jenis ->
                        val value = jenisBuahMap[jenis] ?: 0
                        Text(
                            text = "$value",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.weight(0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 8.dp))

            // Baris total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1.2f)
                )
                sortedFruitKeys.forEach { jenis ->
                    val total = totalJenisBuah[jenis] ?: 0
                    Text(
                        text = "$total",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Legenda warna
        HorizontalDivider(Modifier.padding(top = 16.dp, bottom = 8.dp))
        Text(
            text = "Indikator Warna",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val items = buahColors.filter { (jenis, _) ->
                (totalJenisBuah[jenis] ?: 0) > 0
            }

            val weightPerItem = 1f / (items.size.takeIf { it > 0 } ?: 1)

            items.forEach { (jenis, color) ->
                Box(
                    modifier = Modifier
                        .weight(weightPerItem, fill = true)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LegendItem(label = jenis, color = color)
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            drawRect(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label.replace("Buah ", ""),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}