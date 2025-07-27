package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.teladanprimaagro.tmpp.ui.data.PanenData
import com.teladanprimaagro.tmpp.ui.theme.BackgroundLightGray
import com.teladanprimaagro.tmpp.ui.theme.DotGray
import com.teladanprimaagro.tmpp.ui.theme.PrimaryOrange
import com.teladanprimaagro.tmpp.ui.theme.TeladanPrimaAgroTheme
import com.teladanprimaagro.tmpp.ui.theme.TextGray
import com.teladanprimaagro.tmpp.ui.viewmodels.PanenViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekapPanenScreen(navController: NavController, panenViewModel: PanenViewModel) {

    // Mengambil data panen dari ViewModel
    val panenList = panenViewModel.panenList

    // Hitung total data dan total buah dari data ViewModel
    val totalDataMasuk = panenList.size
    val totalSemuaBuah = panenList.sumOf { it.totalBuah }

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
                text = "Rekap Panen",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* TODO: Aksi pengaturan */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Pengaturan",
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Sortir dan Kelompokkan Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Aksi Sortir */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        containerColor = Color.Transparent
                    ),
                    border = BorderStroke(1.dp, DotGray)
                ) {
                    Text("Sortir")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { /* TODO: Aksi Kelompokkan */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        containerColor = Color.Transparent
                    ),
                    border = BorderStroke(1.dp, DotGray)
                ) {
                    Text("Kelompokkan")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Tabel Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryOrange, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderText(text = "Tanggal/Waktu", weight = 0.2f)
                TableHeaderText(text = "Nama Pemanen", weight = 0.25f)
                TableHeaderText(text = "Blok", weight = 0.15f)
                TableHeaderText(text = "Total Buah", weight = 0.2f)
                TableHeaderText(text = "Edit", weight = 0.1f)
                TableHeaderText(text = "Detail", weight = 0.1f)
            }

            // Tabel Data (menggunakan LazyColumn untuk efisiensi scroll)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(BackgroundLightGray.copy(alpha = 0.1f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            ) {
                if (panenList.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada data panen.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = TextGray
                        )
                    }
                } else {
                    items(panenList) { data ->
                        TableRow(data = data)
                        HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.5f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Total Data Masuk dan Total Buah
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryBox(label = "Data Masuk", value = totalDataMasuk.toString())
                SummaryBox(label = "Total Buah", value = totalSemuaBuah.toString())
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Catatan Kaki
            Text(
                text = "*Data akan di reset setiap pukul 00.00",
                color = TextGray,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

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

// ... (TableHeaderText, TableRow, TableCellText, SummaryBox tidak berubah)

@Composable
fun RowScope.TableHeaderText(text: String, weight: Float) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
fun TableRow(data: PanenData) { // <-- Pastikan ini menerima PanenData yang lengkap
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tampilkan data yang sesuai dengan kolom tabel
        TableCellText(text = data.tanggalWaktu, weight = 0.2f)
        TableCellText(text = data.namaPemanen, weight = 0.25f)
        TableCellText(text = data.blok, weight = 0.15f)
        TableCellText(text = data.totalBuah.toString(), weight = 0.2f)
        // Icon Edit
        Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = PrimaryOrange,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { /* TODO: Aksi edit */ }
            )
        }
        // Icon Detail
        Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Detail",
                tint = PrimaryOrange,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { /* TODO: Aksi detail */ }
            )
        }
    }
}

@Composable
fun RowScope.TableCellText(text: String, weight: Float) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
fun SummaryBox(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(160.dp)
            .background(PrimaryOrange, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Preview(showBackground = true)
@Composable
fun RekapPanenScreenPreview() {
    TeladanPrimaAgroTheme {
        RekapPanenScreen(rememberNavController(), panenViewModel = viewModel())
    }
}