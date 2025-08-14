package com.teladanprimaagro.tmpp.ui.screens


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.FullDateCard
import com.teladanprimaagro.tmpp.ui.components.MenuButton
import com.teladanprimaagro.tmpp.ui.theme.primaryGradient
import com.teladanprimaagro.tmpp.ui.theme.secondaryGradient

// Definisikan palet warna gradien di sini atau impor dari file Color.kt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DriverContent(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FullDateCard(title = "Hai, Driver!")
        Spacer(modifier = Modifier.height(24.dp))

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
                    gradientColors = primaryGradient,
                    onClick = { navController.navigate("scan_input_screen") }
                )

                // Tombol Pengiriman
                MenuButton(
                    text = "Pengiriman",
                    icon = Icons.Default.DriveEta,
                    gradientColors = secondaryGradient,
                    onClick = { navController.navigate("pengiriman_input_screen") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuButton(
                    text = "Rekap",
                    icon = Icons.Default.Description,
                    gradientColors = primaryGradient,
                    onClick = { navController.navigate("rekap_pengiriman_screen") }
                )

                // Tombol Unggah Data
                MenuButton(
                    text = "Unggah Data",
                    icon = Icons.Default.Backup,
                    gradientColors = secondaryGradient,
                    onClick = { navController.navigate("data_pengiriman_screen")}
                )
            }
        }
    }
}