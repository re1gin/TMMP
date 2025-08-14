package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.tv.material3.MaterialTheme
import com.teladanprimaagro.tmpp.ui.components.FullDateCard
import com.teladanprimaagro.tmpp.ui.components.MenuButton
import com.teladanprimaagro.tmpp.ui.theme.OrangePink1
import com.teladanprimaagro.tmpp.ui.theme.OrangePink2
import com.teladanprimaagro.tmpp.ui.theme.primaryGradient
import com.teladanprimaagro.tmpp.ui.theme.secondaryGradient


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HarvesterContent(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    HarvesterScreenBackground {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(0.dp))
            FullDateCard(title = "Hai, Harvester!")

            Spacer(modifier = Modifier.height(70.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MenuButton(
                        text = "Panen",
                        icon = Icons.Default.Add,
                        gradientColors = primaryGradient,
                        onClick = { navController.navigate("panenInputScreen/-1") }
                    )
                    MenuButton(
                        text = "Rekap Panen",
                        icon = Icons.Default.Description,
                        gradientColors = secondaryGradient,
                        onClick = { navController.navigate("rekap_panen_screen") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MenuButton(
                        text = "Statistik",
                        icon = Icons.Default.Equalizer,
                        gradientColors = primaryGradient,
                        onClick = { navController.navigate("statistik_panen_screen") }
                    )
                    MenuButton(
                        text = "Data Terkirim",
                        icon = Icons.Default.CloudDone,
                        gradientColors = secondaryGradient,
                        onClick = { navController.navigate("data_terkirim_screen") }
                    )
                }
            }
        }
    }
}

@Composable
fun HarvesterScreenBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val waveHeight = 10f
            val waveLength = size.width

            // Gradient di bagian atas layar
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        OrangePink1, OrangePink2
                    ),
                    startY = 0f,
                    endY = size.height * 0.4f
                ),
                size = size
            )

            val whitePath = Path().apply {
                moveTo(0f, size.height / 3)
                cubicTo(
                    waveLength / 2, size.height / 4 - waveHeight,
                    waveLength * 2 / 3, size.height / 2 + waveHeight,
                    waveLength, size.height / 3,
                )
                lineTo(waveLength, size.height)
                lineTo(0f, size.height)
                close()
            }

            // Gradient untuk area putih
            drawPath(
                path = whitePath,
                color = Color.White
            )
        }
        content()
    }
}