package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.FullDateCard
import com.teladanprimaagro.tmpp.ui.components.MenuButton


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HarvesterContent(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(0.dp))
        FullDateCard(title = "Hai, Harvester!")

        Spacer(modifier = Modifier.height(50.dp))

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
                    onClick = { navController.navigate("panenInputScreen/-1") }
                )
                MenuButton(
                    text = "Rekap Panen",
                    icon = Icons.Default.Description,
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
                    onClick = { navController.navigate("statistik_panen_screen") }
                )
                MenuButton(
                    text = "Data Terkirim",
                    icon = Icons.Default.CloudDone,
                    onClick = { navController.navigate("data_terkirim_screen") }
                )
            }
        }
    }
}
