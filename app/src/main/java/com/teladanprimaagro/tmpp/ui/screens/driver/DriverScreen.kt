package com.teladanprimaagro.tmpp.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.CustomMenuButton
import com.teladanprimaagro.tmpp.ui.components.DataBox
import com.teladanprimaagro.tmpp.ui.components.MenuButton
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.Grey
import com.teladanprimaagro.tmpp.ui.theme.LightGrey
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.OldGrey
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverContent(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ineka",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(27.dp)
                            .background(MainColor, CircleShape)
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(27.dp)
                            .background(MainColor, CircleShape)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kiri
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Selamat Datang",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )
                    Text(
                        text = "Driver!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { navController.navigate("nfc_scanner_screen") },
                        colors = ButtonDefaults.buttonColors(containerColor = OldGrey),
                        shape = RoundedCornerShape(15.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .width(60.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "Baca NFC",
                            tint = White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DashboardCardDriver(navController = navController)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Aksi Utama",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = White,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuButton(
                    text = "Scan",
                    icon = Icons.Default.Nfc,
                    onClick = { navController.navigate("scan_input_screen") },
                    modifier = Modifier.weight(1f),
                    backgroundColor = MainColor
                )
                MenuButton(
                    text = "Pengiriman",
                    icon = Icons.Default.DriveEta,
                    onClick = { navController.navigate("pengiriman_input_screen") },
                    modifier = Modifier.weight(1f),
                    backgroundColor = LightGrey
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Label "Menu lain"
            Text(
                text = "Menu lain",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Baris tombol Menu lain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomMenuButton(
                    text = "Statistik",
                    icon = Icons.Default.Equalizer,
                    onClick = { navController.navigate("statistik_pengiriman_screen") },
                    modifier = Modifier.weight(1f),
                    backgroundColor = LightGrey
                )
                CustomMenuButton(
                    text = "Unggah",
                    icon = Icons.Default.Backup,
                    onClick = { navController.navigate("data_pengiriman_screen") },
                    modifier = Modifier.weight(1f),
                    backgroundColor = LightGrey
                )
                CustomMenuButton(
                    text = "Rekap",
                    icon = Icons.Default.Description,
                    onClick = { navController.navigate("rekap_pengiriman_screen") },
                    modifier = Modifier.weight(1f),
                    backgroundColor = LightGrey
                )
            }
        }
    }
}

@Composable
fun DashboardCardDriver(navController: NavController, pengirimanViewModel: PengirimanViewModel = viewModel()) {
    val totalSemuaBuah by pengirimanViewModel.totalSemuaBuah.collectAsState()
    val totalSuccessfulScans by pengirimanViewModel.totalSuccessfulScans.collectAsState()

    val currentDate = remember {
        val formatter = SimpleDateFormat("EEEE, dd-MM-yyyy", Locale.getDefault())
        formatter.format(Date())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Grey)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Data Pengiriman", fontSize = 12.sp, color = Black)
                Text(text = currentDate, fontSize = 12.sp, color = Black)
            }

            Text(
                text = "Teladan Prima Agro",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DataBox(
                    title = "Jumlah Scan",
                    value = "$totalSuccessfulScans Tag",
                    modifier = Modifier.weight(1f)
                )
                DataBox(
                    title = "Total Buah",
                    value = "$totalSemuaBuah JJG",
                    modifier = Modifier.weight(1f)
                )
            }

            // Bagian bawah
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "“Lihat Statistik” untuk detail",
                    fontSize = 10.sp,
                    color = Black
                )

                Button(
                    onClick = { navController.navigate("statistik_pengiriman_screen") },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = OldGrey),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = "Lihat Statistik",
                        fontSize = 12.sp,
                        color = White
                    )
                }
            }
        }
    }
}
