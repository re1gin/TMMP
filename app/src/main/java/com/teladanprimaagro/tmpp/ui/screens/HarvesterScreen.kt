package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.Grey
import com.teladanprimaagro.tmpp.ui.theme.LightGrey
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.OldGrey
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.PanenViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HarvesterContent(
    navController: NavController,
    modifier: Modifier = Modifier,
    panenViewModel: PanenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
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
                        text = "Harvester!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Kanan
                Box(
                    modifier = Modifier
                        .background(MainColor, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Krani Panen",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        color = Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DashboardCard(navController = navController, panenViewModel = panenViewModel)

            Spacer(modifier = Modifier.height(24.dp))

            // Label "Aksi Utama"
            Text(
                text = "Aksi Utama",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = White,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Baris tombol Aksi Utama
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuButton(
                    text = "Panen",
                    icon = Icons.Default.Add,
                    onClick = { navController.navigate("panenInputScreen/-1") },
                    modifier = Modifier.weight(1f),
                    backgroundColor = MainColor
                )
                MenuButton(
                    text = "Rekap Panen",
                    icon = Icons.Default.Description,
                    onClick = { navController.navigate("rekap_panen_screen") },
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
                    onClick = { navController.navigate("statistik_panen_screen") },
                    modifier = Modifier.weight(1f),
                    backgroundColor = LightGrey
                )
                CustomMenuButton(
                    text = "Upload",
                    icon = Icons.Default.CloudDone,
                    onClick = { navController.navigate("data_terkirim_screen") },
                    modifier = Modifier.weight(1f),
                    backgroundColor = LightGrey
                )
                CustomMenuButton(
                    text = "Lokasi",
                    icon = Icons.Default.Settings,
                    onClick = { navController.navigate("peta_screen") },
                    modifier = Modifier.weight(1f),
                    backgroundColor = LightGrey
                )
            }
        }
    }
}

@Composable
fun DashboardCard(navController: NavController, panenViewModel: PanenViewModel) {
    val totalDataMasuk by panenViewModel.totalDataMasuk.collectAsState()
    val totalSemuaBuah by panenViewModel.totalSemuaBuah.collectAsState()

    // Get the current date
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
            // Bagian atas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Data Panen", fontSize = 12.sp, color = Color.Black)
                Text(text = currentDate, fontSize = 12.sp, color = Color.Black)
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
                    title = "Data Masuk",
                    value = "$totalDataMasuk DATA",
                    modifier = Modifier.weight(1f)
                )
                DataBox(
                    title = "Total Janjang",
                    value = "$totalSemuaBuah JJ",
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
                    onClick = { navController.navigate("statistik_panen_screen") },
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

// DataBox helper
@Composable
fun DataBox(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(White, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(text = title, fontSize = 14.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Black)
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(12.dp) // kasih ruang dalam tombol
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp) // ukuran lingkaran
                    .background(White.copy(0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = Black.copy(0.8f),
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = text,
                color = Black,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun CustomMenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(100.dp), // tinggi 100 dp
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(12.dp) // ruang dalam tombol
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Lingkaran dengan ikon di tengah
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = OldGrey,
                    modifier = Modifier.size(48.dp) // ukuran ikon
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                color = Black,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
