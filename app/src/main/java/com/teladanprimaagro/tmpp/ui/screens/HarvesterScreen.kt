// src/main/java/com/teladanprimaagro/tmpp/ui/screens/HarvesterContent.kt

package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Equalizer // <-- Tambahkan ikon baru ini
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.MenuButton

@Composable
fun HarvesterContent(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuButton(
                    text = "Panen",
                    icon = Icons.Default.Add,
                    onClick = { navController.navigate("panen_input_screen") }
                )

                MenuButton(
                    text = "Rekap Panen",
                    icon = Icons.Default.Description,
                    onClick = { navController.navigate("rekap_panen_screen") }
                )
            }

            // --- Baris 2: Statistik dan Pengaturan ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Statistik (BARU)
                MenuButton(
                    text = "Statistik",
                    icon = Icons.Default.Equalizer,
                    onClick = { navController.navigate("statistik_panen_screen") } // Rute navigasi baru
                )

                // Tombol Pengaturan
                MenuButton(
                    text = "Pengaturan",
                    icon = Icons.Default.Settings,
                    onClick = { navController.navigate("pengaturan_screen") }
                )
            }
        }
    }
}