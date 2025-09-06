package com.teladanprimaagro.tmpp.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.PanenViewModel
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanenExportScreen(
    navController: NavController,
    panenViewModel: PanenViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allPanenData by panenViewModel.allPanenData.collectAsState()

    // Launcher untuk membuat file CSV
    val createCsvFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                exportDataToFile(context, uri, allPanenData, "text/csv")
            }
        } else {
            Toast.makeText(context, "Ekspor dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher untuk membuat file JSON
    val createJsonFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                exportDataToFile(context, uri, allPanenData, "application/json")
            }
        } else {
            Toast.makeText(context, "Ekspor dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ekspor Data Panen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MainBackground)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ExportButton(
                    text = "Cetak",
                    icon = Icons.Default.Print,
                    onClick = { Toast.makeText(context, "Fitur cetak belum diimplementasikan", Toast.LENGTH_SHORT).show() },
                    enabled = allPanenData.isNotEmpty()
                )
                ExportButton(
                    text = "CSV",
                    icon = Icons.Default.Description,
                    onClick = {
                        if (allPanenData.isNotEmpty()) {
                            val defaultFileName = "panen_data_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.csv"
                            createCsvFileLauncher.launch(defaultFileName)
                        } else {
                            Toast.makeText(context, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = allPanenData.isNotEmpty()
                )
                ExportButton(
                    text = "JSON",
                    icon = Icons.Default.Description,
                    onClick = {
                        if (allPanenData.isNotEmpty()) {
                            val defaultFileName = "panen_data_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.json"
                            createJsonFileLauncher.launch(defaultFileName)
                        } else {
                            Toast.makeText(context, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = allPanenData.isNotEmpty()
                )
            }

            if (allPanenData.isEmpty()) {
                Text(
                    text = "Tidak ada data panen untuk ditampilkan.",
                    color = White,
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(allPanenData) { panen -> PanenItemCard(panen) }
                }
            }
        }
    }
}

@Composable
fun ExportButton(text: String, icon: ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(100.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MainColor),
        enabled = enabled
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = text, tint = White)
            Text(text, color = White, fontSize = 12.sp)
        }
    }
}

@Composable
fun PanenItemCard(panen: PanenData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Unique No: ${panen.uniqueNo}",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tanggal: ${panen.tanggalWaktu}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Blok: ${panen.blok}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Total Buah: ${panen.totalBuah}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Status: ${if (panen.isSynced) "Sudah Disinkronkan" else "Belum Disinkronkan"}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun exportDataToFile(context: Context, uri: Uri, dataList: List<PanenData>, mimeType: String) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            when (mimeType) {
                "text/csv" -> {
                    val writer = OutputStreamWriter(outputStream)
                    val header = "tanggalWaktu,uniqueNo,blok,totalBuah,isSynced\n"
                    writer.write(header)
                    dataList.forEach { data ->
                        val line = "${data.tanggalWaktu},${data.uniqueNo},${data.blok},${data.totalBuah},${data.isSynced}\n"
                        writer.write(line)
                    }
                    writer.flush()
                    writer.close()
                }
                "application/json" -> {
                    val gson = Gson()
                    val jsonString = gson.toJson(dataList)
                    outputStream.write(jsonString.toByteArray())
                }
            }
        }
        Toast.makeText(context, "Data berhasil diekspor!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        android.util.Log.e("PanenExportScreen", "Export failed: ${e.message}", e)
        Toast.makeText(context, "Gagal mengekspor data: ${e.message}", Toast.LENGTH_LONG).show()
    }
}