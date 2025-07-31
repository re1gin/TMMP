package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.MenuButton // Pastikan ini diimpor dengan benar

@Composable
fun HarvesterContent(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
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

            // Baris 2: Tombol "Pengaturan"
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
    }
}