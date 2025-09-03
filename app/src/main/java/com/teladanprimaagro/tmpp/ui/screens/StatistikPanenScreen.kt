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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.Grey
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.PanenViewModel

@RequiresApi(Build.VERSION_CODES.O)
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
    val statistikJenisBuahPerBlok by panenViewModel.statistikJenisBuahPerBlok.collectAsState()
    val totalJenisBuah by panenViewModel.totalJenisBuah.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Diagram Buah", "Tabel Detail")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Statistik Panen",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Ringkasan Statistik Panen Hari ini",
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
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        SegmentedButton(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = tabTitles.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Color.White,
                                activeContentColor = Color.Black,
                                inactiveContainerColor = Color.DarkGray,
                                inactiveContentColor = Color.White
                            )
                        ) {
                            Text(title)
                        }
                    }
                }
            }

            when (selectedTabIndex) {
                0 -> { // Diagram Buah Tab
                    item {
                        StatistikSection(
                            title = "Buah per Pemanen",
                            data = statistikPerPemanen,
                            barColor = MainColor
                        )
                    }
                    item {
                        StatistikSection(
                            title = "Buah per Blok",
                            data = statistikPerBlok,
                            barColor = MainColor // Orange color
                        )
                    }
                }
                1 -> { // Tabel Detail Tab
                    item {
                        FruitTableSection(
                            data = statistikJenisBuahPerPemanen,
                            totalJenisBuah = totalJenisBuah
                        )
                    }
                    item {
                        FruitTableSection(
                            data = statistikJenisBuahPerBlok,
                            totalJenisBuah = totalJenisBuah,
                            title = "Detail Buah per Blok" // Assuming this section would be added
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatistikSection(title: String, data: Map<String, Int>, barColor: Color) {
    val maxValue = data.values.maxOrNull()?.toFloat() ?: 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = White)

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
            color = White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(White.copy(alpha = 0.2f))
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
            color = White,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}


@Composable
fun FruitTableSection(
    data: Map<String, Map<String, Int>>,
    totalJenisBuah: Map<String, Int>,
    title: String = "Detail Buah per Pemanen"
) {
    val sortedFruitKeys = listOf("N", "A", "OR", "E", "B", "BL")

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.DarkGray)

        if (data.isEmpty()) {
            Text(
                text = "Belum ada data panen buah.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        } else {
            // Header Tabel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MainColor),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Nama",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(7.dp)
                )
                sortedFruitKeys.forEach { jenis ->
                    Text(
                        text = jenis,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            data.forEach { (pemanen, jenisBuahMap) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .background(Grey.copy(0.5f))
                        .padding(horizontal = 0.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pemanen,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(horizontal = 8.dp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    sortedFruitKeys.forEach { jenis ->
                        val value = jenisBuahMap["Buah $jenis"] ?: 0
                        Text(
                            text = "$value",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.weight(0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.DarkGray)

            // Baris total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MainColor.copy(0.5f)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(7.dp)
                )
                sortedFruitKeys.forEach { jenis ->
                    val total = totalJenisBuah["Buah $jenis"] ?: 0
                    Text(
                        text = "$total",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}