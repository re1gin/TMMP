package com.teladanprimaagro.tmpp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.teladanprimaagro.tmpp.data.PanenData
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanenDetailDialog(
    panenData: PanenData,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Mengisi 90% lebar layar
                .fillMaxHeight(0.8f) // Mengisi 80% tinggi layar
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Detail Data Panen",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val imageToDisplay = panenData.localImageUri ?: panenData.firebaseImageUrl

                imageToDisplay?.let { uriString ->
                    val imageUri = uriString.toUri()
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUri),
                        contentDescription = "Gambar Panen",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // --- AKHIR PERUBAHAN ---

                DetailRow("No. Unik:", panenData.uniqueNo)
                DetailRow("Tanggal/Waktu:", panenData.tanggalWaktu)
                DetailRow("Latitude:", panenData.locationPart1)
                DetailRow("Longitude:", panenData.locationPart2)
                DetailRow("Kemandoran:", panenData.kemandoran)
                DetailRow("Nama Pemanen:", panenData.namaPemanen)
                DetailRow("Blok:", panenData.blok)
                DetailRow("No. TPH:", panenData.noTph)
                DetailRow("Total Buah:", panenData.totalBuah.toString())
                DetailRow("Buah N:", panenData.buahN.toString())
                DetailRow("Buah A:", panenData.buahA.toString())
                DetailRow("Buah OR:", panenData.buahOR.toString())
                DetailRow("Buah E:", panenData.buahE.toString())
                DetailRow("Buah AB:", panenData.buahAB.toString())
                DetailRow("Berondolan Lepas:", panenData.buahBL.toString())

                // Tambahan: Tampilkan status sinkronisasi
                DetailRow("Status Sinkron:", if (panenData.isSynced) "Sudah Sinkron" else "Belum Sinkron")


                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text("Tutup")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.6f)
        )
    }
}