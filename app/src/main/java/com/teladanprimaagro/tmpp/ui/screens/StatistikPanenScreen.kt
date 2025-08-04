package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.theme.BackgroundLightGray
import com.teladanprimaagro.tmpp.ui.viewmodels.PanenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistikPanenScreen(
    navController: NavController,
    panenViewModel: PanenViewModel = viewModel()
) {
    val statistikPerPemanen by panenViewModel.statistikPerPemanen.collectAsState()
    val statistikPerBlok by panenViewModel.statistikPerBlok.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Statistik Panen",
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
                .padding(16.dp)
        ) {
            // Statistik Per Pemanen
            StatistikSection(
                title = "Total Buah per Pemanen",
                data = statistikPerPemanen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Statistik Per Blok
            StatistikSection(
                title = "Total Buah per Blok",
                data = statistikPerBlok
            )
        }
    }
}

@Composable
fun StatistikSection(title: String, data: Map<String, Int>) {
    // Cari nilai maksimum untuk menentukan skala bar
    val maxValue = data.values.maxOrNull()?.toFloat() ?: 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundLightGray.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(Modifier.padding(bottom = 8.dp))

        if (data.isEmpty()) {
            Text(
                text = "Belum ada data panen.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(data.toList()) { (key, value) ->
                    BarChartItem(
                        label = key,
                        value = value,
                        maxValue = maxValue,
                        barColor = MaterialTheme.colorScheme.primary
                    )
                }
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

    // Animasi untuk bar chart
    val barWidth by animateFloatAsState(
        targetValue = if (animationPlayed) (value.toFloat() / maxValue) else 0f,
        animationSpec = tween(durationMillis = 1000)
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
            fontWeight = FontWeight.Medium
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
            modifier = Modifier.width(40.dp)
        )
    }
}