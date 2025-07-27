package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.teladanprimaagro.tmpp.ui.theme.DotGray
import com.teladanprimaagro.tmpp.ui.theme.TeladanPrimaAgroTheme
import com.teladanprimaagro.tmpp.ui.theme.TextGray

@Composable
fun HarvesterScreen(navController: NavController) {

    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Latar belakang abu-abu gelap
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp) // Tinggi tetap untuk header
                .background(MaterialTheme.colorScheme.primary), // Warna oranye
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "BPBKS",
                color = MaterialTheme.colorScheme.onPrimary, // Warna teks kontras
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                fontWeight = FontWeight.Bold
            )
        }

        // Main Content Area (Rounded Black Section) dengan animasi fade-in
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 200)), // Fade in setelah 200ms
            exit = fadeOut(animationSpec = tween(durationMillis = 300)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surface, // Warna hitam gelap
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Grid untuk Tombol Menu (Panen, Rekap Panen, Pengaturan)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Spasi antar baris tombol
                ) {
                    // Baris 1: Dua Tombol "Panen"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround, // Spasi merata
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol Panen
                        MenuButton(
                            text = "Panen",
                            icon = Icons.Default.Add, // Ikon tambah
                            onClick = { navController.navigate("panen_input_screen") }
                        )

                        // Tombol Rekap Panen
                        MenuButton(
                            text = "Rekap Panen",
                            icon = Icons.Default.Description, // Ikon dokumen/deskripsi
                            onClick = { navController.navigate("rekap_panen_screen") }
                        )
                    }

                    // Baris 2: Tombol "Rekap Panen" dan "Pengaturan"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol Pengaturan
                        MenuButton(
                            text = "Pengaturan",
                            icon = Icons.Default.Settings, // Ikon pengaturan
                            onClick = { navController.navigate("pengaturan_screen") }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f)) // Dorong konten ke atas

                // Version and Dots (Footer)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(DotGray, shape = RoundedCornerShape(50))
                                .padding(horizontal = 4.dp)
                        )
                        if (it < 2) Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Text(
                    text = "Version: V 1.0.0.0",
                    color = TextGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Untuk Card onClick
@Composable
fun MenuButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f, // Sedikit mengecil saat ditekan
        animationSpec = tween(durationMillis = 100)
    )

    Card(
        modifier = Modifier
            .size(140.dp, 160.dp) // Ukuran tombol kartu
            .graphicsLayer {
                scaleX = scale // Terapkan animasi skala
                scaleY = scale // Terapkan animasi skala
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), // Warna oranye untuk tombol
        onClick = onClick,
        interactionSource = interactionSource // Kaitkan interactionSource dengan Card
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimary, // Warna ikon putih
                modifier = Modifier.size(48.dp) // Ukuran ikon
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary, // Warna teks putih
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HarvesterScreenPreview() {
    TeladanPrimaAgroTheme {
        HarvesterScreen(rememberNavController())
    }
}