package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.theme.DotGray
import com.teladanprimaagro.tmpp.ui.theme.TextGray
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Section (Back, Title, Settings)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = "Pengaturan",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* TODO: Aksi pengaturan global jika ada */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Pengaturan Umum",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Main Content Area (Rounded Black Section)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Menu Item: Tema
            SettingMenuItem(text = "Tema") {
                // TODO: Aksi untuk mengubah tema (misalnya, menampilkan dialog pilihan tema)
            }
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            // Menu Item: Kemandoran
            SettingMenuItem(text = "Kemandoran") {
                navController.navigate("kelola_kemandoran_screen")
            }
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            // Menu Item: Pemanen
            SettingMenuItem(text = "Pemanen") {
                navController.navigate("kelola_pemanen_screen")
            }
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            // Menu Item: Blok
            SettingMenuItem(text = "Blok") {
                navController.navigate("kelola_blok_screen")
            }
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            // Menu Item: No. TPH
            SettingMenuItem(text = "No. TPH") {
                navController.navigate("kelola_tph_screen")
            }
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.weight(1f)) // Dorong footer ke bawah

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

@Composable
fun SettingMenuItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        // Menambahkan ikon panah kanan
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Lihat Detail",
            tint = TextGray
        )
    }
}