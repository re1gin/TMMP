package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.MenuButton

@Composable
fun DriverContent(navController: NavController, modifier: Modifier = Modifier) {
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
                // Tombol Scan
                MenuButton(
                    text = "Scan",
                    icon = Icons.Default.Nfc,
                    onClick = { navController.navigate("scan_input_screen") } // Rute baru untuk scan
                )

                // Tombol Pengiriman
                MenuButton(
                    text = "Pengiriman",
                    icon = Icons.Default.CarRental, // Ikon pengiriman
                    onClick = { navController.navigate("pengiriman_input_screen") } // Rute baru untuk pengiriman
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuButton(
                    text = "Rekap",
                    icon = Icons.Default.Description, // Ikon rekap
                    onClick = { navController.navigate("rekap_pengiriman_screen") } // Rute baru untuk rekap pengiriman
                )

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